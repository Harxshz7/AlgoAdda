package com.algoadda.core.listing;

import com.algoadda.core.bot.*;
import com.algoadda.core.bot.dto.PublishRequest;
import com.algoadda.core.bot.dto.PublishResponse;
import com.algoadda.core.bot.service.BotService;
import com.algoadda.core.compliance.ComplianceCheck;
import com.algoadda.core.compliance.ComplianceCheckRepository;
import com.algoadda.core.compliance.ReviewerType;
import com.algoadda.core.user.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("null")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StoreEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SellerProfileRepository sellerProfileRepository;

    @Autowired
    private BotRepository botRepository;

    @Autowired
    private BotVersionRepository botVersionRepository;

    @Autowired
    private ComplianceCheckRepository complianceCheckRepository;

    @Autowired
    private BacktestResultRepository backtestResultRepository;

    @Autowired
    private com.algoadda.core.bot.BacktestJobRepository backtestJobRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private ListingViewRepository listingViewRepository;

    @Autowired
    private BotService botService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User officialSeller;
    private User thirdPartySeller;

    private Listing officialListing;
    private Listing thirdPartyListing;

    @BeforeEach
    void setUp() {
        listingViewRepository.deleteAll();
        listingRepository.deleteAll();
        complianceCheckRepository.deleteAll();
        backtestJobRepository.deleteAll();
        backtestResultRepository.deleteAll();
        botVersionRepository.deleteAll();
        botRepository.deleteAll();
        sellerProfileRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Create Official Seller (email matches default config official@algoadda.com)
        officialSeller = userRepository.save(User.builder()
            .email("official@algoadda.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .role(Role.SELLER)
            .build());

        sellerProfileRepository.save(SellerProfile.builder()
            .user(officialSeller)
            .displayName("AlgoAdda Official Labs")
            .bio("First-party verified algorithmic strategies by Harsha.")
            .kycStatus(KycStatus.VERIFIED)
            .build());

        // 2. Create Third-Party Seller
        thirdPartySeller = userRepository.save(User.builder()
            .email("thirdparty_quant@gmail.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .role(Role.SELLER)
            .build());

        sellerProfileRepository.save(SellerProfile.builder()
            .user(thirdPartySeller)
            .displayName("Community Developer X")
            .bio("Independent quantitative strategy developer.")
            .kycStatus(KycStatus.VERIFIED)
            .build());

        // 3. Seed Official Bot + Version + Passed Compliance + Publish
        Bot botOfficial = botRepository.save(Bot.builder()
            .seller(officialSeller)
            .name("AlgoAdda Institutional Momentum")
            .description("Official flagship momentum trading bot")
            .riskDisclaimer("Capital at risk.")
            .strategyType("MOMENTUM")
            .status(BotStatus.PENDING_REVIEW)
            .build());

        BotVersion vOfficial = botVersionRepository.save(BotVersion.builder()
            .bot(botOfficial)
            .versionNumber("1.0.0")
            .disclosedLogic("EMA 20/50 Crossover with ATR volatility filter")
            .fileStorageKey("bots/official/strategy.py")
            .backtestStatus(BacktestStatus.COMPLETED)
            .build());

        complianceCheckRepository.save(ComplianceCheck.builder()
            .botVersion(vOfficial)
            .reviewerType(ReviewerType.AUTO)
            .checklistResults("{\"disclosed_logic_present\": true}")
            .passed(true)
            .reviewedAt(Instant.now())
            .build());

        PublishRequest pubReq1 = new PublishRequest();
        pubReq1.setPrice(new BigDecimal("199.99"));
        pubReq1.setLicenseType(LicenseType.ONE_TIME);

        PublishResponse pubRes1 = botService.publishBotVersion(officialSeller.getId(), botOfficial.getId(), vOfficial.getId(), pubReq1);
        assertTrue(pubRes1.isOfficial());
        officialListing = listingRepository.findById(pubRes1.getListingId()).orElseThrow();

        // 4. Seed Third-Party Bot + Version + Passed Compliance + Publish
        Bot botThirdParty = botRepository.save(Bot.builder()
            .seller(thirdPartySeller)
            .name("Community Scalper Bot")
            .description("Third-party community scalping algorithm")
            .riskDisclaimer("Substantial risk of loss.")
            .strategyType("SCALPING")
            .status(BotStatus.PENDING_REVIEW)
            .build());

        BotVersion vThirdParty = botVersionRepository.save(BotVersion.builder()
            .bot(botThirdParty)
            .versionNumber("1.0.0")
            .disclosedLogic("RSI oversold rebound")
            .fileStorageKey("bots/thirdparty/strategy.py")
            .backtestStatus(BacktestStatus.COMPLETED)
            .build());

        complianceCheckRepository.save(ComplianceCheck.builder()
            .botVersion(vThirdParty)
            .reviewerType(ReviewerType.AUTO)
            .checklistResults("{\"disclosed_logic_present\": true}")
            .passed(true)
            .reviewedAt(Instant.now())
            .build());

        PublishRequest pubReq2 = new PublishRequest();
        pubReq2.setPrice(new BigDecimal("49.99"));
        pubReq2.setLicenseType(LicenseType.ONE_TIME);

        PublishResponse pubRes2 = botService.publishBotVersion(thirdPartySeller.getId(), botThirdParty.getId(), vThirdParty.getId(), pubReq2);
        assertFalse(pubRes2.isOfficial());
        thirdPartyListing = listingRepository.findById(pubRes2.getListingId()).orElseThrow();
    }

    @Test
    @DisplayName("1. Store Listings Endpoint (/api/store/listings): Returns ONLY official listings")
    void testOfficialStoreListingsEndpoint() throws Exception {
        mockMvc.perform(get("/api/store/listings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].listingId").value(officialListing.getId().toString()))
            .andExpect(jsonPath("$.content[0].name").value("AlgoAdda Institutional Momentum"))
            .andExpect(jsonPath("$.content[0].official").value(true));
    }

    @Test
    @DisplayName("2. Public Marketplace Endpoint (/api/listings): Returns BOTH official & third-party listings")
    void testGeneralMarketplaceIncludesOfficial() throws Exception {
        mockMvc.perform(get("/api/listings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.content[*].listingId", hasItems(
                officialListing.getId().toString(),
                thirdPartyListing.getId().toString()
            )));
    }

    @Test
    @DisplayName("3. Store Listing Detail Endpoint (/api/store/listings/{id}): 200 OK for official, 404 for third-party")
    void testOfficialStoreDetailAccessControl() throws Exception {
        // Official listing access via /api/store/listings/{id} -> 200 OK
        mockMvc.perform(get("/api/store/listings/" + officialListing.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("AlgoAdda Institutional Momentum"))
            .andExpect(jsonPath("$.official").value(true));

        // Third-party listing access via /api/store/listings/{id} -> 404 Not Found
        mockMvc.perform(get("/api/store/listings/" + thirdPartyListing.getId()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
