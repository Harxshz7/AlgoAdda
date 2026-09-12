package com.algoadda.core.bot.dto;

import java.time.Instant;

public class BotVersionUploadRequest {

    private String disclosedLogic;
    private String changelog;
    private String strategyConfig;
    private Instant dateRangeStart;
    private Instant dateRangeEnd;

    public BotVersionUploadRequest() {
    }

    public BotVersionUploadRequest(String disclosedLogic, String changelog, String strategyConfig, Instant dateRangeStart, Instant dateRangeEnd) {
        this.disclosedLogic = disclosedLogic;
        this.changelog = changelog;
        this.strategyConfig = strategyConfig;
        this.dateRangeStart = dateRangeStart;
        this.dateRangeEnd = dateRangeEnd;
    }

    public static BotVersionUploadRequestBuilder builder() {
        return new BotVersionUploadRequestBuilder();
    }

    public static class BotVersionUploadRequestBuilder {
        private String disclosedLogic;
        private String changelog;
        private String strategyConfig;
        private Instant dateRangeStart;
        private Instant dateRangeEnd;

        public BotVersionUploadRequestBuilder disclosedLogic(String disclosedLogic) {
            this.disclosedLogic = disclosedLogic;
            return this;
        }

        public BotVersionUploadRequestBuilder changelog(String changelog) {
            this.changelog = changelog;
            return this;
        }

        public BotVersionUploadRequestBuilder strategyConfig(String strategyConfig) {
            this.strategyConfig = strategyConfig;
            return this;
        }

        public BotVersionUploadRequestBuilder dateRangeStart(Instant dateRangeStart) {
            this.dateRangeStart = dateRangeStart;
            return this;
        }

        public BotVersionUploadRequestBuilder dateRangeEnd(Instant dateRangeEnd) {
            this.dateRangeEnd = dateRangeEnd;
            return this;
        }

        public BotVersionUploadRequest build() {
            return new BotVersionUploadRequest(disclosedLogic, changelog, strategyConfig, dateRangeStart, dateRangeEnd);
        }
    }

    public String getDisclosedLogic() {
        return disclosedLogic;
    }

    public void setDisclosedLogic(String disclosedLogic) {
        this.disclosedLogic = disclosedLogic;
    }

    public String getChangelog() {
        return changelog;
    }

    public void setChangelog(String changelog) {
        this.changelog = changelog;
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
}
