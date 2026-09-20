package com.algoadda.core.favorite;

import com.algoadda.core.bot.*;
import com.algoadda.core.compliance.ComplianceCheckRepository;
import com.algoadda.core.config.JwtTokenProvider;
import com.algoadda.core.listing.LicenseType;
import com.algoadda.core.listing.Listing;
import com.algoadda.core.listing.ListingRepository;
import com.algoadda.core.order.LicenseRepository;
import com.algoadda.core.review.ReviewRepository;
import com.algoadda.core.user.RefreshTokenRepository;
import com.algoadda.core.user.Role;
import com.algoadda.core.user.SellerProfileRepository;
import com.algoadda.core.user.User;
import com.algoadda.core.user.UserRepository;
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
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("null")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FavoriteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BotRepository botRepository;

    @Autowired
    private BotVersionRepository botVersionRepository;

    @Autowired
    private BacktestResultRepository backtestResultRepository;

    @Autowired
    private BacktestJobRepository backtestJobRepository;

    @Autowired
    private ComplianceCheckRepository complianceCheckRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private SellerProfileRepository sellerProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User seller;
    private User buyerA;
    private User buyerB;

    private String buyerAToken;
    private String buyerBToken;
    private String sellerToken;

    private Bot publishedBot;
    private Bot draftBot;
    private BotVersion publishedBotVersion;
    private Listing listing;

    @BeforeEach
    void setUp() {
        favoriteRepository.deleteAll();
        reviewRepository.deleteAll();
        licenseRepository.deleteAll();
        listingRepository.deleteAll();
        complianceCheckRepository.deleteAll();
        backtestJobRepository.deleteAll();
        backtestResultRepository.deleteAll();
        botVersionRepository.deleteAll();
        botRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        sellerProfileRepository.deleteAll();
        userRepository.deleteAll();

        seller = userRepository.save(User.builder()
            .email("fav_seller@algoadda.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .role(Role.SELLER)
            .build());

        buyerA = userRepository.save(User.builder()
            .email("buyer_a@algoadda.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .role(Role.BUYER)
            .build());

        buyerB = userRepository.save(User.builder()
            .email("buyer_b@algoadda.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .role(Role.BUYER)
            .build());

        buyerAToken = "Bearer " + jwtTokenProvider.generateAccessToken(buyerA);
        buyerBToken = "Bearer " + jwtTokenProvider.generateAccessToken(buyerB);
        sellerToken = "Bearer " + jwtTokenProvider.generateAccessToken(seller);

        publishedBot = botRepository.save(Bot.builder()
            .seller(seller)
            .name("Alpha Momentum Trend")
            .description("High Sharpe ratio momentum algorithm")
            .strategyType("MOMENTUM")
            .status(BotStatus.PUBLISHED)
            .build());

        draftBot = botRepository.save(Bot.builder()
            .seller(seller)
            .name("Experimental Grid Bot")
            .description("Work in progress draft")
            .strategyType("GRID")
            .status(BotStatus.DRAFT)
            .build());

        publishedBotVersion = botVersionRepository.save(BotVersion.builder()
            .bot(publishedBot)
            .versionNumber("1.0.0")
            .disclosedLogic("EMA 20 crossover")
            .fileStorageKey("bots/test/strategy.py")
            .build());

        listing = listingRepository.save(Listing.builder()
            .botVersion(publishedBotVersion)
            .price(BigDecimal.valueOf(2500.00))
            .licenseType(LicenseType.ONE_TIME)
            .active(true)
            .official(false)
            .build());
    }

    @Test
    @DisplayName("Favorite Creation & Idempotency: Favoriting twice succeeds without error or duplicate rows")
    void testAddFavoriteSuccessAndIdempotency() throws Exception {
        // First favorite request -> 200 OK
        mockMvc.perform(post("/api/bots/" + publishedBot.getId() + "/favorite")
                .header("Authorization", buyerAToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.favorited").value(true));

        assertEquals(1, favoriteRepository.count());

        // Second favorite request (same buyer & bot) -> 200 OK (idempotent no-op)
        mockMvc.perform(post("/api/bots/" + publishedBot.getId() + "/favorite")
                .header("Authorization", buyerAToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.favorited").value(true));

        assertEquals(1, favoriteRepository.count());
    }

    @Test
    @DisplayName("Favorite Removal & Idempotency: Unfavoriting non-favorited bot succeeds without error")
    void testRemoveFavoriteSuccessAndIdempotency() throws Exception {
        // Favorite first
        mockMvc.perform(post("/api/bots/" + publishedBot.getId() + "/favorite")
                .header("Authorization", buyerAToken))
            .andExpect(status().isOk());

        assertEquals(1, favoriteRepository.count());

        // Remove favorite -> 200 OK
        mockMvc.perform(delete("/api/bots/" + publishedBot.getId() + "/favorite")
                .header("Authorization", buyerAToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.favorited").value(false));

        assertEquals(0, favoriteRepository.count());

        // Remove again (already deleted / non-favorited) -> 200 OK (idempotent no-op)
        mockMvc.perform(delete("/api/bots/" + publishedBot.getId() + "/favorite")
                .header("Authorization", buyerAToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.favorited").value(false));

        assertEquals(0, favoriteRepository.count());
    }

    @Test
    @DisplayName("Per-Buyer Isolation: Buyer A favoriting does NOT affect Buyer B")
    void testPerBuyerIsolation() throws Exception {
        // Buyer A favorites the published bot
        mockMvc.perform(post("/api/bots/" + publishedBot.getId() + "/favorite")
                .header("Authorization", buyerAToken))
            .andExpect(status().isOk());

        // Buyer A sees bot in favorites
        mockMvc.perform(get("/api/buyers/me/favorites")
                .header("Authorization", buyerAToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].botId").value(publishedBot.getId().toString()))
            .andExpect(jsonPath("$[0].isFavorited").value(true));

        // Buyer B sees empty favorites
        mockMvc.perform(get("/api/buyers/me/favorites")
                .header("Authorization", buyerBToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));

        // Marketplace listing endpoint reflects isFavorited for Buyer A
        mockMvc.perform(get("/api/listings")
                .header("Authorization", buyerAToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].isFavorited").value(true));

        // Marketplace listing endpoint reflects isFavorited=false for Buyer B
        mockMvc.perform(get("/api/listings")
                .header("Authorization", buyerBToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].isFavorited").value(false));

        // Unauthenticated request reflects isFavorited=false
        mockMvc.perform(get("/api/listings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].isFavorited").value(false));

        // Listing detail endpoint reflects isFavorited for Buyer A
        mockMvc.perform(get("/api/listings/" + listing.getId())
                .header("Authorization", buyerAToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isFavorited").value(true));

        // Listing detail endpoint reflects isFavorited=false for Buyer B
        mockMvc.perform(get("/api/listings/" + listing.getId())
                .header("Authorization", buyerBToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isFavorited").value(false));
    }

    @Test
    @DisplayName("Validation: Draft/Unpublished bot or non-existent bot cannot be favorited")
    void testDraftOrInvalidBotCannotBeFavorited() throws Exception {
        // Attempt to favorite DRAFT bot -> 400 Bad Request
        mockMvc.perform(post("/api/bots/" + draftBot.getId() + "/favorite")
                .header("Authorization", buyerAToken))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("published")));

        // Attempt to favorite non-existent bot -> 404/400
        mockMvc.perform(post("/api/bots/" + UUID.randomUUID() + "/favorite")
                .header("Authorization", buyerAToken))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Role Guard: Only BUYER role can manage favorites")
    void testOnlyBuyerCanManageFavorites() throws Exception {
        // Seller cannot favorite
        mockMvc.perform(post("/api/bots/" + publishedBot.getId() + "/favorite")
                .header("Authorization", sellerToken))
            .andExpect(status().isForbidden());

        // Seller cannot access watchlist
        mockMvc.perform(get("/api/buyers/me/favorites")
                .header("Authorization", sellerToken))
            .andExpect(status().isForbidden());

        // Anonymous user cannot favorite
        mockMvc.perform(post("/api/bots/" + publishedBot.getId() + "/favorite"))
            .andExpect(status().isUnauthorized());
    }
}
