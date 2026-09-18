package com.algoadda.core.listing;

import com.algoadda.core.bot.*;
import com.algoadda.core.compliance.ComplianceCheckRepository;
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

import static org.hamcrest.Matchers.*;
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
    private com.algoadda.core.bot.BacktestJobRepository backtestJobRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User seller;
    private Listing listingMomentum;
    private Listing listingDraft;

    @BeforeEach
    void setUp() {
        listingRepository.deleteAll();
        complianceCheckRepository.deleteAll();
        backtestJobRepository.deleteAll();
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

        // 1. Seed Bot 1: Momentum ($99.99, Win rate: 68.5%)
        Bot bot1 = botRepository.save(Bot.builder()
            .seller(seller)
            .name("Nifty Momentum Alpha")
            .description("High frequency momentum strategy on Nifty 50 futures")
            .riskDisclaimer("Capital at risk.")
            .strategyType("MOMENTUM")
            .status(BotStatus.PUBLISHED)
            .build());
        BotVersion v1 = botVersionRepository.save(BotVersion.builder()
            .bot(bot1)
            .versionNumber("1.0.0")
            .disclosedLogic("EMA 20/50 Crossover with RSI filter")
            .fileStorageKey("bots/secret/strategy_a.py")
            .backtestStatus(BacktestStatus.COMPLETED)
            .build());
        backtestResultRepository.save(BacktestResult.builder()
            .botVersion(v1)
            .metrics("{\"win_rate\": 68.5, \"max_drawdown\": 6.2, \"sharpe_ratio\": 2.10}")
            .createdAt(java.time.Instant.now())
            .build());
        listingMomentum = listingRepository.save(Listing.builder()
            .botVersion(v1)
            .price(new BigDecimal("99.99"))
            .licenseType(LicenseType.ONE_TIME)
            .active(true)
            .build());

        // 2. Seed Bot 2: Scalping ($249.00, Win rate: 54.0%)
        Bot bot2 = botRepository.save(Bot.builder()
            .seller(seller)
            .name("BankNifty Scalper Pro")
            .description("Intraday scalping strategy on Bank Nifty index")
            .riskDisclaimer("Substantial risk of loss.")
            .strategyType("SCALPING")
            .status(BotStatus.PUBLISHED)
            .build());
        BotVersion v2 = botVersionRepository.save(BotVersion.builder()
            .bot(bot2)
            .versionNumber("1.0.0")
            .disclosedLogic("Bollinger Bands Mean Reversion")
            .fileStorageKey("bots/secret/strategy_b.py")
            .backtestStatus(BacktestStatus.COMPLETED)
            .build());
        backtestResultRepository.save(BacktestResult.builder()
            .botVersion(v2)
            .metrics("{\"win_rate\": 54.0, \"max_drawdown\": 12.5, \"sharpe_ratio\": 1.45}")
            .createdAt(java.time.Instant.now())
            .build());
        listingRepository.save(Listing.builder()
            .botVersion(v2)
            .price(new BigDecimal("249.00"))
            .licenseType(LicenseType.ONE_TIME)
            .active(true)
            .build());

        // 3. Seed Bot 3: Arbitrage ($149.50, Win rate: 81.2%)
        Bot bot3 = botRepository.save(Bot.builder()
            .seller(seller)
            .name("Options Delta Neutral")
            .description("Statistical arbitrage and options delta neutral strategy")
            .riskDisclaimer("Options trading carries substantial risk.")
            .strategyType("ARBITRAGE")
            .status(BotStatus.PUBLISHED)
            .build());
        BotVersion v3 = botVersionRepository.save(BotVersion.builder()
            .bot(bot3)
            .versionNumber("1.0.0")
            .disclosedLogic("Delta Neutral Straddle Hedging")
            .fileStorageKey("bots/secret/strategy_c.py")
            .backtestStatus(BacktestStatus.COMPLETED)
            .build());
        backtestResultRepository.save(BacktestResult.builder()
            .botVersion(v3)
            .metrics("{\"win_rate\": 81.2, \"max_drawdown\": 3.8, \"sharpe_ratio\": 3.05}")
            .createdAt(java.time.Instant.now())
            .build());
        listingRepository.save(Listing.builder()
            .botVersion(v3)
            .price(new BigDecimal("149.50"))
            .licenseType(LicenseType.ONE_TIME)
            .active(true)
            .build());

        // 4. Seed Bot 4: Trend ($49.99, Win rate: 45.0%)
        Bot bot4 = botRepository.save(Bot.builder()
            .seller(seller)
            .name("Trend Following Master")
            .description("Multi-asset trend follower for positional traders")
            .riskDisclaimer("Past performance does not guarantee future results.")
            .strategyType("TREND")
            .status(BotStatus.PUBLISHED)
            .build());
        BotVersion v4 = botVersionRepository.save(BotVersion.builder()
            .bot(bot4)
            .versionNumber("1.0.0")
            .disclosedLogic("Donchian Channel Breakout")
            .fileStorageKey("bots/secret/strategy_d.py")
            .backtestStatus(BacktestStatus.COMPLETED)
            .build());
        backtestResultRepository.save(BacktestResult.builder()
            .botVersion(v4)
            .metrics("{\"win_rate\": 45.0, \"max_drawdown\": 18.0, \"sharpe_ratio\": 1.10}")
            .createdAt(java.time.Instant.now())
            .build());
        listingRepository.save(Listing.builder()
            .botVersion(v4)
            .price(new BigDecimal("49.99"))
            .licenseType(LicenseType.ONE_TIME)
            .active(true)
            .build());

        // 5. Seed Bot 5: DRAFT (Unpublished - MUST be excluded from all public endpoints)
        Bot bot5 = botRepository.save(Bot.builder()
            .seller(seller)
            .name("Draft Experimental Bot")
            .description("Unpublished internal strategy in development")
            .strategyType("MOMENTUM")
            .status(BotStatus.DRAFT)
            .build());
        BotVersion v5 = botVersionRepository.save(BotVersion.builder()
            .bot(bot5)
            .versionNumber("0.1.0")
            .disclosedLogic("Draft logic")
            .build());
        listingDraft = listingRepository.save(Listing.builder()
            .botVersion(v5)
            .price(new BigDecimal("10.00"))
            .licenseType(LicenseType.ONE_TIME)
            .active(false)
            .build());
    }

    @Test
    @DisplayName("1. Public Listings List: Excludes draft bots, omits disclosedLogic & file reference")
    void testPublicListingsListEndpoint() throws Exception {
        mockMvc.perform(get("/api/listings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(4))
            .andExpect(jsonPath("$.content.length()").value(4))
            .andExpect(jsonPath("$.content[*].name", not(hasItem("Draft Experimental Bot"))))
            .andExpect(jsonPath("$.content[0].disclosedLogic").doesNotExist())
            .andExpect(jsonPath("$.content[0].fileStorageKey").doesNotExist());
    }

    @Test
    @DisplayName("1b. Public Listings Filters & Sorting: strategyType, minPrice/maxPrice, sortBy")
    void testFiltersAndSorting() throws Exception {
        // StrategyType filter
        mockMvc.perform(get("/api/listings").param("strategyType", "SCALPING"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].name").value("BankNifty Scalper Pro"));

        // MinPrice & MaxPrice filter ($100 to $200)
        mockMvc.perform(get("/api/listings")
                .param("minPrice", "100.00")
                .param("maxPrice", "200.00"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].name").value("Options Delta Neutral"));

        // Sort by price (asc)
        mockMvc.perform(get("/api/listings").param("sortBy", "price"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].price").value(49.99))
            .andExpect(jsonPath("$.content[3].price").value(249.00));

        // Sort by winRate (desc)
        mockMvc.perform(get("/api/listings").param("sortBy", "winRate"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("Options Delta Neutral"))
            .andExpect(jsonPath("$.content[0].winRate").value(81.2));
    }

    @Test
    @DisplayName("1c. Public Listings Pagination: page 1 vs page 2 return different items")
    void testPagination() throws Exception {
        // Page 0 (size 2)
        mockMvc.perform(get("/api/listings").param("page", "0").param("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(4))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.content.length()").value(2));

        // Page 1 (size 2)
        mockMvc.perform(get("/api/listings").param("page", "1").param("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(4))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("2. Search: term match, nonsense empty result, combined query + filter")
    void testSearchEndpoint() throws Exception {
        // Search term match
        mockMvc.perform(get("/api/listings").param("q", "Scalper"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].name").value("BankNifty Scalper Pro"));

        // Nonsense search
        mockMvc.perform(get("/api/listings").param("q", "xyz999nonsense"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(0))
            .andExpect(jsonPath("$.content").isEmpty());

        // Combined q + strategyType
        mockMvc.perform(get("/api/listings")
                .param("q", "Alpha")
                .param("strategyType", "MOMENTUM"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].name").value("Nifty Momentum Alpha"));
    }

    @Test
    @DisplayName("3. Listing Detail: Published includes disclosedLogic/sellerInfo, Unpublished is blocked")
    void testListingDetailEndpoint() throws Exception {
        // Published detail
        mockMvc.perform(get("/api/listings/" + listingMomentum.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Nifty Momentum Alpha"))
            .andExpect(jsonPath("$.disclosedLogic").value("EMA 20/50 Crossover with RSI filter"))
            .andExpect(jsonPath("$.riskDisclaimer").value("Capital at risk."))
            .andExpect(jsonPath("$.sellerDisplayName").value("Alpha Quant Capital"))
            .andExpect(jsonPath("$.sellerBio").value("Institutional-grade algorithmic trading systems."))
            .andExpect(jsonPath("$.fileStorageKey").doesNotExist())
            .andExpect(jsonPath("$.email").doesNotExist());

        // Unpublished / draft listing ID
        mockMvc.perform(get("/api/listings/" + listingDraft.getId()))
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("4. Seller Profile: Public fields only, active listings linkable, unpublished excluded")
    void testSellerProfileEndpoint() throws Exception {
        mockMvc.perform(get("/api/sellers/" + seller.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.displayName").value("Alpha Quant Capital"))
            .andExpect(jsonPath("$.bio").value("Institutional-grade algorithmic trading systems."))
            .andExpect(jsonPath("$.activeListings.length()").value(4))
            .andExpect(jsonPath("$.activeListings[*].name", not(hasItem("Draft Experimental Bot"))))
            .andExpect(jsonPath("$.email").doesNotExist())
            .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }
}

