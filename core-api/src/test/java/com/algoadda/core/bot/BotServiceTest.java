package com.algoadda.core.bot;

import com.algoadda.core.bot.dto.*;
import com.algoadda.core.bot.service.BacktestServiceClient;
import com.algoadda.core.bot.service.BotService;
import com.algoadda.core.bot.service.S3StorageService;
import com.algoadda.core.user.Role;
import com.algoadda.core.user.User;
import com.algoadda.core.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class BotServiceTest {

    @Mock
    private BotRepository botRepository;

    @Mock
    private BotVersionRepository botVersionRepository;

    @Mock
    private BacktestResultRepository backtestResultRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private S3StorageService s3StorageService;

    @Mock
    private BacktestServiceClient backtestServiceClient;

    private BotService botService;

    @BeforeEach
    void setUp() {
        botService = new BotService(
            botRepository,
            botVersionRepository,
            backtestResultRepository,
            userRepository,
            s3StorageService,
            backtestServiceClient
        );
    }

    @Test
    @DisplayName("Upload: Reject blank disclosed_logic with White Box compliance error")
    void testUploadRejectsBlankDisclosedLogic() {
        UUID sellerId = UUID.randomUUID();
        BotUploadRequest request = BotUploadRequest.builder()
            .name("Alpha Bot")
            .strategyType("MOMENTUM")
            .disclosedLogic("   ")
            .build();

        MockMultipartFile file = new MockMultipartFile("file", "bot.py", "text/plain", "print(1)".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> botService.createBot(sellerId, request, file))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Disclosed logic is required for all listings under AlgoAdda's White Box compliance model");
    }

    @Test
    @DisplayName("Upload: Successfully create Bot, BotVersion (1.0.0), S3 upload, and trigger backtest")
    void testUploadSuccess() {
        UUID sellerId = UUID.randomUUID();
        User seller = User.builder()
            .id(sellerId)
            .email("seller@algoadda.com")
            .role(Role.SELLER)
            .build();

        BotUploadRequest request = BotUploadRequest.builder()
            .name("Trend Master")
            .description("SMA 20/50 Crossover strategy")
            .strategyType("TREND_FOLLOWING")
            .disclosedLogic("Enters on 20 SMA crossing above 50 SMA; exits on reverse")
            .strategyConfig("{\"symbol\":\"^NSEI\"}")
            .build();

        MockMultipartFile file = new MockMultipartFile("file", "trend_master.py", "text/x-python", "code".getBytes(StandardCharsets.UTF_8));

        UUID botId = UUID.randomUUID();
        Bot savedBot = Bot.builder()
            .id(botId)
            .seller(seller)
            .name(request.getName())
            .description(request.getDescription())
            .strategyType(request.getStrategyType())
            .status(BotStatus.DRAFT)
            .createdAt(Instant.now())
            .build();

        UUID versionId = UUID.randomUUID();
        BotVersion savedVersion = BotVersion.builder()
            .id(versionId)
            .bot(savedBot)
            .versionNumber("1.0.0")
            .disclosedLogic(request.getDisclosedLogic())
            .fileStorageKey("bots/" + botId + "/v1.0.0/trend_master.py")
            .backtestStatus(BacktestStatus.COMPLETED)
            .createdAt(Instant.now())
            .build();

        when(userRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(botRepository.save(any(Bot.class))).thenReturn(savedBot);
        when(s3StorageService.uploadFile(anyString(), any(byte[].class), anyString())).thenReturn("key");
        when(botVersionRepository.save(any(BotVersion.class))).thenReturn(savedVersion);
        when(botVersionRepository.findById(versionId)).thenReturn(Optional.of(savedVersion));

        BotResponse response = botService.createBot(sellerId, request, file);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Trend Master");
        assertThat(response.getStatus()).isEqualTo(BotStatus.DRAFT);
        assertThat(response.getLatestVersion()).isNotNull();
        assertThat(response.getLatestVersion().getVersionNumber()).isEqualTo("1.0.0");
        assertThat(response.getLatestVersion().getDisclosedLogic()).contains("SMA");

        verify(s3StorageService).uploadFile(contains("trend_master.py"), any(byte[].class), eq("text/x-python"));
        verify(backtestServiceClient).runBacktest(any(BotVersion.class), eq("{\"symbol\":\"^NSEI\"}"), any(), any());
    }

    @Test
    @DisplayName("Create Version: Increment version number to 1.1.0 for existing bot")
    void testCreateVersionSuccess() {
        UUID sellerId = UUID.randomUUID();
        UUID botId = UUID.randomUUID();

        User seller = User.builder().id(sellerId).email("seller@algoadda.com").role(Role.SELLER).build();
        Bot bot = Bot.builder().id(botId).seller(seller).name("Bot 1").strategyType("MOMENTUM").build();

        BotVersion v1 = BotVersion.builder().id(UUID.randomUUID()).bot(bot).versionNumber("1.0.0").build();

        BotVersionUploadRequest request = BotVersionUploadRequest.builder()
            .disclosedLogic("Updated logic with RSI filter")
            .changelog("Added RSI 14 filter to avoid false breakouts")
            .build();

        MockMultipartFile file = new MockMultipartFile("file", "v2.py", "text/plain", "code v2".getBytes(StandardCharsets.UTF_8));

        UUID version2Id = UUID.randomUUID();
        BotVersion v2 = BotVersion.builder()
            .id(version2Id)
            .bot(bot)
            .versionNumber("1.1.0")
            .disclosedLogic(request.getDisclosedLogic())
            .changelog(request.getChangelog())
            .backtestStatus(BacktestStatus.COMPLETED)
            .build();

        when(botRepository.findById(botId)).thenReturn(Optional.of(bot));
        when(botVersionRepository.findByBotId(botId)).thenReturn(List.of(v1));
        when(s3StorageService.uploadFile(anyString(), any(byte[].class), anyString())).thenReturn("key2");
        when(botVersionRepository.save(any(BotVersion.class))).thenReturn(v2);
        when(botVersionRepository.findById(version2Id)).thenReturn(Optional.of(v2));

        BotVersionResponse response = botService.createBotVersion(sellerId, botId, request, file);

        assertThat(response).isNotNull();
        assertThat(response.getVersionNumber()).isEqualTo("1.1.0");
        assertThat(response.getChangelog()).contains("RSI");
        verify(s3StorageService).uploadFile(contains("v1.1.0"), any(byte[].class), anyString());
    }

    @Test
    @DisplayName("Dashboard: Retrieve seller bots with latest version status")
    void testGetSellerDashboardBots() {
        UUID sellerId = UUID.randomUUID();
        UUID botId = UUID.randomUUID();
        User seller = User.builder().id(sellerId).build();
        Bot bot = Bot.builder().id(botId).seller(seller).name("Scalper Pro").strategyType("SCALPING").status(BotStatus.DRAFT).build();
        BotVersion v1 = BotVersion.builder().id(UUID.randomUUID()).bot(bot).versionNumber("1.0.0").backtestStatus(BacktestStatus.COMPLETED).build();

        when(botRepository.findBySellerId(sellerId)).thenReturn(List.of(bot));
        when(botVersionRepository.findFirstByBotIdOrderByCreatedAtDesc(botId)).thenReturn(Optional.of(v1));

        List<SellerDashboardBotDto> dashboard = botService.getSellerDashboardBots(sellerId);

        assertThat(dashboard).hasSize(1);
        assertThat(dashboard.get(0).getName()).isEqualTo("Scalper Pro");
        assertThat(dashboard.get(0).getLatestVersionNumber()).isEqualTo("1.0.0");
        assertThat(dashboard.get(0).getBacktestStatus()).isEqualTo(BacktestStatus.COMPLETED);
    }
}
