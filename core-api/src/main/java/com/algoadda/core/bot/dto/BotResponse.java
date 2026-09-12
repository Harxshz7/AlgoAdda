package com.algoadda.core.bot.dto;

import com.algoadda.core.bot.BotStatus;

import java.time.Instant;
import java.util.UUID;

public class BotResponse {

    private UUID id;
    private UUID sellerId;
    private String name;
    private String description;
    private String strategyType;
    private BotStatus status;
    private Instant createdAt;
    private BotVersionResponse latestVersion;

    public BotResponse() {
    }

    public BotResponse(UUID id, UUID sellerId, String name, String description, String strategyType, BotStatus status, Instant createdAt, BotVersionResponse latestVersion) {
        this.id = id;
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.strategyType = strategyType;
        this.status = status;
        this.createdAt = createdAt;
        this.latestVersion = latestVersion;
    }

    public static BotResponseBuilder builder() {
        return new BotResponseBuilder();
    }

    public static class BotResponseBuilder {
        private UUID id;
        private UUID sellerId;
        private String name;
        private String description;
        private String strategyType;
        private BotStatus status;
        private Instant createdAt;
        private BotVersionResponse latestVersion;

        public BotResponseBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public BotResponseBuilder sellerId(UUID sellerId) {
            this.sellerId = sellerId;
            return this;
        }

        public BotResponseBuilder name(String name) {
            this.name = name;
            return this;
        }

        public BotResponseBuilder description(String description) {
            this.description = description;
            return this;
        }

        public BotResponseBuilder strategyType(String strategyType) {
            this.strategyType = strategyType;
            return this;
        }

        public BotResponseBuilder status(BotStatus status) {
            this.status = status;
            return this;
        }

        public BotResponseBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public BotResponseBuilder latestVersion(BotVersionResponse latestVersion) {
            this.latestVersion = latestVersion;
            return this;
        }

        public BotResponse build() {
            return new BotResponse(id, sellerId, name, description, strategyType, status, createdAt, latestVersion);
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSellerId() {
        return sellerId;
    }

    public void setSellerId(UUID sellerId) {
        this.sellerId = sellerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public BotVersionResponse getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(BotVersionResponse latestVersion) {
        this.latestVersion = latestVersion;
    }
}
