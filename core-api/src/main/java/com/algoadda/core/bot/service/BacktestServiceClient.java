package com.algoadda.core.bot.service;

import com.algoadda.core.bot.BacktestResult;
import com.algoadda.core.bot.BacktestResultRepository;
import com.algoadda.core.bot.BacktestStatus;
import com.algoadda.core.bot.BotVersion;
import com.algoadda.core.bot.BotVersionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("null")
@Service
public class BacktestServiceClient {

    private static final Logger log = LoggerFactory.getLogger(BacktestServiceClient.class);

    private final BotVersionRepository botVersionRepository;
    private final BacktestResultRepository backtestResultRepository;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final int maxRetries;

    public BacktestServiceClient(
        BotVersionRepository botVersionRepository,
        BacktestResultRepository backtestResultRepository,
        ObjectMapper objectMapper,
        @Value("${algoadda.backtest-service.url:http://localhost:8000}") String baseUrl,
        @Value("${algoadda.backtest-service.connect-timeout-ms:5000}") int connectTimeoutMs,
        @Value("${algoadda.backtest-service.read-timeout-ms:30000}") int readTimeoutMs,
        @Value("${algoadda.backtest-service.max-retries:2}") int maxRetries
    ) {
        this.botVersionRepository = botVersionRepository;
        this.backtestResultRepository = backtestResultRepository;
        this.objectMapper = objectMapper;
        this.maxRetries = maxRetries;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .requestFactory(requestFactory)
            .build();
    }

    @Transactional
    public boolean runBacktest(BotVersion botVersion, String strategyConfigJson, Instant start, Instant end) {
        Instant rangeStart = start != null ? start : Instant.now().minus(365, ChronoUnit.DAYS);
        Instant rangeEnd = end != null ? end : Instant.now();

        Map<String, Object> payload = new HashMap<>();
        try {
            if (strategyConfigJson != null && !strategyConfigJson.isBlank()) {
                payload.put("strategy_config", objectMapper.readTree(strategyConfigJson));
            } else {
                Map<String, Object> defaultConfig = Map.of(
                    "symbol", "^NSEI",
                    "timeframe", "1d",
                    "strategy_name", "SMA_CROSSOVER",
                    "parameters", Map.of("fast_period", 20, "slow_period", 50)
                );
                payload.put("strategy_config", defaultConfig);
            }
        } catch (Exception e) {
            log.warn("Failed to parse strategyConfigJson, using raw object: {}", e.getMessage());
            payload.put("strategy_config", Map.of("raw", strategyConfigJson != null ? strategyConfigJson : ""));
        }

        payload.put("date_range_start", rangeStart.toString());
        payload.put("date_range_end", rangeEnd.toString());

        int attempts = 0;
        Exception lastException = null;

        while (attempts <= maxRetries) {
            attempts++;
            try {
                log.info("Sending backtest request for botVersion {} (attempt {}/{})", botVersion.getId(), attempts, maxRetries + 1);

                String responseBody = restClient.post()
                    .uri("/backtest")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(String.class);

                if (responseBody != null) {
                    JsonNode rootNode = objectMapper.readTree(responseBody);
                    JsonNode metricsNode = rootNode.path("metrics");
                    String metricsJson = metricsNode.isMissingNode() ? "{}" : objectMapper.writeValueAsString(metricsNode);
                    String summary = rootNode.path("report_summary").asText("Backtest completed successfully.");

                    BacktestResult backtestResult = BacktestResult.builder()
                        .botVersion(botVersion)
                        .dateRangeStart(rangeStart)
                        .dateRangeEnd(rangeEnd)
                        .methodologyNotes("Vectorbt backtest executed via Backtest Service: " + summary)
                        .metrics(metricsJson)
                        .build();

                    backtestResultRepository.save(backtestResult);

                    botVersion.setBacktestStatus(BacktestStatus.COMPLETED);
                    botVersionRepository.save(botVersion);
                    log.info("Backtest successfully completed and persisted for botVersion {}", botVersion.getId());
                    return true;
                }
            } catch (Exception e) {
                lastException = e;
                log.warn("Backtest call attempt {} failed for botVersion {}: {}", attempts, botVersion.getId(), e.getMessage());
            }
        }

        // All retries failed - handle gracefully
        log.error("All backtest attempts failed for botVersion {}. Setting status to FAILED. Cause: {}", botVersion.getId(), lastException != null ? lastException.getMessage() : "Unknown");
        botVersion.setBacktestStatus(BacktestStatus.FAILED);
        botVersionRepository.save(botVersion);
        return false;
    }
}
