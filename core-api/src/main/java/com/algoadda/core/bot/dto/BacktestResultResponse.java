package com.algoadda.core.bot.dto;

import java.time.Instant;
import java.util.UUID;

public class BacktestResultResponse {

    private UUID id;
    private UUID botVersionId;
    private Instant dateRangeStart;
    private Instant dateRangeEnd;
    private String methodologyNotes;
    private String metrics;
    private String reportFileKey;
    private Instant createdAt;

    public BacktestResultResponse() {
    }

    public BacktestResultResponse(UUID id, UUID botVersionId, Instant dateRangeStart, Instant dateRangeEnd, String methodologyNotes, String metrics, String reportFileKey, Instant createdAt) {
        this.id = id;
        this.botVersionId = botVersionId;
        this.dateRangeStart = dateRangeStart;
        this.dateRangeEnd = dateRangeEnd;
        this.methodologyNotes = methodologyNotes;
        this.metrics = metrics;
        this.reportFileKey = reportFileKey;
        this.createdAt = createdAt;
    }

    public static BacktestResultResponseBuilder builder() {
        return new BacktestResultResponseBuilder();
    }

    public static class BacktestResultResponseBuilder {
        private UUID id;
        private UUID botVersionId;
        private Instant dateRangeStart;
        private Instant dateRangeEnd;
        private String methodologyNotes;
        private String metrics;
        private String reportFileKey;
        private Instant createdAt;

        public BacktestResultResponseBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public BacktestResultResponseBuilder botVersionId(UUID botVersionId) {
            this.botVersionId = botVersionId;
            return this;
        }

        public BacktestResultResponseBuilder dateRangeStart(Instant dateRangeStart) {
            this.dateRangeStart = dateRangeStart;
            return this;
        }

        public BacktestResultResponseBuilder dateRangeEnd(Instant dateRangeEnd) {
            this.dateRangeEnd = dateRangeEnd;
            return this;
        }

        public BacktestResultResponseBuilder methodologyNotes(String methodologyNotes) {
            this.methodologyNotes = methodologyNotes;
            return this;
        }

        public BacktestResultResponseBuilder metrics(String metrics) {
            this.metrics = metrics;
            return this;
        }

        public BacktestResultResponseBuilder reportFileKey(String reportFileKey) {
            this.reportFileKey = reportFileKey;
            return this;
        }

        public BacktestResultResponseBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public BacktestResultResponse build() {
            return new BacktestResultResponse(id, botVersionId, dateRangeStart, dateRangeEnd, methodologyNotes, metrics, reportFileKey, createdAt);
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getBotVersionId() {
        return botVersionId;
    }

    public void setBotVersionId(UUID botVersionId) {
        this.botVersionId = botVersionId;
    }

    public Instant getDateRangeStart() {
        return dateRangeStart;
    }

    public void setDateRangeStart(Instant dateRangeStart) {
        this.dateRangeStart = dateRangeStart;
    }

    public Instant getDateRangeEnd() {
        return dateRangeEnd;
    }

    public void setDateRangeEnd(Instant dateRangeEnd) {
        this.dateRangeEnd = dateRangeEnd;
    }

    public String getMethodologyNotes() {
        return methodologyNotes;
    }

    public void setMethodologyNotes(String methodologyNotes) {
        this.methodologyNotes = methodologyNotes;
    }

    public String getMetrics() {
        return metrics;
    }

    public void setMetrics(String metrics) {
        this.metrics = metrics;
    }

    public String getReportFileKey() {
        return reportFileKey;
    }

    public void setReportFileKey(String reportFileKey) {
        this.reportFileKey = reportFileKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
