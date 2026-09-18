package com.algoadda.core.bot;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "backtest_jobs")
public class BacktestJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bot_version_id", nullable = false)
    private BotVersion botVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BacktestJobStatus status = BacktestJobStatus.QUEUED;

    @Column(name = "strategy_config", columnDefinition = "TEXT")
    private String strategyConfig;

    @Column(name = "date_range_start")
    private Instant dateRangeStart;

    @Column(name = "date_range_end")
    private Instant dateRangeEnd;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public BacktestJob() {
    }

    public BacktestJob(UUID id, BotVersion botVersion, BacktestJobStatus status, String strategyConfig, Instant dateRangeStart, Instant dateRangeEnd, String errorMessage, Instant createdAt, Instant startedAt, Instant completedAt) {
        this.id = id;
        this.botVersion = botVersion;
        this.status = status != null ? status : BacktestJobStatus.QUEUED;
        this.strategyConfig = strategyConfig;
        this.dateRangeStart = dateRangeStart;
        this.dateRangeEnd = dateRangeEnd;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    public static BacktestJobBuilder builder() {
        return new BacktestJobBuilder();
    }

    public static class BacktestJobBuilder {
        private UUID id;
        private BotVersion botVersion;
        private BacktestJobStatus status = BacktestJobStatus.QUEUED;
        private String strategyConfig;
        private Instant dateRangeStart;
        private Instant dateRangeEnd;
        private String errorMessage;
        private Instant createdAt;
        private Instant startedAt;
        private Instant completedAt;

        public BacktestJobBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public BacktestJobBuilder botVersion(BotVersion botVersion) {
            this.botVersion = botVersion;
            return this;
        }

        public BacktestJobBuilder status(BacktestJobStatus status) {
            this.status = status;
            return this;
        }

        public BacktestJobBuilder strategyConfig(String strategyConfig) {
            this.strategyConfig = strategyConfig;
            return this;
        }

        public BacktestJobBuilder dateRangeStart(Instant dateRangeStart) {
            this.dateRangeStart = dateRangeStart;
            return this;
        }

        public BacktestJobBuilder dateRangeEnd(Instant dateRangeEnd) {
            this.dateRangeEnd = dateRangeEnd;
            return this;
        }

        public BacktestJobBuilder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public BacktestJobBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public BacktestJobBuilder startedAt(Instant startedAt) {
            this.startedAt = startedAt;
            return this;
        }

        public BacktestJobBuilder completedAt(Instant completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public BacktestJob build() {
            return new BacktestJob(id, botVersion, status, strategyConfig, dateRangeStart, dateRangeEnd, errorMessage, createdAt, startedAt, completedAt);
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

    public BacktestJobStatus getStatus() {
        return status;
    }

    public void setStatus(BacktestJobStatus status) {
        this.status = status;
    }

    public String getStrategyConfig() {
        return strategyConfig;
    }

    public void setStrategyConfig(String strategyConfig) {
        this.strategyConfig = strategyConfig;
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

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
