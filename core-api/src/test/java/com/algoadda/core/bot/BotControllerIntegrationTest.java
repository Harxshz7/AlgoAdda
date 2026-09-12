package com.algoadda.core.bot;

import com.algoadda.core.bot.service.BacktestServiceClient;
import com.algoadda.core.bot.service.S3StorageService;
import com.algoadda.core.config.JwtTokenProvider;
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
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("null")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BotControllerIntegrationTest {

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
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private SellerProfileRepository sellerProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private S3StorageService s3StorageService;

    @MockBean
    private BacktestServiceClient backtestServiceClient;

    private String sellerToken;
    private String buyerToken;
    private User seller;

    @BeforeEach
    void setUp() {
        backtestResultRepository.deleteAll();
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

        User buyer = userRepository.save(User.builder()
            .email("retail_buyer@algoadda.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .role(Role.BUYER)
            .build());

        sellerToken = "Bearer " + jwtTokenProvider.generateAccessToken(seller);
        buyerToken = "Bearer " + jwtTokenProvider.generateAccessToken(buyer);

        when(s3StorageService.uploadFile(anyString(), any(byte[].class), anyString())).thenReturn("mock-s3-key");
        when(backtestServiceClient.runBacktest(any(BotVersion.class), any(), any(), any())).thenReturn(true);
    }

    @Test
    @DisplayName("Upload Flow: Seller uploads bot, creates DRAFT status and initial version")
    void testSellerUploadBotSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "momentum.py",
            "text/x-python",
            "import numpy as np\nprint('Momentum strategy')".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/bots")
                .file(file)
                .header("Authorization", sellerToken)
                .param("name", "Nifty Momentum v1")
                .param("description", "High frequency momentum on NIFTY index")
                .param("strategyType", "MOMENTUM")
                .param("disclosedLogic", "Long when 20 EMA > 50 EMA with ATR stop")
                .param("strategyConfig", "{\"symbol\":\"^NSEI\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Nifty Momentum v1"))
            .andExpect(jsonPath("$.status").value("DRAFT"))
            .andExpect(jsonPath("$.latestVersion.versionNumber").value("1.0.0"))
            .andExpect(jsonPath("$.latestVersion.disclosedLogic").value("Long when 20 EMA > 50 EMA with ATR stop"));
    }

    @Test
    @DisplayName("RBAC: Buyer cannot upload bots")
    void testBuyerCannotUploadBot() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "bot.py",
            "text/plain",
            "code".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/bots")
                .file(file)
                .header("Authorization", buyerToken)
                .param("name", "Illegal Bot")
                .param("strategyType", "SCALPING")
                .param("disclosedLogic", "Some logic"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Dashboard: Seller retrieves owned bots and status")
    void testSellerDashboardEndpoint() throws Exception {
        Bot bot = botRepository.save(Bot.builder()
            .seller(seller)
            .name("Alpha Mean Reversion")
            .strategyType("MEAN_REVERSION")
            .status(BotStatus.DRAFT)
            .build());

        botVersionRepository.save(BotVersion.builder()
            .bot(bot)
            .versionNumber("1.0.0")
            .disclosedLogic("Bollinger Bands mean reversion")
            .backtestStatus(BacktestStatus.COMPLETED)
            .build());

        mockMvc.perform(get("/api/sellers/me/bots")
                .header("Authorization", sellerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Alpha Mean Reversion"))
            .andExpect(jsonPath("$[0].latestVersionNumber").value("1.0.0"))
            .andExpect(jsonPath("$[0].backtestStatus").value("COMPLETED"));
    }

    @Test
    @DisplayName("Backtest Results: Retrieve persisted metrics for a version")
    void testGetBacktestResultEndpoint() throws Exception {
        Bot bot = botRepository.save(Bot.builder()
            .seller(seller)
            .name("Trend Crossover")
            .strategyType("TREND_FOLLOWING")
            .build());

        BotVersion version = botVersionRepository.save(BotVersion.builder()
            .bot(bot)
            .versionNumber("1.0.0")
            .disclosedLogic("Trend following logic")
            .backtestStatus(BacktestStatus.COMPLETED)
            .build());

        backtestResultRepository.save(BacktestResult.builder()
            .botVersion(version)
            .dateRangeStart(Instant.now().minusSeconds(86400 * 30))
            .dateRangeEnd(Instant.now())
            .methodologyNotes("Vectorbt benchmark test")
            .metrics("{\"win_rate\": 64.5, \"sharpe_ratio\": 1.85, \"max_drawdown\": 8.2}")
            .build());

        mockMvc.perform(get("/api/bots/" + bot.getId() + "/versions/" + version.getId() + "/backtest")
                .header("Authorization", sellerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.botVersionId").value(version.getId().toString()))
            .andExpect(jsonPath("$.methodologyNotes").value("Vectorbt benchmark test"));
    }
}
