package com.algoadda.core.listing.dto;

import com.algoadda.core.bot.RiskLabel;
import com.algoadda.core.listing.LicenseType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class ListingSummaryResponse {

    private UUID listingId;
    private UUID botId;
    private UUID botVersionId;
    private String name;
    private String description;
    private String strategyType;
    private BigDecimal price;
    private LicenseType licenseType;
    private UUID sellerId;
    private String sellerDisplayName;
    private Double winRate;
    private Double maxDrawdown;
    private Double sharpeRatio;
    private RiskLabel riskLabel;
    private boolean isOfficial;
    private Instant createdAt;

    public ListingSummaryResponse() {
    }

    public ListingSummaryResponse(
        UUID listingId,
        UUID botId,
        UUID botVersionId,
        String name,
        String description,
        String strategyType,
        BigDecimal price,
        LicenseType licenseType,
        UUID sellerId,
        String sellerDisplayName,
        Double winRate,
        Double maxDrawdown,
        Double sharpeRatio,
        RiskLabel riskLabel,
        boolean isOfficial,
        Instant createdAt
    ) {
        this.listingId = listingId;
        this.botId = botId;
        this.botVersionId = botVersionId;
        this.name = name;
        this.description = description;
        this.strategyType = strategyType;
        this.price = price;
        this.licenseType = licenseType;
        this.sellerId = sellerId;
        this.sellerDisplayName = sellerDisplayName;
        this.winRate = winRate;
        this.maxDrawdown = maxDrawdown;
        this.sharpeRatio = sharpeRatio;
        this.riskLabel = riskLabel;
        this.isOfficial = isOfficial;
        this.createdAt = createdAt;
    }

    public static ListingSummaryResponseBuilder builder() {
        return new ListingSummaryResponseBuilder();
    }

    public static class ListingSummaryResponseBuilder {
        private UUID listingId;
        private UUID botId;
        private UUID botVersionId;
        private String name;
        private String description;
        private String strategyType;
        private BigDecimal price;
        private LicenseType licenseType;
        private UUID sellerId;
        private String sellerDisplayName;
        private Double winRate;
        private Double maxDrawdown;
        private Double sharpeRatio;
        private RiskLabel riskLabel;
        private boolean isOfficial;
        private Instant createdAt;

        public ListingSummaryResponseBuilder listingId(UUID listingId) {
            this.listingId = listingId;
            return this;
        }

        public ListingSummaryResponseBuilder botId(UUID botId) {
            this.botId = botId;
            return this;
        }

        public ListingSummaryResponseBuilder botVersionId(UUID botVersionId) {
            this.botVersionId = botVersionId;
            return this;
        }

        public ListingSummaryResponseBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ListingSummaryResponseBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ListingSummaryResponseBuilder strategyType(String strategyType) {
            this.strategyType = strategyType;
            return this;
        }

        public ListingSummaryResponseBuilder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public ListingSummaryResponseBuilder licenseType(LicenseType licenseType) {
            this.licenseType = licenseType;
            return this;
        }

        public ListingSummaryResponseBuilder sellerId(UUID sellerId) {
            this.sellerId = sellerId;
            return this;
        }

        public ListingSummaryResponseBuilder sellerDisplayName(String sellerDisplayName) {
            this.sellerDisplayName = sellerDisplayName;
            return this;
        }

        public ListingSummaryResponseBuilder winRate(Double winRate) {
            this.winRate = winRate;
            return this;
        }

        public ListingSummaryResponseBuilder maxDrawdown(Double maxDrawdown) {
            this.maxDrawdown = maxDrawdown;
            return this;
        }

        public ListingSummaryResponseBuilder sharpeRatio(Double sharpeRatio) {
            this.sharpeRatio = sharpeRatio;
            return this;
        }

        public ListingSummaryResponseBuilder riskLabel(RiskLabel riskLabel) {
            this.riskLabel = riskLabel;
            return this;
        }

        public ListingSummaryResponseBuilder isOfficial(boolean isOfficial) {
            this.isOfficial = isOfficial;
            return this;
        }

        public ListingSummaryResponseBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ListingSummaryResponse build() {
            return new ListingSummaryResponse(
                listingId, botId, botVersionId, name, description, strategyType,
                price, licenseType, sellerId, sellerDisplayName, winRate, maxDrawdown,
                sharpeRatio, riskLabel, isOfficial, createdAt
            );
        }
    }

    public UUID getListingId() {
        return listingId;
    }

    public void setListingId(UUID listingId) {
        this.listingId = listingId;
    }

    public UUID getBotId() {
        return botId;
    }

    public void setBotId(UUID botId) {
        this.botId = botId;
    }

    public UUID getBotVersionId() {
        return botVersionId;
    }

    public void setBotVersionId(UUID botVersionId) {
        this.botVersionId = botVersionId;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LicenseType getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(LicenseType licenseType) {
        this.licenseType = licenseType;
    }

    public UUID getSellerId() {
        return sellerId;
    }

    public void setSellerId(UUID sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerDisplayName() {
        return sellerDisplayName;
    }

    public void setSellerDisplayName(String sellerDisplayName) {
        this.sellerDisplayName = sellerDisplayName;
    }

    public Double getWinRate() {
        return winRate;
    }

    public void setWinRate(Double winRate) {
        this.winRate = winRate;
    }

    public Double getMaxDrawdown() {
        return maxDrawdown;
    }

    public void setMaxDrawdown(Double maxDrawdown) {
        this.maxDrawdown = maxDrawdown;
    }

    public Double getSharpeRatio() {
        return sharpeRatio;
    }

    public void setSharpeRatio(Double sharpeRatio) {
        this.sharpeRatio = sharpeRatio;
    }

    public RiskLabel getRiskLabel() {
        return riskLabel;
    }

    public void setRiskLabel(RiskLabel riskLabel) {
        this.riskLabel = riskLabel;
    }

    public boolean isOfficial() {
        return isOfficial;
    }

    public void setOfficial(boolean official) {
        isOfficial = official;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
