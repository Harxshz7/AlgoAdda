package com.algoadda.core.bot;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "backtest_results")
public class BacktestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bot_version_id", nullable = false)
    private BotVersion botVersion;

    @Column(name = "date_range_start")
    private Instant dateRangeStart;

    @Column(name = "date_range_end")
    private Instant dateRangeEnd;

    @Column(name = "methodology_notes", columnDefinition = "TEXT")
    private String methodologyNotes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metrics")
    private String metrics;

    @Column(name = "report_file_key", length = 500)
    private String reportFileKey;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public BacktestResult() {
    }

    public BacktestResult(UUID id, BotVersion botVersion, Instant dateRangeStart, Instant dateRangeEnd, String methodologyNotes, String metrics, String reportFileKey, Instant createdAt) {
        this.id = id;
        this.botVersion = botVersion;
        this.dateRangeStart = dateRangeStart;
        this.dateRangeEnd = dateRangeEnd;
        this.methodologyNotes = methodologyNotes;
        this.metrics = metrics;
        this.reportFileKey = reportFileKey;
        this.createdAt = createdAt;
    }

    public static BacktestResultBuilder builder() {
        return new BacktestResultBuilder();
    }

    public static class BacktestResultBuilder {
        private UUID id;
        private BotVersion botVersion;
        private Instant dateRangeStart;
        private Instant dateRangeEnd;
        private String methodologyNotes;
        private String metrics;
        private String reportFileKey;
        private Instant createdAt;

        public BacktestResultBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public BacktestResultBuilder botVersion(BotVersion botVersion) {
            this.botVersion = botVersion;
            return this;
        }

        public BacktestResultBuilder dateRangeStart(Instant dateRangeStart) {
            this.dateRangeStart = dateRangeStart;
            return this;
        }

        public BacktestResultBuilder dateRangeEnd(Instant dateRangeEnd) {
            this.dateRangeEnd = dateRangeEnd;
            return this;
        }

        public BacktestResultBuilder methodologyNotes(String methodologyNotes) {
            this.methodologyNotes = methodologyNotes;
            return this;
        }

        public BacktestResultBuilder metrics(String metrics) {
            this.metrics = metrics;
            return this;
        }

        public BacktestResultBuilder reportFileKey(String reportFileKey) {
            this.reportFileKey = reportFileKey;
            return this;
        }

        public BacktestResultBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public BacktestResult build() {
            return new BacktestResult(id, botVersion, dateRangeStart, dateRangeEnd, methodologyNotes, metrics, reportFileKey, createdAt);
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BotVersion getBotVersion() {
        return botVersion;
    }

    public void setBotVersion(BotVersion botVersion) {
        this.botVersion = botVersion;
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
