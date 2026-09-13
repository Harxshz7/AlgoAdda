package com.algoadda.core.compliance;

import com.algoadda.core.bot.*;
import com.algoadda.core.bot.dto.PublishRequest;
import com.algoadda.core.compliance.dto.AdminReviewRequest;
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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("null")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PublishAndReviewIntegrationTest {

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

    private String sellerToken;
    private String adminToken;
    private User seller;
    private User admin;

    @BeforeEach
    void setUp() {
        listingRepository.deleteAll();
        complianceCheckRepository.deleteAll();
        backtestResultRepository.deleteAll();
        botVersionRepository.deleteAll();
        botRepository.deleteAll();
        userRepository.deleteAll();

        seller = userRepository.save(User.builder()
            .email("seller_compliance@algoadda.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .role(Role.SELLER)
            .build());

        admin = userRepository.save(User.builder()
            .email("admin_compliance@algoadda.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .role(Role.ADMIN)
            .build());

        sellerToken = "Bearer " + jwtTokenProvider.generateAccessToken(seller);
        adminToken = "Bearer " + jwtTokenProvider.generateAccessToken(admin);
    }

    @Test
    @DisplayName("Publish Gate: Rejects publish when compliance check fails due to guaranteed language")
    void testPublishBlockedWhenComplianceFails() throws Exception {
        Bot bot = botRepository.save(Bot.builder()
            .seller(seller)
            .name("Guaranteed Return Bot")
            .description("This strategy promises 100% profit with zero risk")
            .riskDisclaimer("Risk disclaimer text")
            .strategyType("MOMENTUM")
            .status(BotStatus.DRAFT)
            .build());

        BotVersion version = botVersionRepository.save(BotVersion.builder()
            .bot(bot)
            .versionNumber("1.0.0")
            .disclosedLogic("Guaranteed returns momentum strategy")
            .backtestStatus(BacktestStatus.COMPLETED)
            .build());

        backtestResultRepository.save(BacktestResult.builder()
            .botVersion(version)
            .metrics("{}")
            .build());

        // Run automated compliance check (will fail due to blocklisted phrase)
        complianceService.runAutomatedComplianceCheck(version);

        PublishRequest publishRequest = new PublishRequest(new BigDecimal("99.99"), LicenseType.ONE_TIME);

        mockMvc.perform(post("/api/bots/" + bot.getId() + "/versions/" + version.getId() + "/publish")
                .header("Authorization", sellerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(publishRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Publish blocked: Compliance check failed")));
    }

    @Test
    @DisplayName("Publish Gate: Succeeds when compliance check passes")
    void testPublishSucceedsWhenCompliancePasses() throws Exception {
        Bot bot = botRepository.save(Bot.builder()
            .seller(seller)
            .name("Clean Scalper")
            .description("Clean momentum scalping strategy")
            .riskDisclaimer("Trading involves high risk of loss.")
            .strategyType("SCALPING")
            .status(BotStatus.DRAFT)
            .build());

        BotVersion version = botVersionRepository.save(BotVersion.builder()
            .bot(bot)
            .versionNumber("1.0.0")
            .disclosedLogic("Pure EMA cross scalper")
            .backtestStatus(BacktestStatus.COMPLETED)
            .build());

        backtestResultRepository.save(BacktestResult.builder()
            .botVersion(version)
            .metrics("{}")
            .build());

        // Run automated compliance check (will pass)
        complianceService.runAutomatedComplianceCheck(version);

        PublishRequest publishRequest = new PublishRequest(new BigDecimal("149.00"), LicenseType.ONE_TIME);

        mockMvc.perform(post("/api/bots/" + bot.getId() + "/versions/" + version.getId() + "/publish")
                .header("Authorization", sellerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(publishRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.botStatus").value("PUBLISHED"))
            .andExpect(jsonPath("$.price").value(149.00))
            .andExpect(jsonPath("$.active").value(true));

        // Verify Bot status updated to PUBLISHED
        Bot updatedBot = botRepository.findById(bot.getId()).orElseThrow();
        assertEquals(BotStatus.PUBLISHED, updatedBot.getStatus());

        // Verify Listing created
        List<Listing> listings = listingRepository.findByBotVersionId(version.getId());
        assertEquals(1, listings.size());
        assertTrue(listings.get(0).isActive());
    }

    @Test
    @DisplayName("Admin Queue & Manual Review Override: Admin reviews failed check, overrides to passed, allowing seller to publish")
    void testAdminManualReviewOverride() throws Exception {
        Bot bot = botRepository.save(Bot.builder()
            .seller(seller)
            .name("Edge Case Bot")
            .description("Description with borderline phrase")
            .riskDisclaimer("Standard market risks apply.")
            .strategyType("ARBITRAGE")
            .status(BotStatus.DRAFT)
            .build());

        BotVersion version = botVersionRepository.save(BotVersion.builder()
            .bot(bot)
            .versionNumber("1.0.0")
            .disclosedLogic("Arbitrage strategy with guaranteed execution speed") // triggers 'guaranteed'
            .backtestStatus(BacktestStatus.COMPLETED)
            .build());

        backtestResultRepository.save(BacktestResult.builder()
            .botVersion(version)
            .metrics("{}")
            .build());

        // Run automated check -> fails
        complianceService.runAutomatedComplianceCheck(version);

        // 1. Check admin queue includes this version
        mockMvc.perform(get("/api/admin/compliance/queue")
                .header("Authorization", adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].botVersionId").value(version.getId().toString()))
            .andExpect(jsonPath("$[0].passed").value(false));

        // 2. Admin submits manual review override
        AdminReviewRequest adminReviewRequest = new AdminReviewRequest(true, "Reviewed execution speed guarantee context; approved manually.");

        mockMvc.perform(post("/api/admin/compliance/" + version.getId() + "/review")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminReviewRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reviewerType").value("MANUAL"))
            .andExpect(jsonPath("$.passed").value(true));

        // 3. Seller publishes now that manual review passed
        PublishRequest publishRequest = new PublishRequest(new BigDecimal("199.99"), LicenseType.ONE_TIME);

        mockMvc.perform(post("/api/bots/" + bot.getId() + "/versions/" + version.getId() + "/publish")
                .header("Authorization", sellerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(publishRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.botStatus").value("PUBLISHED"));
    }
}
