package com.algoadda.core.bot;

import com.algoadda.core.bot.dto.MetricDeltaDto;
import com.algoadda.core.bot.dto.VersionComparisonResponse;
import com.algoadda.core.bot.dto.VersionDiffLineDto;
import com.algoadda.core.user.User;
import com.algoadda.core.user.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class BotVersionCompareService {

    private static final Logger log = LoggerFactory.getLogger(BotVersionCompareService.class);

    private final BotRepository botRepository;
    private final BotVersionRepository botVersionRepository;
    private final BacktestResultRepository backtestResultRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public BotVersionCompareService(
        BotRepository botRepository,
        BotVersionRepository botVersionRepository,
        BacktestResultRepository backtestResultRepository,
        UserRepository userRepository,
        ObjectMapper objectMapper
    ) {
        this.botRepository = botRepository;
        this.botVersionRepository = botVersionRepository;
        this.backtestResultRepository = backtestResultRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public VersionComparisonResponse compareVersions(UUID botId, UUID fromVersionId, UUID toVersionId, String requesterEmail) {
        Bot bot = botRepository.findById(botId)
            .orElseThrow(() -> new NoSuchElementException("Bot not found with id: " + botId));

        BotVersion fromVersion = botVersionRepository.findById(fromVersionId)
            .orElseThrow(() -> new NoSuchElementException("From version not found with id: " + fromVersionId));

        BotVersion toVersion = botVersionRepository.findById(toVersionId)
            .orElseThrow(() -> new NoSuchElementException("To version not found with id: " + toVersionId));

        if (!fromVersion.getBot().getId().equals(botId) || !toVersion.getBot().getId().equals(botId)) {
            throw new IllegalArgumentException("Versions do not belong to the specified bot");
        }

        // Authorization check
        User requester = null;
        if (requesterEmail != null && !requesterEmail.isBlank()) {
            requester = userRepository.findByEmail(requesterEmail).orElse(null);
        }

        boolean isOwner = requester != null && bot.getSeller() != null && requester.getId().equals(bot.getSeller().getId());

        // Security rule: Expose draft/unpublished versions only to the owner!
        boolean isPublished = bot.getStatus() == BotStatus.PUBLISHED;
        if (!isOwner && !isPublished) {
            throw new AccessDeniedException("Access denied to unpublished version comparison");
        }

        // Line-by-line diff of disclosed_logic
        List<VersionDiffLineDto> logicDiff = computeLineDiff(
            fromVersion.getDisclosedLogic(),
            toVersion.getDisclosedLogic()
        );

        // Performance metrics side-by-side & deltas
        List<MetricDeltaDto> performanceMetrics = computeMetricsComparison(fromVersionId, toVersionId);

        return new VersionComparisonResponse(
            bot.getId(),
            bot.getName(),
            fromVersion.getId(),
            fromVersion.getVersionNumber(),
            toVersion.getId(),
            toVersion.getVersionNumber(),
            logicDiff,
            performanceMetrics
        );
    }

    public List<VersionDiffLineDto> computeLineDiff(String textFrom, String textTo) {
        String[] linesFrom = textFrom != null && !textFrom.isEmpty() ? textFrom.split("\\r?\\n") : new String[0];
        String[] linesTo = textTo != null && !textTo.isEmpty() ? textTo.split("\\r?\\n") : new String[0];

        int n = linesFrom.length;
        int m = linesTo.length;

        int[][] dp = new int[n + 1][m + 1];
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (linesFrom[i - 1].equals(linesTo[j - 1])) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        List<VersionDiffLineDto> reversed = new ArrayList<>();
        int i = n, j = m;

        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && linesFrom[i - 1].equals(linesTo[j - 1])) {
                reversed.add(new VersionDiffLineDto(VersionDiffLineDto.DiffType.UNCHANGED, linesFrom[i - 1], i, j));
                i--;
                j--;
            } else if (j > 0 && (i == 0 || dp[i][j - 1] >= dp[i - 1][j])) {
                reversed.add(new VersionDiffLineDto(VersionDiffLineDto.DiffType.ADDED, linesTo[j - 1], null, j));
                j--;
            } else if (i > 0 && (j == 0 || dp[i][j - 1] < dp[i - 1][j])) {
                reversed.add(new VersionDiffLineDto(VersionDiffLineDto.DiffType.REMOVED, linesFrom[i - 1], i, null));
                i--;
            }
        }

        Collections.reverse(reversed);
        return reversed;
    }

    private List<MetricDeltaDto> computeMetricsComparison(UUID fromVersionId, UUID toVersionId) {
        BacktestResult fromResult = backtestResultRepository
            .findFirstByBotVersionIdOrderByCreatedAtDesc(fromVersionId)
            .orElse(null);

        BacktestResult toResult = backtestResultRepository
            .findFirstByBotVersionIdOrderByCreatedAtDesc(toVersionId)
            .orElse(null);

        Map<String, Double> fromMetrics = parseMetrics(fromResult != null ? fromResult.getMetrics() : null);
        Map<String, Double> toMetrics = parseMetrics(toResult != null ? toResult.getMetrics() : null);

        List<MetricDeltaDto> list = new ArrayList<>();
        String[] keys = new String[]{"win_rate", "max_drawdown", "sharpe_ratio"};

        for (String key : keys) {
            Double fromVal = fromMetrics.get(key);
            Double toVal = toMetrics.get(key);
            Double delta = (fromVal != null && toVal != null)
                ? (double) Math.round((toVal - fromVal) * 1000.0) / 1000.0
                : null;

            list.add(new MetricDeltaDto(key, fromVal, toVal, delta));
        }

        return list;
    }

    private Map<String, Double> parseMetrics(String json) {
        Map<String, Double> map = new HashMap<>();
        if (json == null || json.isBlank()) return map;

        try {
            JsonNode root = objectMapper.readTree(json);
            if (root.isTextual()) {
                root = objectMapper.readTree(root.asText());
            }
            if (root.has("win_rate")) map.put("win_rate", root.get("win_rate").asDouble());
            if (root.has("max_drawdown")) map.put("max_drawdown", root.get("max_drawdown").asDouble());
            if (root.has("sharpe_ratio")) map.put("sharpe_ratio", root.get("sharpe_ratio").asDouble());
        } catch (Exception e) {
            log.warn("Failed to parse backtest metrics: {}", e.getMessage());
        }

        return map;
    }
}
