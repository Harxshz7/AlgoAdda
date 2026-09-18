package com.algoadda.core.bot.service;

import com.algoadda.core.bot.BacktestResult;
import com.algoadda.core.bot.BacktestResultRepository;
import com.algoadda.core.bot.BacktestStatus;
import com.algoadda.core.bot.BotVersion;
import com.algoadda.core.bot.BotVersionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.algoadda.core.compliance.ComplianceService;
import org.springframework.context.annotation.Lazy;
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
    private final ComplianceService complianceService;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final int maxRetries;

    public BacktestServiceClient(
        BotVersionRepository botVersionRepository,
        BacktestResultRepository backtestResultRepository,
        @Lazy ComplianceService complianceService,
        ObjectMapper objectMapper,
        @Value("${algoadda.backtest-service.url:http://localhost:8000}") String baseUrl,
        @Value("${algoadda.backtest-service.connect-timeout-ms:5000}") int connectTimeoutMs,
        @Value("${algoadda.backtest-service.read-timeout-ms:30000}") int readTimeoutMs,
        @Value("${algoadda.backtest-service.max-retries:2}") int maxRetries
    ) {
        this.botVersionRepository = botVersionRepository;
        this.backtestResultRepository = backtestResultRepository;
        this.complianceService = complianceService;
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

    public static class ExecutionResult {
        private final boolean success;
        private final String errorMessage;

        public ExecutionResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public static ExecutionResult ok() {
            return new ExecutionResult(true, null);
        }

        public static ExecutionResult failure(String errorMessage) {
            return new ExecutionResult(false, errorMessage);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    @Transactional
    public ExecutionResult runBacktestJob(BotVersion botVersion, String strategyConfigJson, Instant start, Instant end) {
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
            log.warn("Failed to parse strategyConfigJson: {}", e.getMessage());
            return ExecutionResult.failure("invalid strategy_config: " + e.getMessage());
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
                        .riskLabel(RiskClassifier.classify(metricsJson))
                        .build();

                    backtestResultRepository.save(backtestResult);
                    log.info("Backtest result persisted for botVersion {}", botVersion.getId());
                    return ExecutionResult.ok();
                }
            } catch (org.springframework.web.client.HttpClientErrorException | org.springframework.web.client.HttpServerErrorException e) {
                lastException = e;
                log.warn("Backtest HTTP error (attempt {}): {} {}", attempts, e.getStatusCode(), e.getResponseBodyAsString());
                if (e.getStatusCode().is4xxClientError()) {
                    return ExecutionResult.failure("backtest-service HTTP " + e.getStatusCode().value() + ": " + e.getResponseBodyAsString());
                }
            } catch (Exception e) {
                lastException = e;
                log.warn("Backtest call attempt {} failed for botVersion {}: {}", attempts, botVersion.getId(), e.getMessage());
            }
        }

        String detailedError = "backtest-service unreachable: " + (lastException != null ? lastException.getMessage() : "Connection timeout / no response");
        log.error("All backtest attempts failed for botVersion {}. Cause: {}", botVersion.getId(), detailedError);
        return ExecutionResult.failure(detailedError);
    }

    @Transactional
    public boolean runBacktest(BotVersion botVersion, String strategyConfigJson, Instant start, Instant end) {
        ExecutionResult result = runBacktestJob(botVersion, strategyConfigJson, start, end);
        if (result.isSuccess()) {
            botVersion.setBacktestStatus(BacktestStatus.COMPLETED);
            BotVersion savedVersion = botVersionRepository.save(botVersion);
            try {
                complianceService.runAutomatedComplianceCheck(savedVersion);
            } catch (Exception e) {
                log.error("Failed compliance check for botVersion {}: {}", botVersion.getId(), e.getMessage());
            }
            return true;
        } else {
            botVersion.setBacktestStatus(BacktestStatus.FAILED);
            botVersionRepository.save(botVersion);
            return false;
        }
    }
}
