package com.algoadda.core.bot.dto;

import java.time.Instant;

public class BotUploadRequest {

    private String name;
    private String description;
    private String strategyType;
    private String disclosedLogic;
    private String strategyConfig;
    private Instant dateRangeStart;
    private Instant dateRangeEnd;

    public BotUploadRequest() {
    }

    public BotUploadRequest(String name, String description, String strategyType, String disclosedLogic, String strategyConfig, Instant dateRangeStart, Instant dateRangeEnd) {
        this.name = name;
        this.description = description;
        this.strategyType = strategyType;
        this.disclosedLogic = disclosedLogic;
        this.strategyConfig = strategyConfig;
        this.dateRangeStart = dateRangeStart;
        this.dateRangeEnd = dateRangeEnd;
    }

    public static BotUploadRequestBuilder builder() {
        return new BotUploadRequestBuilder();
    }

    public static class BotUploadRequestBuilder {
        private String name;
        private String description;
        private String strategyType;
        private String disclosedLogic;
        private String strategyConfig;
        private Instant dateRangeStart;
        private Instant dateRangeEnd;

        public BotUploadRequestBuilder name(String name) {
            this.name = name;
            return this;
        }

        public BotUploadRequestBuilder description(String description) {
            this.description = description;
            return this;
        }

        public BotUploadRequestBuilder strategyType(String strategyType) {
            this.strategyType = strategyType;
            return this;
        }

        public BotUploadRequestBuilder disclosedLogic(String disclosedLogic) {
            this.disclosedLogic = disclosedLogic;
            return this;
        }

        public BotUploadRequestBuilder strategyConfig(String strategyConfig) {
            this.strategyConfig = strategyConfig;
            return this;
        }

        public BotUploadRequestBuilder dateRangeStart(Instant dateRangeStart) {
            this.dateRangeStart = dateRangeStart;
            return this;
        }

        public BotUploadRequestBuilder dateRangeEnd(Instant dateRangeEnd) {
            this.dateRangeEnd = dateRangeEnd;
            return this;
        }

        public BotUploadRequest build() {
            return new BotUploadRequest(name, description, strategyType, disclosedLogic, strategyConfig, dateRangeStart, dateRangeEnd);
        }
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

    public String getDisclosedLogic() {
        return disclosedLogic;
    }

    public void setDisclosedLogic(String disclosedLogic) {
        this.disclosedLogic = disclosedLogic;
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
