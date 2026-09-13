package com.algoadda.core.listing.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class PublicSellerProfileResponse {

    private UUID sellerId;
    private String displayName;
    private String bio;
    private Instant createdAt;
    private List<ListingSummaryResponse> activeListings;

    public PublicSellerProfileResponse() {
    }

    public PublicSellerProfileResponse(UUID sellerId, String displayName, String bio, Instant createdAt, List<ListingSummaryResponse> activeListings) {
        this.sellerId = sellerId;
        this.displayName = displayName;
        this.bio = bio;
        this.createdAt = createdAt;
        this.activeListings = activeListings;
    }

    public static PublicSellerProfileResponseBuilder builder() {
        return new PublicSellerProfileResponseBuilder();
    }

    public static class PublicSellerProfileResponseBuilder {
        private UUID sellerId;
        private String displayName;
        private String bio;
        private Instant createdAt;
        private List<ListingSummaryResponse> activeListings;

        public PublicSellerProfileResponseBuilder sellerId(UUID sellerId) {
            this.sellerId = sellerId;
            return this;
        }

        public PublicSellerProfileResponseBuilder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public PublicSellerProfileResponseBuilder bio(String bio) {
            this.bio = bio;
            return this;
        }

        public PublicSellerProfileResponseBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public PublicSellerProfileResponseBuilder activeListings(List<ListingSummaryResponse> activeListings) {
            this.activeListings = activeListings;
            return this;
        }

        public PublicSellerProfileResponse build() {
            return new PublicSellerProfileResponse(sellerId, displayName, bio, createdAt, activeListings);
        }
    }

    public UUID getSellerId() {
        return sellerId;
    }

    public void setSellerId(UUID sellerId) {
        this.sellerId = sellerId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<ListingSummaryResponse> getActiveListings() {
        return activeListings;
    }

    public void setActiveListings(List<ListingSummaryResponse> activeListings) {
        this.activeListings = activeListings;
    }
}
