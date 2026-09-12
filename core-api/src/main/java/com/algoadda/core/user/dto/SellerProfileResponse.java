package com.algoadda.core.user.dto;

import com.algoadda.core.user.KycStatus;

import java.time.Instant;
import java.util.UUID;

public class SellerProfileResponse {

    private UUID id;
    private UUID userId;
    private String displayName;
    private String bio;
    private KycStatus kycStatus;
    private Instant createdAt;
    private Instant updatedAt;

    public SellerProfileResponse() {
    }

    public SellerProfileResponse(UUID id, UUID userId, String displayName, String bio, KycStatus kycStatus, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.displayName = displayName;
        this.bio = bio;
        this.kycStatus = kycStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SellerProfileResponseBuilder builder() {
        return new SellerProfileResponseBuilder();
    }

    public static class SellerProfileResponseBuilder {
        private UUID id;
        private UUID userId;
        private String displayName;
        private String bio;
        private KycStatus kycStatus;
        private Instant createdAt;
        private Instant updatedAt;

        public SellerProfileResponseBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public SellerProfileResponseBuilder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public SellerProfileResponseBuilder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public SellerProfileResponseBuilder bio(String bio) {
            this.bio = bio;
            return this;
        }

        public SellerProfileResponseBuilder kycStatus(KycStatus kycStatus) {
            this.kycStatus = kycStatus;
            return this;
        }

        public SellerProfileResponseBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public SellerProfileResponseBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public SellerProfileResponse build() {
            return new SellerProfileResponse(id, userId, displayName, bio, kycStatus, createdAt, updatedAt);
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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

    public KycStatus getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(KycStatus kycStatus) {
        this.kycStatus = kycStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
