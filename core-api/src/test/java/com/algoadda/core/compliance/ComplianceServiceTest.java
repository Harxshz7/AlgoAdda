package com.algoadda.core.compliance;

import com.algoadda.core.bot.*;
import com.algoadda.core.compliance.dto.CheckItemResult;
import com.algoadda.core.compliance.dto.ComplianceCheckResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class ComplianceServiceTest {

    @Mock
    private ComplianceCheckRepository complianceCheckRepository;

    @Mock
    private BotVersionRepository botVersionRepository;

    @Mock
    private BotRepository botRepository;

    @Mock
    private BacktestResultRepository backtestResultRepository;

    @Mock
    private com.algoadda.core.email.EmailService emailService;

    private ObjectMapper objectMapper;
    private List<String> blocklist;
    private ComplianceService complianceService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        blocklist = List.of("guaranteed", "assured returns", "risk-free", "no loss", "100% profit");
        complianceService = new ComplianceService(
            complianceCheckRepository,
            botVersionRepository,
            botRepository,
            backtestResultRepository,
            objectMapper,
            emailService,
            blocklist
        );
    }

    @Test
    void checkDisclosedLogic_Pass() {
        BotVersion version = BotVersion.builder()
            .disclosedLogic("This strategy uses SMA 20 and 50 crossover.")
            .build();

        CheckItemResult result = complianceService.checkDisclosedLogic(version);
        assertTrue(result.isPassed());
        assertEquals("Disclosed logic provided", result.getNote());
    }

    @Test
    void checkDisclosedLogic_Fail_NullOrEmpty() {
        BotVersion versionEmpty = BotVersion.builder()
            .disclosedLogic("   ")
            .build();

        CheckItemResult resultEmpty = complianceService.checkDisclosedLogic(versionEmpty);
        assertFalse(resultEmpty.isPassed());

        BotVersion versionNull = BotVersion.builder()
            .disclosedLogic(null)
            .build();

        CheckItemResult resultNull = complianceService.checkDisclosedLogic(versionNull);
        assertFalse(resultNull.isPassed());
    }

    @Test
    void checkGuaranteedReturnLanguage_Pass() {
        Bot bot = Bot.builder()
            .name("Safe Momentum Trader")
            .description("A momentum trading strategy based on RSI and MACD.")
            .build();

        BotVersion version = BotVersion.builder()
            .bot(bot)
            .disclosedLogic("Buys when RSI < 30 and sells when RSI > 70.")
            .build();

        CheckItemResult result = complianceService.checkGuaranteedReturnLanguage(version);
        assertTrue(result.isPassed());
    }

    @Test
    void checkGuaranteedReturnLanguage_Fail_GuaranteedWord() {
        Bot bot = Bot.builder()
            .name("Super Bot")
            .description("High return strategy")
            .build();

        BotVersion version = BotVersion.builder()
            .bot(bot)
            .disclosedLogic("This strategy offers guaranteed 5% daily returns!")
            .build();

        CheckItemResult result = complianceService.checkGuaranteedReturnLanguage(version);
        assertFalse(result.isPassed());
        assertTrue(result.getNote().contains("guaranteed"));
    }

    @Test
    void checkGuaranteedReturnLanguage_Fail_RiskFreeDescription() {
        Bot bot = Bot.builder()
            .name("Bot Pro")
            .description("Completely Risk-Free arbitrage bot")
            .build();

        BotVersion version = BotVersion.builder()
            .bot(bot)
            .disclosedLogic("Arbitrage strategy across exchanges")
            .build();

        CheckItemResult result = complianceService.checkGuaranteedReturnLanguage(version);
        assertFalse(result.isPassed());
        assertTrue(result.getNote().contains("risk-free"));
    }

    @Test
    void checkRiskDisclaimer_Pass() {
        Bot bot = Bot.builder()
            .riskDisclaimer("Trading futures and options carries substantial risk of financial loss.")
            .build();

        BotVersion version = BotVersion.builder()
            .bot(bot)
            .build();

        CheckItemResult result = complianceService.checkRiskDisclaimer(version);
        assertTrue(result.isPassed());
    }

    @Test
    void checkRiskDisclaimer_Fail_Missing() {
        Bot bot = Bot.builder()
            .riskDisclaimer("   ")
            .build();

        BotVersion version = BotVersion.builder()
            .bot(bot)
            .build();

        CheckItemResult result = complianceService.checkRiskDisclaimer(version);
        assertFalse(result.isPassed());
    }

    @Test
    void checkBacktestStatus_Pass() {
        UUID versionId = UUID.randomUUID();
        BotVersion version = BotVersion.builder()
            .id(versionId)
            .backtestStatus(BacktestStatus.COMPLETED)
            .build();

        BacktestResult backtestResult = BacktestResult.builder()
            .botVersion(version)
            .metrics("{}")
            .build();

        when(backtestResultRepository.findFirstByBotVersionIdOrderByCreatedAtDesc(versionId))
            .thenReturn(Optional.of(backtestResult));

        CheckItemResult result = complianceService.checkBacktestStatus(version);
        assertTrue(result.isPassed());
    }

    @Test
    void checkBacktestStatus_Fail_PendingOrFailed() {
        UUID versionId = UUID.randomUUID();
        BotVersion version = BotVersion.builder()
            .id(versionId)
            .backtestStatus(BacktestStatus.PENDING)
            .build();

        CheckItemResult resultNoRecord = complianceService.checkBacktestStatus(version);
        assertFalse(resultNoRecord.isPassed());

        BacktestResult backtestResult = BacktestResult.builder().botVersion(version).build();
        when(backtestResultRepository.findFirstByBotVersionIdOrderByCreatedAtDesc(versionId))
            .thenReturn(Optional.of(backtestResult));

        CheckItemResult resultPending = complianceService.checkBacktestStatus(version);
        assertFalse(resultPending.isPassed());
    }

    @Test
    void runAutomatedComplianceCheck_FullPass() {
        UUID versionId = UUID.randomUUID();
        Bot bot = Bot.builder()
            .name("Safe Trader")
            .description("Clean strategy")
            .riskDisclaimer("Capital at risk.")
            .build();

        BotVersion version = BotVersion.builder()
            .id(versionId)
            .bot(bot)
            .disclosedLogic("Disclosed strategy rules")
            .backtestStatus(BacktestStatus.COMPLETED)
            .build();

        when(backtestResultRepository.findFirstByBotVersionIdOrderByCreatedAtDesc(versionId))
            .thenReturn(Optional.of(BacktestResult.builder().botVersion(version).build()));

        when(complianceCheckRepository.save(any(ComplianceCheck.class)))
            .thenAnswer(invocation -> {
                ComplianceCheck check = invocation.getArgument(0);
                check.setId(UUID.randomUUID());
                check.setReviewedAt(Instant.now());
                return check;
            });

        ComplianceCheckResponse response = complianceService.runAutomatedComplianceCheck(version);
        assertTrue(response.isPassed());
        assertEquals(ReviewerType.AUTO, response.getReviewerType());
    }
}
