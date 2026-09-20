package com.algoadda.core.review;

import com.algoadda.core.bot.Bot;
import com.algoadda.core.bot.BotRepository;
import com.algoadda.core.bot.BotStatus;
import com.algoadda.core.bot.BotVersion;
import com.algoadda.core.bot.BotVersionRepository;
import com.algoadda.core.config.JwtTokenProvider;
import com.algoadda.core.listing.LicenseType;
import com.algoadda.core.listing.Listing;
import com.algoadda.core.listing.ListingRepository;
import com.algoadda.core.order.License;
import com.algoadda.core.order.LicenseRepository;
import com.algoadda.core.review.dto.ReviewRequest;
import com.algoadda.core.user.RefreshTokenRepository;
import com.algoadda.core.user.Role;
import com.algoadda.core.user.SellerProfileRepository;
import com.algoadda.core.user.User;
import com.algoadda.core.user.UserRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("null")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReviewIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BotRepository botRepository;

    @Autowired
    private BotVersionRepository botVersionRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private SellerProfileRepository sellerProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User seller;
    private User buyer1;
    private User buyer2;
    private User unverifiedBuyer;
    private User admin;

    private String buyer1Token;
    private String buyer2Token;
    private String unverifiedBuyerToken;
    private String adminToken;

    private Bot bot;
    private BotVersion botVersion;
    private Listing listing;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        licenseRepository.deleteAll();
        listingRepository.deleteAll();
        botVersionRepository.deleteAll();
        botRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        sellerProfileRepository.deleteAll();
        userRepository.deleteAll();

        seller = userRepository.save(User.builder()
            .email("quant_seller@algoadda.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .role(Role.SELLER)
            .build());

        buyer1 = userRepository.save(User.builder()
            .email("verified_buyer1@algoadda.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .role(Role.BUYER)
            .build());

        buyer2 = userRepository.save(User.builder()
            .email("verified_buyer2@algoadda.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .role(Role.BUYER)
            .build());

        unverifiedBuyer = userRepository.save(User.builder()
            .email("unverified@algoadda.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .role(Role.BUYER)
            .build());

        admin = userRepository.save(User.builder()
            .email("admin@algoadda.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .role(Role.ADMIN)
            .build());

        buyer1Token = "Bearer " + jwtTokenProvider.generateAccessToken(buyer1);
        buyer2Token = "Bearer " + jwtTokenProvider.generateAccessToken(buyer2);
        unverifiedBuyerToken = "Bearer " + jwtTokenProvider.generateAccessToken(unverifiedBuyer);
        adminToken = "Bearer " + jwtTokenProvider.generateAccessToken(admin);

        bot = botRepository.save(Bot.builder()
            .seller(seller)
            .name("Alpha Momentum Trend")
            .description("A high Sharpe ratio trend following algorithm")
            .strategyType("MOMENTUM")
            .status(BotStatus.PUBLISHED)
            .build());

        botVersion = botVersionRepository.save(BotVersion.builder()
            .bot(bot)
            .versionNumber("1.0.0")
            .disclosedLogic("EMA 20 crossover")
            .fileStorageKey("bots/test/strategy.py")
            .build());

        listing = listingRepository.save(Listing.builder()
            .botVersion(botVersion)
            .price(BigDecimal.valueOf(2500.00))
            .licenseType(LicenseType.ONE_TIME)
            .active(true)
            .official(false)
            .build());

        // Grant active licenses to buyer1 and buyer2, but NOT unverifiedBuyer
        licenseRepository.save(License.builder()
            .buyer(buyer1)
            .botVersion(botVersion)
            .revoked(false)
            .build());

        licenseRepository.save(License.builder()
            .buyer(buyer2)
            .botVersion(botVersion)
            .revoked(false)
            .build());
    }

    @Test
    @DisplayName("Verified Buyer Gate: Non-license owner cannot submit a review")
    void testUnverifiedBuyerCannotSubmitReview() throws Exception {
        ReviewRequest request = new ReviewRequest(5, "Great strategy!");

        mockMvc.perform(post("/api/bots/" + bot.getId() + "/reviews")
                .header("Authorization", unverifiedBuyerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("verified buyers")));
    }

    @Test
    @DisplayName("Review Creation: Verified buyer can successfully submit a review")
    void testVerifiedBuyerCanSubmitReview() throws Exception {
        ReviewRequest request = new ReviewRequest(5, "Solid Sharpe ratio and smooth returns in live testing.");

        mockMvc.perform(post("/api/bots/" + bot.getId() + "/reviews")
                .header("Authorization", buyer1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.botId").value(bot.getId().toString()))
            .andExpect(jsonPath("$.buyerId").value(buyer1.getId().toString()))
            .andExpect(jsonPath("$.rating").value(5))
            .andExpect(jsonPath("$.comment").value("Solid Sharpe ratio and smooth returns in live testing."))
            .andExpect(jsonPath("$.buyerDisplayName", containsString("Verified Buyer")));
    }

    @Test
    @DisplayName("Single Review per Buyer: Resubmitting updates existing review without creating duplicate")
    void testResubmittingReviewUpdatesExisting() throws Exception {
        ReviewRequest initialRequest = new ReviewRequest(4, "Initial review");

        mockMvc.perform(post("/api/bots/" + bot.getId() + "/reviews")
                .header("Authorization", buyer1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(initialRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rating").value(4));

        ReviewRequest updatedRequest = new ReviewRequest(5, "Updated to 5 stars after another week of profits!");

        mockMvc.perform(post("/api/bots/" + bot.getId() + "/reviews")
                .header("Authorization", buyer1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rating").value(5))
            .andExpect(jsonPath("$.comment").value("Updated to 5 stars after another week of profits!"));

        // Check only 1 review row exists in database
        mockMvc.perform(get("/api/bots/" + bot.getId() + "/reviews"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reviewCount").value(1))
            .andExpect(jsonPath("$.averageRating").value(5.0))
            .andExpect(jsonPath("$.reviews.length()").value(1));
    }

    @Test
    @DisplayName("Review Aggregation: Multiple buyer reviews correctly compute average and count")
    void testMultipleReviewsComputeCorrectAverage() throws Exception {
        // Buyer 1 submits rating 5
        mockMvc.perform(post("/api/bots/" + bot.getId() + "/reviews")
                .header("Authorization", buyer1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ReviewRequest(5, "Excellent"))))
            .andExpect(status().isOk());

        // Buyer 2 submits rating 4
        mockMvc.perform(post("/api/bots/" + bot.getId() + "/reviews")
                .header("Authorization", buyer2Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ReviewRequest(4, "Good"))))
            .andExpect(status().isOk());

        // GET reviews summary
        mockMvc.perform(get("/api/bots/" + bot.getId() + "/reviews")
                .header("Authorization", buyer1Token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.averageRating").value(4.5))
            .andExpect(jsonPath("$.reviewCount").value(2))
            .andExpect(jsonPath("$.reviews.length()").value(2))
            .andExpect(jsonPath("$.userReview").exists())
            .andExpect(jsonPath("$.userReview.rating").value(5))
            .andExpect(jsonPath("$.isVerifiedBuyer").value(true));
    }

    @Test
    @DisplayName("Admin Moderation: Admin can delete abusive review and average updates immediately")
    void testAdminCanDeleteReview() throws Exception {
        // Buyer 1 submits 5 star, Buyer 2 submits 1 star (abusive)
        mockMvc.perform(post("/api/bots/" + bot.getId() + "/reviews")
                .header("Authorization", buyer1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ReviewRequest(5, "Great"))))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/bots/" + bot.getId() + "/reviews")
                .header("Authorization", buyer2Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ReviewRequest(1, "Abusive spam"))))
            .andExpect(status().isOk());

        Review abusiveReview = reviewRepository.findByBotIdAndBuyerId(bot.getId(), buyer2.getId()).orElseThrow();

        // Non-admin cannot delete
        mockMvc.perform(delete("/api/admin/reviews/" + abusiveReview.getId())
                .header("Authorization", buyer1Token))
            .andExpect(status().isForbidden());

        // Admin deletes review
        mockMvc.perform(delete("/api/admin/reviews/" + abusiveReview.getId())
                .header("Authorization", adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        // Average recalculates to 5.0 and count is 1
        mockMvc.perform(get("/api/bots/" + bot.getId() + "/reviews"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.averageRating").value(5.0))
            .andExpect(jsonPath("$.reviewCount").value(1));
    }

    @Test
    @DisplayName("Listing Integration: Marketplace and detail endpoints return averageRating and reviewCount")
    void testListingEndpointsIncludeRatingSummary() throws Exception {
        // Submit review for bot
        mockMvc.perform(post("/api/bots/" + bot.getId() + "/reviews")
                .header("Authorization", buyer1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ReviewRequest(5, "Exceptional"))))
            .andExpect(status().isOk());

        // Marketplace listings summary
        mockMvc.perform(get("/api/listings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].averageRating").value(5.0))
            .andExpect(jsonPath("$.content[0].reviewCount").value(1));

        // Listing detail
        mockMvc.perform(get("/api/listings/" + listing.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.averageRating").value(5.0))
            .andExpect(jsonPath("$.reviewCount").value(1));
    }
}
