package com.algoadda.core.bot.dto;

import com.algoadda.core.bot.BotStatus;
import com.algoadda.core.listing.LicenseType;

import java.math.BigDecimal;
import java.util.UUID;

public class PublishResponse {
    private UUID listingId;
    private UUID botId;
    private UUID botVersionId;
    private BigDecimal price;
    private LicenseType licenseType;
    private boolean active;
    private BotStatus botStatus;

    public PublishResponse() {
    }

    public PublishResponse(UUID listingId, UUID botId, UUID botVersionId, BigDecimal price, LicenseType licenseType, boolean active, BotStatus botStatus) {
        this.listingId = listingId;
        this.botId = botId;
        this.botVersionId = botVersionId;
        this.price = price;
        this.licenseType = licenseType;
        this.active = active;
        this.botStatus = botStatus;
    }

    public static PublishResponseBuilder builder() {
        return new PublishResponseBuilder();
    }

    public static class PublishResponseBuilder {
        private UUID listingId;
        private UUID botId;
        private UUID botVersionId;
        private BigDecimal price;
        private LicenseType licenseType;
        private boolean active;
        private BotStatus botStatus;

        public PublishResponseBuilder listingId(UUID listingId) {
            this.listingId = listingId;
            return this;
        }

        public PublishResponseBuilder botId(UUID botId) {
            this.botId = botId;
            return this;
        }

        public PublishResponseBuilder botVersionId(UUID botVersionId) {
            this.botVersionId = botVersionId;
            return this;
        }

        public PublishResponseBuilder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public PublishResponseBuilder licenseType(LicenseType licenseType) {
            this.licenseType = licenseType;
            return this;
        }

        public PublishResponseBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public PublishResponseBuilder botStatus(BotStatus botStatus) {
            this.botStatus = botStatus;
            return this;
        }

        public PublishResponse build() {
            return new PublishResponse(listingId, botId, botVersionId, price, licenseType, active, botStatus);
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public BotStatus getBotStatus() {
        return botStatus;
    }

    public void setBotStatus(BotStatus botStatus) {
        this.botStatus = botStatus;
    }
}
