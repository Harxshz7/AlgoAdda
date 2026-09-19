package com.algoadda.core.bot.dto;

import java.util.List;
import java.util.UUID;

public class VersionComparisonResponse {
    private UUID botId;
    private String botName;
    private UUID fromVersionId;
    private String fromVersionNumber;
    private UUID toVersionId;
    private String toVersionNumber;
    private List<VersionDiffLineDto> logicDiff;
    private List<MetricDeltaDto> performanceMetrics;

    public VersionComparisonResponse() {
    }

    public VersionComparisonResponse(UUID botId, String botName, UUID fromVersionId, String fromVersionNumber,
                                     UUID toVersionId, String toVersionNumber,
                                     List<VersionDiffLineDto> logicDiff,
                                     List<MetricDeltaDto> performanceMetrics) {
        this.botId = botId;
        this.botName = botName;
        this.fromVersionId = fromVersionId;
        this.fromVersionNumber = fromVersionNumber;
        this.toVersionId = toVersionId;
        this.toVersionNumber = toVersionNumber;
        this.logicDiff = logicDiff;
        this.performanceMetrics = performanceMetrics;
    }

    public UUID getBotId() { return botId; }
    public void setBotId(UUID botId) { this.botId = botId; }

    public String getBotName() { return botName; }
    public void setBotName(String botName) { this.botName = botName; }

    public UUID getFromVersionId() { return fromVersionId; }
    public void setFromVersionId(UUID fromVersionId) { this.fromVersionId = fromVersionId; }

    public String getFromVersionNumber() { return fromVersionNumber; }
    public void setFromVersionNumber(String fromVersionNumber) { this.fromVersionNumber = fromVersionNumber; }

    public UUID getToVersionId() { return toVersionId; }
    public void setToVersionId(UUID toVersionId) { this.toVersionId = toVersionId; }

    public String getToVersionNumber() { return toVersionNumber; }
    public void setToVersionNumber(String toVersionNumber) { this.toVersionNumber = toVersionNumber; }

    public List<VersionDiffLineDto> getLogicDiff() { return logicDiff; }
    public void setLogicDiff(List<VersionDiffLineDto> logicDiff) { this.logicDiff = logicDiff; }

    public List<MetricDeltaDto> getPerformanceMetrics() { return performanceMetrics; }
    public void setPerformanceMetrics(List<MetricDeltaDto> performanceMetrics) { this.performanceMetrics = performanceMetrics; }
}
