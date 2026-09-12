package com.algoadda.core.bot.dto;

import com.algoadda.core.bot.BacktestStatus;

import java.time.Instant;
import java.util.UUID;

public class BotVersionResponse {

    private UUID id;
    private UUID botId;
    private String versionNumber;
    private String disclosedLogic;
    private String fileStorageKey;
    private String changelog;
    private BacktestStatus backtestStatus;
    private Instant createdAt;

    public BotVersionResponse() {
    }

    public BotVersionResponse(UUID id, UUID botId, String versionNumber, String disclosedLogic, String fileStorageKey, String changelog, BacktestStatus backtestStatus, Instant createdAt) {
        this.id = id;
        this.botId = botId;
        this.versionNumber = versionNumber;
        this.disclosedLogic = disclosedLogic;
        this.fileStorageKey = fileStorageKey;
        this.changelog = changelog;
        this.backtestStatus = backtestStatus;
        this.createdAt = createdAt;
    }

    public static BotVersionResponseBuilder builder() {
        return new BotVersionResponseBuilder();
    }

    public static class BotVersionResponseBuilder {
        private UUID id;
        private UUID botId;
        private String versionNumber;
        private String disclosedLogic;
        private String fileStorageKey;
        private String changelog;
        private BacktestStatus backtestStatus;
        private Instant createdAt;

        public BotVersionResponseBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public BotVersionResponseBuilder botId(UUID botId) {
            this.botId = botId;
            return this;
        }

        public BotVersionResponseBuilder versionNumber(String versionNumber) {
            this.versionNumber = versionNumber;
            return this;
        }

        public BotVersionResponseBuilder disclosedLogic(String disclosedLogic) {
            this.disclosedLogic = disclosedLogic;
            return this;
        }

        public BotVersionResponseBuilder fileStorageKey(String fileStorageKey) {
            this.fileStorageKey = fileStorageKey;
            return this;
        }

        public BotVersionResponseBuilder changelog(String changelog) {
            this.changelog = changelog;
            return this;
        }

        public BotVersionResponseBuilder backtestStatus(BacktestStatus backtestStatus) {
            this.backtestStatus = backtestStatus;
            return this;
        }

        public BotVersionResponseBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public BotVersionResponse build() {
            return new BotVersionResponse(id, botId, versionNumber, disclosedLogic, fileStorageKey, changelog, backtestStatus, createdAt);
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getBotId() {
        return botId;
    }

    public void setBotId(UUID botId) {
        this.botId = botId;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getDisclosedLogic() {
        return disclosedLogic;
    }

    public void setDisclosedLogic(String disclosedLogic) {
        this.disclosedLogic = disclosedLogic;
    }

    public String getFileStorageKey() {
        return fileStorageKey;
    }

    public void setFileStorageKey(String fileStorageKey) {
        this.fileStorageKey = fileStorageKey;
    }

    public String getChangelog() {
        return changelog;
    }

    public void setChangelog(String changelog) {
        this.changelog = changelog;
    }

    public BacktestStatus getBacktestStatus() {
        return backtestStatus;
    }

    public void setBacktestStatus(BacktestStatus backtestStatus) {
        this.backtestStatus = backtestStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
