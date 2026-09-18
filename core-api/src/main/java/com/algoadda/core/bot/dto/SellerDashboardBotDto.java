package com.algoadda.core.bot.dto;

import com.algoadda.core.bot.BacktestStatus;
import com.algoadda.core.bot.BotStatus;
import com.algoadda.core.compliance.dto.ComplianceCheckResponse;

import java.time.Instant;
import java.util.UUID;

public class SellerDashboardBotDto {

    private UUID botId;
    private String name;
    private String strategyType;
    private BotStatus status;
    private UUID latestVersionId;
    private String latestVersionNumber;
    private BacktestStatus backtestStatus;
    private String errorMessage;
    private Instant createdAt;
    private ComplianceCheckResponse latestComplianceCheck;

    public SellerDashboardBotDto() {
    }

    public SellerDashboardBotDto(UUID botId, String name, String strategyType, BotStatus status, UUID latestVersionId, String latestVersionNumber, BacktestStatus backtestStatus, String errorMessage, Instant createdAt, ComplianceCheckResponse latestComplianceCheck) {
        this.botId = botId;
        this.name = name;
        this.strategyType = strategyType;
        this.status = status;
        this.latestVersionId = latestVersionId;
        this.latestVersionNumber = latestVersionNumber;
        this.backtestStatus = backtestStatus;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.latestComplianceCheck = latestComplianceCheck;
    }

    public static SellerDashboardBotDtoBuilder builder() {
        return new SellerDashboardBotDtoBuilder();
    }

    public static class SellerDashboardBotDtoBuilder {
        private UUID botId;
        private String name;
        private String strategyType;
        private BotStatus status;
        private UUID latestVersionId;
        private String latestVersionNumber;
        private BacktestStatus backtestStatus;
        private String errorMessage;
        private Instant createdAt;
        private ComplianceCheckResponse latestComplianceCheck;

        public SellerDashboardBotDtoBuilder botId(UUID botId) {
            this.botId = botId;
            return this;
        }

        public SellerDashboardBotDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public SellerDashboardBotDtoBuilder strategyType(String strategyType) {
            this.strategyType = strategyType;
            return this;
        }

        public SellerDashboardBotDtoBuilder status(BotStatus status) {
            this.status = status;
            return this;
        }

        public SellerDashboardBotDtoBuilder latestVersionId(UUID latestVersionId) {
            this.latestVersionId = latestVersionId;
            return this;
        }

        public SellerDashboardBotDtoBuilder latestVersionNumber(String latestVersionNumber) {
            this.latestVersionNumber = latestVersionNumber;
            return this;
        }

        public SellerDashboardBotDtoBuilder backtestStatus(BacktestStatus backtestStatus) {
            this.backtestStatus = backtestStatus;
            return this;
        }

        public SellerDashboardBotDtoBuilder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public SellerDashboardBotDtoBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public SellerDashboardBotDtoBuilder latestComplianceCheck(ComplianceCheckResponse latestComplianceCheck) {
            this.latestComplianceCheck = latestComplianceCheck;
            return this;
        }

        public SellerDashboardBotDto build() {
            return new SellerDashboardBotDto(botId, name, strategyType, status, latestVersionId, latestVersionNumber, backtestStatus, errorMessage, createdAt, latestComplianceCheck);
        }
    }

    public UUID getBotId() {
        return botId;
    }

    public void setBotId(UUID botId) {
        this.botId = botId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStrategyType() {
        return strategyType;
    }

    public void setStrategyType(String strategyType) {
        this.strategyType = strategyType;
    }

    public BotStatus getStatus() {
        return status;
    }

    public void setStatus(BotStatus status) {
        this.status = status;
    }

    public UUID getLatestVersionId() {
        return latestVersionId;
    }

    public void setLatestVersionId(UUID latestVersionId) {
        this.latestVersionId = latestVersionId;
    }

    public String getLatestVersionNumber() {
        return latestVersionNumber;
    }

    public void setLatestVersionNumber(String latestVersionNumber) {
        this.latestVersionNumber = latestVersionNumber;
    }

    public BacktestStatus getBacktestStatus() {
        return backtestStatus;
    }

    public void setBacktestStatus(BacktestStatus backtestStatus) {
        this.backtestStatus = backtestStatus;
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

    public ComplianceCheckResponse getLatestComplianceCheck() {
        return latestComplianceCheck;
    }

    public void setLatestComplianceCheck(ComplianceCheckResponse latestComplianceCheck) {
        this.latestComplianceCheck = latestComplianceCheck;
    }
}
