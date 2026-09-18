package com.algoadda.core.bot;

import com.algoadda.core.bot.dto.BotResponse;
import com.algoadda.core.bot.dto.BotUploadRequest;
import com.algoadda.core.bot.service.BacktestJobWorker;
import com.algoadda.core.bot.service.BacktestServiceClient;
import com.algoadda.core.bot.service.BotService;
import com.algoadda.core.compliance.ComplianceCheckRepository;
import com.algoadda.core.compliance.ComplianceService;
import com.algoadda.core.compliance.dto.ComplianceCheckResponse;
import com.algoadda.core.user.Role;
import com.algoadda.core.user.User;
import com.algoadda.core.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SuppressWarnings("null")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AsyncBacktestQueueIntegrationTest {

    @Autowired
    private BotService botService;

    @Autowired
    private BacktestJobWorker backtestJobWorker;

    @Autowired
    private BacktestJobRepository backtestJobRepository;

    @Autowired
    private BotVersionRepository botVersionRepository;

    @Autowired
    private BotRepository botRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComplianceService complianceService;

    @Autowired
    private ComplianceCheckRepository complianceCheckRepository;

    @MockBean
    private BacktestServiceClient backtestServiceClient;

    private User seller;

    @BeforeEach
    void setUp() {
        complianceCheckRepository.deleteAll();
        backtestJobRepository.deleteAll();
        botVersionRepository.deleteAll();
        botRepository.deleteAll();
        userRepository.deleteAll();

        seller = userRepository.save(User.builder()
            .email("asyncseller@example.com")
            .passwordHash("hash")
            .role(Role.SELLER)
            .build());
    }

    @Test
    @DisplayName("Async Queue: Upload returns immediately with QUEUED backtestStatus without waiting for backtest")
    void testUploadReturnsImmediatelyWithQueuedStatus() {
        BotUploadRequest request = BotUploadRequest.builder()
            .name("Async Momentum Bot")
            .description("Asynchronous execution test strategy")
            .strategyType("MOMENTUM")
            .disclosedLogic("Buy when 14-period RSI crosses 50 upwards; risk disclaimer present")
            .riskDisclaimer("Trading involves risk of loss.")
            .strategyConfig("{\"symbol\":\"^NSEI\"}")
            .build();

        MockMultipartFile file = new MockMultipartFile("file", "async_strategy.py", "text/plain", "print('hello')".getBytes(StandardCharsets.UTF_8));

        BotResponse response = botService.createBot(seller.getId(), request, file);

        assertThat(response).isNotNull();
        assertThat(response.getLatestVersion()).isNotNull();
        assertThat(response.getLatestVersion().getBacktestStatus()).isEqualTo(BacktestStatus.QUEUED);

        List<BacktestJob> jobs = backtestJobRepository.findByStatusOrderByCreatedAtAsc(BacktestJobStatus.QUEUED);
        assertThat(jobs).hasSize(1);
        assertThat(jobs.get(0).getBotVersion().getId()).isEqualTo(response.getLatestVersion().getId());
    }

    @Test
    @DisplayName("Compliance Gate Race Condition: Compliance check blocks while job is QUEUED, succeeds after Worker completes job")
    void testComplianceGateWaitsForAsyncWorkerCompletion() {
        BotUploadRequest request = BotUploadRequest.builder()
            .name("Compliance Race Bot")
            .description("Testing compliance gate race condition prevention")
            .strategyType("TREND_FOLLOWING")
            .disclosedLogic("Whitebox disclosed strategy logic details here")
            .riskDisclaimer("Risk disclaimer: No guaranteed returns.")
            .strategyConfig("{\"symbol\":\"^NSEI\"}")
            .build();

        MockMultipartFile file = new MockMultipartFile("file", "race_strategy.py", "text/plain", "pass".getBytes(StandardCharsets.UTF_8));

        BotResponse response = botService.createBot(seller.getId(), request, file);
        UUID versionId = response.getLatestVersion().getId();
        BotVersion version = botVersionRepository.findById(versionId).orElseThrow();

        // 1. Evaluate compliance BEFORE async job runs -> MUST BE BLOCKED (passed = false)
        ComplianceCheckResponse initialCheck = complianceService.runAutomatedComplianceCheck(version);
        assertThat(initialCheck.isPassed()).isFalse();
        assertThat(initialCheck.getChecklistResults()).contains("Backtest status is QUEUED");

        // 2. Mock successful backtest service execution
        when(backtestServiceClient.runBacktestJob(any(), any(), any(), any()))
            .thenReturn(BacktestServiceClient.ExecutionResult.ok());

        // 3. Trigger worker poller
        backtestJobWorker.processQueuedJobs();

        // 4. Verify job updated to COMPLETED and version backtestStatus is COMPLETED
        BotVersion completedVersion = botVersionRepository.findById(versionId).orElseThrow();
        assertThat(completedVersion.getBacktestStatus()).isEqualTo(BacktestStatus.COMPLETED);

        // 5. Verify compliance check now passes automatically!
        ComplianceCheckResponse postWorkerCheck = complianceService.runAutomatedComplianceCheck(completedVersion);
        assertThat(postWorkerCheck.isPassed()).isTrue();
    }
}
