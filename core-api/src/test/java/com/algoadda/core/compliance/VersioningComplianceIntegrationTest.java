package com.algoadda.core.compliance;

import com.algoadda.core.bot.*;
import com.algoadda.core.bot.dto.PublishRequest;
import com.algoadda.core.bot.service.BacktestServiceClient;
import com.algoadda.core.bot.service.S3StorageService;
import com.algoadda.core.config.JwtTokenProvider;
import com.algoadda.core.listing.LicenseType;
import com.algoadda.core.listing.Listing;
import com.algoadda.core.listing.ListingRepository;
import com.algoadda.core.user.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("null")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VersioningComplianceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BotRepository botRepository;

    @Autowired
    private BotVersionRepository botVersionRepository;

    @Autowired
    private ComplianceCheckRepository complianceCheckRepository;

    @Autowired
    private BacktestResultRepository backtestResultRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ComplianceService complianceService;

    @Autowired
    private com.algoadda.core.bot.BacktestJobRepository backtestJobRepository;

    @MockBean
    private S3StorageService s3StorageService;

    @MockBean
    private BacktestServiceClient backtestServiceClient;

    private String sellerToken;
    private User seller;

    @BeforeEach
    void setUp() {
        listingRepository.deleteAll();
        complianceCheckRepository.deleteAll();
        backtestJobRepository.deleteAll();
        backtestResultRepository.deleteAll();
        botVersionRepository.deleteAll();
        botRepository.deleteAll();
        userRepository.deleteAll();

        seller = userRepository.save(User.builder()
            .email("version_seller@algoadda.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .role(Role.SELLER)
            .build());

        sellerToken = "Bearer " + jwtTokenProvider.generateAccessToken(seller);

        when(s3StorageService.uploadFile(anyString(), any(byte[].class), anyString())).thenReturn("mock-s3-key");
        when(backtestServiceClient.runBacktest(any(BotVersion.class), any(), any(), any())).thenReturn(true);
    }

    @Test
    @DisplayName("Versioning Rule: Uploading v2 starts in DRAFT, does not inherit v1 compliance, and v1 listing stays active")
    void testVersioningComplianceAndListingSafety() throws Exception {
        // 1. Create Bot with v1
        Bot bot = botRepository.save(Bot.builder()
            .seller(seller)
            .name("Multi-Version Trend Bot")
            .description("Trend following strategy")
            .riskDisclaimer("Risk of market loss.")
            .strategyType("TREND_FOLLOWING")
            .status(BotStatus.DRAFT)
            .build());

        BotVersion v1 = botVersionRepository.save(BotVersion.builder()
            .bot(bot)
            .versionNumber("1.0.0")
            .disclosedLogic("v1 EMA crossover logic")
            .backtestStatus(BacktestStatus.COMPLETED)
            .build());

        backtestResultRepository.save(BacktestResult.builder()
            .botVersion(v1)
            .metrics("{}")
            .build());

        // Run compliance and publish v1
        complianceService.runAutomatedComplianceCheck(v1);
        
        Listing v1Listing = listingRepository.save(Listing.builder()
            .botVersion(v1)
            .price(new BigDecimal("49.99"))
            .licenseType(LicenseType.ONE_TIME)
            .active(true)
            .build());

        bot.setStatus(BotStatus.PUBLISHED);
        botRepository.save(bot);

        // 2. Seller uploads Version 2 (v2)
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "strategy_v2.py",
            "text/x-python",
            "print('v2 strategy')".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/bots/" + bot.getId() + "/versions")
                .file(file)
                .header("Authorization", sellerToken)
                .param("disclosedLogic", "v2 improved RSI + EMA logic")
                .param("changelog", "Added RSI filter"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.versionNumber").value("1.1.0"))
            .andExpect(jsonPath("$.backtestStatus").value("QUEUED"));

        // Retrieve created v2 version
        List<BotVersion> versions = botVersionRepository.findByBotId(bot.getId());
        assertEquals(2, versions.size());
        BotVersion v2 = versions.stream()
            .filter(v -> v.getVersionNumber().equals("1.1.0"))
            .findFirst()
            .orElseThrow();

        // 3. Verify v1 Listing remains active and purchasable
        List<Listing> activeListings = listingRepository.findByActiveTrue();
        assertEquals(1, activeListings.size());
        assertEquals(v1Listing.getId(), activeListings.get(0).getId());

        // 4. Attempting to publish v2 before compliance check fails
        PublishRequest publishRequest = new PublishRequest(new BigDecimal("79.99"), LicenseType.ONE_TIME);

        mockMvc.perform(post("/api/bots/" + bot.getId() + "/versions/" + v2.getId() + "/publish")
                .header("Authorization", sellerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(publishRequest)))
            .andExpect(status().isBadRequest());

        // 5. Complete v2 backtest and compliance check
        v2.setBacktestStatus(BacktestStatus.COMPLETED);
        botVersionRepository.save(v2);
        backtestResultRepository.save(BacktestResult.builder()
            .botVersion(v2)
            .metrics("{}")
            .build());

        complianceService.runAutomatedComplianceCheck(v2);

        // 6. Now publish v2 successfully
        mockMvc.perform(post("/api/bots/" + bot.getId() + "/versions/" + v2.getId() + "/publish")
                .header("Authorization", sellerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(publishRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.price").value(79.99));

        // 7. Confirm v1 listing still remains active alongside v2 listing (seller explicitly deactivates v1 if desired)
        List<Listing> allListings = listingRepository.findByBotVersionId(v1.getId());
        assertEquals(1, allListings.size());
        assertTrue(allListings.get(0).isActive(), "v1 listing should remain active when v2 is published");
    }
}
