package com.algoadda.core.listing.dto;

import com.algoadda.core.bot.RiskLabel;
import com.algoadda.core.listing.LicenseType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class ListingDetailResponse {

    private UUID listingId;
    private UUID botId;
    private UUID botVersionId;
    private String name;
    private String description;
    private String strategyType;
    private BigDecimal price;
    private LicenseType licenseType;
    private String disclosedLogic;
    private String riskDisclaimer;
    private UUID sellerId;
    private String sellerDisplayName;
    private String sellerBio;
    private Instant sellerCreatedAt;
    private String methodologyNotes;
    private String metrics;
    private RiskLabel riskLabel;
    private String reportFileKey;
    private boolean isOfficial;
    private Double averageRating;
    private Long reviewCount;
    private Boolean isFavorited;
    private Instant createdAt;

    public ListingDetailResponse() {
    }

    public ListingDetailResponse(
        UUID listingId,
        UUID botId,
        UUID botVersionId,
        String name,
        String description,
        String strategyType,
        BigDecimal price,
        LicenseType licenseType,
        String disclosedLogic,
        String riskDisclaimer,
        UUID sellerId,
        String sellerDisplayName,
        String sellerBio,
        Instant sellerCreatedAt,
        String methodologyNotes,
        String metrics,
        RiskLabel riskLabel,
        String reportFileKey,
        boolean isOfficial,
        Double averageRating,
        Long reviewCount,
        Boolean isFavorited,
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
        this.disclosedLogic = disclosedLogic;
        this.riskDisclaimer = riskDisclaimer;
        this.sellerId = sellerId;
        this.sellerDisplayName = sellerDisplayName;
        this.sellerBio = sellerBio;
        this.sellerCreatedAt = sellerCreatedAt;
        this.methodologyNotes = methodologyNotes;
        this.metrics = metrics;
        this.riskLabel = riskLabel;
        this.reportFileKey = reportFileKey;
        this.isOfficial = isOfficial;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
        this.isFavorited = isFavorited;
        this.createdAt = createdAt;
    }

    public static ListingDetailResponseBuilder builder() {
        return new ListingDetailResponseBuilder();
    }

    public static class ListingDetailResponseBuilder {
        private UUID listingId;
        private UUID botId;
        private UUID botVersionId;
        private String name;
        private String description;
        private String strategyType;
        private BigDecimal price;
        private LicenseType licenseType;
        private String disclosedLogic;
        private String riskDisclaimer;
        private UUID sellerId;
        private String sellerDisplayName;
        private String sellerBio;
        private Instant sellerCreatedAt;
        private String methodologyNotes;
        private String metrics;
        private RiskLabel riskLabel;
        private String reportFileKey;
        private boolean isOfficial;
        private Double averageRating;
        private Long reviewCount;
        private Boolean isFavorited;
        private Instant createdAt;

        public ListingDetailResponseBuilder listingId(UUID listingId) {
            this.listingId = listingId;
            return this;
        }

        public ListingDetailResponseBuilder botId(UUID botId) {
            this.botId = botId;
            return this;
        }

        public ListingDetailResponseBuilder botVersionId(UUID botVersionId) {
            this.botVersionId = botVersionId;
            return this;
        }

        public ListingDetailResponseBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ListingDetailResponseBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ListingDetailResponseBuilder strategyType(String strategyType) {
            this.strategyType = strategyType;
            return this;
        }

        public ListingDetailResponseBuilder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public ListingDetailResponseBuilder licenseType(LicenseType licenseType) {
            this.licenseType = licenseType;
            return this;
        }

        public ListingDetailResponseBuilder disclosedLogic(String disclosedLogic) {
            this.disclosedLogic = disclosedLogic;
            return this;
        }

        public ListingDetailResponseBuilder riskDisclaimer(String riskDisclaimer) {
            this.riskDisclaimer = riskDisclaimer;
            return this;
        }

        public ListingDetailResponseBuilder sellerId(UUID sellerId) {
            this.sellerId = sellerId;
            return this;
        }

        public ListingDetailResponseBuilder sellerDisplayName(String sellerDisplayName) {
            this.sellerDisplayName = sellerDisplayName;
            return this;
        }

        public ListingDetailResponseBuilder sellerBio(String sellerBio) {
            this.sellerBio = sellerBio;
            return this;
        }

        public ListingDetailResponseBuilder sellerCreatedAt(Instant sellerCreatedAt) {
            this.sellerCreatedAt = sellerCreatedAt;
            return this;
        }

        public ListingDetailResponseBuilder methodologyNotes(String methodologyNotes) {
            this.methodologyNotes = methodologyNotes;
            return this;
        }

        public ListingDetailResponseBuilder metrics(String metrics) {
            this.metrics = metrics;
            return this;
        }

        public ListingDetailResponseBuilder riskLabel(RiskLabel riskLabel) {
            this.riskLabel = riskLabel;
            return this;
        }

        public ListingDetailResponseBuilder reportFileKey(String reportFileKey) {
            this.reportFileKey = reportFileKey;
            return this;
        }

        public ListingDetailResponseBuilder isOfficial(boolean isOfficial) {
            this.isOfficial = isOfficial;
            return this;
        }

        public ListingDetailResponseBuilder averageRating(Double averageRating) {
            this.averageRating = averageRating;
            return this;
        }

        public ListingDetailResponseBuilder reviewCount(Long reviewCount) {
            this.reviewCount = reviewCount;
            return this;
        }

        public ListingDetailResponseBuilder isFavorited(Boolean isFavorited) {
            this.isFavorited = isFavorited;
            return this;
        }

        public ListingDetailResponseBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ListingDetailResponse build() {
            return new ListingDetailResponse(
                listingId, botId, botVersionId, name, description, strategyType,
                price, licenseType, disclosedLogic, riskDisclaimer, sellerId, sellerDisplayName,
                sellerBio, sellerCreatedAt, methodologyNotes, metrics, riskLabel, reportFileKey, isOfficial,
                averageRating, reviewCount, isFavorited, createdAt
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

    public String getDisclosedLogic() {
        return disclosedLogic;
    }

    public void setDisclosedLogic(String disclosedLogic) {
        this.disclosedLogic = disclosedLogic;
    }

    public String getRiskDisclaimer() {
        return riskDisclaimer;
    }

    public void setRiskDisclaimer(String riskDisclaimer) {
        this.riskDisclaimer = riskDisclaimer;
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

    public String getSellerBio() {
        return sellerBio;
    }

    public void setSellerBio(String sellerBio) {
        this.sellerBio = sellerBio;
    }

    public Instant getSellerCreatedAt() {
        return sellerCreatedAt;
    }

    public void setSellerCreatedAt(Instant sellerCreatedAt) {
        this.sellerCreatedAt = sellerCreatedAt;
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

    public RiskLabel getRiskLabel() {
        return riskLabel;
    }

    public void setRiskLabel(RiskLabel riskLabel) {
        this.riskLabel = riskLabel;
    }

    public String getReportFileKey() {
        return reportFileKey;
    }

    public void setReportFileKey(String reportFileKey) {
        this.reportFileKey = reportFileKey;
    }

    public boolean isOfficial() {
        return isOfficial;
    }

    public void setOfficial(boolean official) {
        this.isOfficial = official;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Long getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Long reviewCount) {
        this.reviewCount = reviewCount;
    }

    public Boolean getIsFavorited() {
        return isFavorited;
    }

    public Boolean isFavorited() {
        return isFavorited;
    }

    public void setIsFavorited(Boolean isFavorited) {
        this.isFavorited = isFavorited;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
