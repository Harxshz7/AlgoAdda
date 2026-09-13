package com.algoadda.core.listing;

import com.algoadda.core.bot.*;
import com.algoadda.core.compliance.ComplianceCheckRepository;
import com.algoadda.core.compliance.ComplianceService;
import com.algoadda.core.listing.dto.PageResponse;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("null")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ListingPublicEndpointTest {

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
    private ListingRepository listingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ComplianceService complianceService;

    private User seller;

    @BeforeEach
    void setUp() {
        listingRepository.deleteAll();
        complianceCheckRepository.deleteAll();
        backtestResultRepository.deleteAll();
        botVersionRepository.deleteAll();
        botRepository.deleteAll();
        sellerProfileRepository.deleteAll();
        userRepository.deleteAll();

        seller = userRepository.save(User.builder()
            .email("alpha_quant@algoadda.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .role(Role.SELLER)
            .build());

        sellerProfileRepository.save(SellerProfile.builder()
            .user(seller)
            .displayName("Alpha Quant Capital")
            .bio("Institutional-grade algorithmic trading systems.")
            .kycStatus(KycStatus.VERIFIED)
            .build());
    }

    @Test
    @DisplayName("Public Listings: Returns active published listings without auth header, supports filter & search")
    void testPublicListingsEndpoint() throws Exception {
        // 1. Seed published Bot A (Momentum, price $99.99)
        Bot botA = botRepository.save(Bot.builder()
            .seller(seller)
            .name("Nifty Momentum Alpha")
            .description("High frequency momentum strategy on Nifty 50 futures")
            .riskDisclaimer("Capital at risk.")
            .strategyType("MOMENTUM")
            .status(BotStatus.PUBLISHED)
            .build());

        BotVersion vA = botVersionRepository.save(BotVersion.builder()
            .bot(botA)
            .versionNumber("1.0.0")
            .disclosedLogic("EMA 20/50 Crossover")
            .fileStorageKey("bots/secret/path/strategy_a.py")
            .backtestStatus(BacktestStatus.COMPLETED)
            .build());

        backtestResultRepository.save(BacktestResult.builder()
            .botVersion(vA)
            .metrics("{\"win_rate\": 68.5, \"max_drawdown\": 6.2, \"sharpe_ratio\": 2.1}")
            .build());

        Listing listingA = listingRepository.save(Listing.builder()
            .botVersion(vA)
            .price(new BigDecimal("99.99"))
            .licenseType(LicenseType.ONE_TIME)
            .active(true)
            .build());

        // 2. Seed published Bot B (Scalping, price $249.00)
        Bot botB = botRepository.save(Bot.builder()
            .seller(seller)
            .name("BankNifty Scalper Pro")
            .description("Intraday scalping strategy on Bank Nifty index")
            .riskDisclaimer("Substantial risk of loss.")
            .strategyType("SCALPING")
            .status(BotStatus.PUBLISHED)
            .build());

        BotVersion vB = botVersionRepository.save(BotVersion.builder()
            .bot(botB)
            .versionNumber("1.0.0")
            .disclosedLogic("Bollinger Bands Mean Reversion")
            .fileStorageKey("bots/secret/path/strategy_b.py")
            .backtestStatus(BacktestStatus.COMPLETED)
            .build());

        backtestResultRepository.save(BacktestResult.builder()
            .botVersion(vB)
            .metrics("{\"win_rate\": 54.0, \"max_drawdown\": 12.5, \"sharpe_ratio\": 1.45}")
            .build());

        listingRepository.save(Listing.builder()
            .botVersion(vB)
            .price(new BigDecimal("249.00"))
            .licenseType(LicenseType.ONE_TIME)
            .active(true)
            .build());

        // 3. Seed Draft Bot C (Unpublished - must NOT appear)
        Bot botC = botRepository.save(Bot.builder()
            .seller(seller)
            .name("Draft Strategy C")
            .description("Internal test bot")
            .strategyType("MOMENTUM")
            .status(BotStatus.DRAFT)
            .build());

        BotVersion vC = botVersionRepository.save(BotVersion.builder()
            .bot(botC)
            .versionNumber("1.0.0")
            .disclosedLogic("Test logic")
            .build());

        listingRepository.save(Listing.builder()
            .botVersion(vC)
            .price(new BigDecimal("10.00"))
            .licenseType(LicenseType.ONE_TIME)
            .active(false)
            .build());

        // Test 1: Fetch all public listings without auth header
        mockMvc.perform(get("/api/listings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.content[0].sellerDisplayName").value("Alpha Quant Capital"));

        // Test 2: Filter by strategyType=MOMENTUM
        mockMvc.perform(get("/api/listings").param("strategyType", "MOMENTUM"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].name").value("Nifty Momentum Alpha"));

        // Test 3: Search query q=Scalper
        mockMvc.perform(get("/api/listings").param("q", "Scalper"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].name").value("BankNifty Scalper Pro"));

        // Test 4: Detail endpoint for Listing A — verifies full disclosed logic & disclaimer, hides fileStorageKey
        mockMvc.perform(get("/api/listings/" + listingA.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Nifty Momentum Alpha"))
            .andExpect(jsonPath("$.disclosedLogic").value("EMA 20/50 Crossover"))
            .andExpect(jsonPath("$.riskDisclaimer").value("Capital at risk."))
            .andExpect(jsonPath("$.sellerBio").value("Institutional-grade algorithmic trading systems."))
            .andExpect(jsonPath("$.fileStorageKey").doesNotExist());
    }

    @Test
    @DisplayName("Public Seller Profile: Returns public seller info and active listings without PII")
    void testPublicSellerProfileEndpoint() throws Exception {
        Bot bot = botRepository.save(Bot.builder()
            .seller(seller)
            .name("Alpha Mean Reversion")
            .description("Mean reversion strategy")
            .strategyType("MEAN_REVERSION")
            .status(BotStatus.PUBLISHED)
            .build());

        BotVersion version = botVersionRepository.save(BotVersion.builder()
            .bot(bot)
            .versionNumber("1.0.0")
            .disclosedLogic("RSI 14 mean reversion")
            .backtestStatus(BacktestStatus.COMPLETED)
            .build());

        listingRepository.save(Listing.builder()
            .botVersion(version)
            .price(new BigDecimal("129.99"))
            .licenseType(LicenseType.ONE_TIME)
            .active(true)
            .build());

        mockMvc.perform(get("/api/sellers/" + seller.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.displayName").value("Alpha Quant Capital"))
            .andExpect(jsonPath("$.bio").value("Institutional-grade algorithmic trading systems."))
            .andExpect(jsonPath("$.activeListings[0].name").value("Alpha Mean Reversion"))
            .andExpect(jsonPath("$.email").doesNotExist())
            .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }
}
