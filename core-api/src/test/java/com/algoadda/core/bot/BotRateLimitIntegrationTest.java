package com.algoadda.core.bot;

import com.algoadda.core.bot.service.BacktestServiceClient;
import com.algoadda.core.bot.service.S3StorageService;
import com.algoadda.core.config.JwtTokenProvider;
import com.algoadda.core.ratelimit.RateLimiterService;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("null")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BotRateLimitIntegrationTest {

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
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private SellerProfileRepository sellerProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RateLimiterService rateLimiterService;

    @MockBean
    private S3StorageService s3StorageService;

    @MockBean
    private BacktestServiceClient backtestServiceClient;

    private User sellerA;
    private User sellerB;
    private String sellerAToken;
    private String sellerBToken;

    @BeforeEach
    void setUp() {
        rateLimiterService.resetAll();
        backtestJobRepository.deleteAll();
        backtestResultRepository.deleteAll();
        botVersionRepository.deleteAll();
        botRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        sellerProfileRepository.deleteAll();
        userRepository.deleteAll();

        sellerA = userRepository.save(User.builder()
            .email("seller_a@algoadda.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .role(Role.SELLER)
            .build());

        sellerB = userRepository.save(User.builder()
            .email("seller_b@algoadda.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .role(Role.SELLER)
            .build());

        sellerAToken = "Bearer " + jwtTokenProvider.generateAccessToken(sellerA);
        sellerBToken = "Bearer " + jwtTokenProvider.generateAccessToken(sellerB);

        when(s3StorageService.uploadFile(anyString(), any(byte[].class), anyString())).thenReturn("mock-s3-key");
        when(backtestServiceClient.runBacktest(any(BotVersion.class), any(), any(), any())).thenReturn(true);
    }

    private MockMultipartFile createSampleFile(String filename) {
        return new MockMultipartFile(
            "file",
            filename,
            "text/x-python",
            "import numpy as np\nprint('Strategy Code')".getBytes(StandardCharsets.UTF_8)
        );
    }

    @Test
    @DisplayName("Rate Limiting: 10 uploads succeed, 11th request returns 429 Too Many Requests with Retry-After header")
    void testUploadRateLimitEnforcedOn11thRequest() throws Exception {
        // First 10 uploads must succeed
        for (int i = 1; i <= 10; i++) {
            mockMvc.perform(multipart("/api/bots")
                    .file(createSampleFile("strat_" + i + ".py"))
                    .header("Authorization", sellerAToken)
                    .param("name", "Bot Version " + i)
                    .param("strategyType", "MOMENTUM")
                    .param("disclosedLogic", "Logic for bot " + i))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bot Version " + i));
        }

        // 11th upload must be rejected with 429 Too Many Requests
        mockMvc.perform(multipart("/api/bots")
                .file(createSampleFile("strat_11.py"))
                .header("Authorization", sellerAToken)
                .param("name", "Bot Version 11 - Exceeded")
                .param("strategyType", "MOMENTUM")
                .param("disclosedLogic", "Logic for bot 11"))
            .andExpect(status().isTooManyRequests())
            .andExpect(header().exists("Retry-After"))
            .andExpect(jsonPath("$.error").value("Too Many Requests"))
            .andExpect(jsonPath("$.retryAfterSeconds").value(greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("Rate Limiting: Per-seller isolation - Seller B is not blocked when Seller A hits rate limit")
    void testRateLimitIsolationBetweenSellers() throws Exception {
        // Exhaust Seller A's quota
        for (int i = 1; i <= 10; i++) {
            mockMvc.perform(multipart("/api/bots")
                    .file(createSampleFile("strat_a_" + i + ".py"))
                    .header("Authorization", sellerAToken)
                    .param("name", "Seller A Bot " + i)
                    .param("strategyType", "MOMENTUM")
                    .param("disclosedLogic", "Logic " + i))
                .andExpect(status().isOk());
        }

        // Confirm Seller A is blocked on 11th attempt
        mockMvc.perform(multipart("/api/bots")
                .file(createSampleFile("strat_a_11.py"))
                .header("Authorization", sellerAToken)
                .param("name", "Seller A Bot 11")
                .param("strategyType", "MOMENTUM")
                .param("disclosedLogic", "Logic 11"))
            .andExpect(status().isTooManyRequests());

        // Seller B should still succeed without any rate limiting interference
        mockMvc.perform(multipart("/api/bots")
                .file(createSampleFile("strat_b_1.py"))
                .header("Authorization", sellerBToken)
                .param("name", "Seller B First Bot")
                .param("strategyType", "GRID")
                .param("disclosedLogic", "Grid strategy logic"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Seller B First Bot"));
    }

    @Test
    @DisplayName("Rate Limiting: Non-upload endpoints are unaffected when seller is rate-limited")
    void testOtherEndpointsUnaffectedByUploadRateLimit() throws Exception {
        // Create 10 bots to consume rate limit tokens
        for (int i = 1; i <= 10; i++) {
            mockMvc.perform(multipart("/api/bots")
                    .file(createSampleFile("strat_" + i + ".py"))
                    .header("Authorization", sellerAToken)
                    .param("name", "Bot " + i)
                    .param("strategyType", "MOMENTUM")
                    .param("disclosedLogic", "Logic " + i))
                .andExpect(status().isOk());
        }

        // 11th upload is rate limited
        mockMvc.perform(multipart("/api/bots")
                .file(createSampleFile("strat_11.py"))
                .header("Authorization", sellerAToken)
                .param("name", "Bot 11")
                .param("strategyType", "MOMENTUM")
                .param("disclosedLogic", "Logic 11"))
            .andExpect(status().isTooManyRequests());

        // Seller can still view seller dashboard
        mockMvc.perform(get("/api/sellers/me/bots")
                .header("Authorization", sellerAToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", is(10)));
    }

    @Test
    @DisplayName("Rate Limiting: Version upload endpoint POST /api/bots/{botId}/versions is also rate limited")
    void testVersionUploadIsRateLimited() throws Exception {
        // First create a bot
        mockMvc.perform(multipart("/api/bots")
                .file(createSampleFile("strat_initial.py"))
                .header("Authorization", sellerAToken)
                .param("name", "Target Bot for Versions")
                .param("strategyType", "MOMENTUM")
                .param("disclosedLogic", "Initial logic"))
            .andExpect(status().isOk());

        Bot bot = botRepository.findAll().get(0);

        // Upload 9 more versions to hit the 10 limit (1 bot upload + 9 version uploads = 10)
        for (int i = 1; i <= 9; i++) {
            mockMvc.perform(multipart("/api/bots/" + bot.getId() + "/versions")
                    .file(createSampleFile("strat_v_" + i + ".py"))
                    .header("Authorization", sellerAToken)
                    .param("disclosedLogic", "Updated logic " + i)
                    .param("changelog", "Update v" + i))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versionNumber").value("1." + i + ".0"));
        }

        // 11th upload (next version upload) must be rejected with 429
        mockMvc.perform(multipart("/api/bots/" + bot.getId() + "/versions")
                .file(createSampleFile("strat_v_exceeded.py"))
                .header("Authorization", sellerAToken)
                .param("disclosedLogic", "Exceeded logic")
                .param("changelog", "Update exceeded"))
            .andExpect(status().isTooManyRequests())
            .andExpect(header().exists("Retry-After"))
            .andExpect(jsonPath("$.error").value("Too Many Requests"));
    }
}
