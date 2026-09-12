package com.algoadda.core.bot;

import com.algoadda.core.bot.service.BacktestServiceClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class BacktestServiceClientTest {

    @Mock
    private BotVersionRepository botVersionRepository;

    @Mock
    private BacktestResultRepository backtestResultRepository;

    private ObjectMapper objectMapper;
    private BacktestServiceClient client;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        // Point to an invalid host with 0 retries to test timeout/failure path
        client = new BacktestServiceClient(
            botVersionRepository,
            backtestResultRepository,
            objectMapper,
            "http://127.0.0.1:59999", // non-existent port
            100,
            100,
            0
        );
    }

    @Test
    @DisplayName("Failure Path: Gracefully mark BotVersion as FAILED when backtest-service is unreachable")
    void testBacktestFailurePath() {
        Bot bot = Bot.builder().id(UUID.randomUUID()).name("Bot Test").build();
        BotVersion version = BotVersion.builder()
            .id(UUID.randomUUID())
            .bot(bot)
            .versionNumber("1.0.0")
            .disclosedLogic("Test logic")
            .backtestStatus(BacktestStatus.PENDING)
            .build();

        boolean result = client.runBacktest(version, "{\"symbol\":\"^NSEI\"}", Instant.now(), Instant.now());

        assertThat(result).isFalse();
        assertThat(version.getBacktestStatus()).isEqualTo(BacktestStatus.FAILED);
        verify(botVersionRepository).save(version);
        verify(backtestResultRepository, never()).save(any(BacktestResult.class));
    }
}
