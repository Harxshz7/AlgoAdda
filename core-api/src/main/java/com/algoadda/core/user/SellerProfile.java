package com.algoadda.core.user;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "seller_profiles")
public class SellerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false, length = 50)
    private KycStatus kycStatus = KycStatus.NOT_STARTED;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public SellerProfile() {
    }

    public SellerProfile(UUID id, User user, String displayName, String bio, KycStatus kycStatus, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.user = user;
        this.displayName = displayName;
        this.bio = bio;
        this.kycStatus = kycStatus != null ? kycStatus : KycStatus.NOT_STARTED;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SellerProfileBuilder builder() {
        return new SellerProfileBuilder();
    }

    public static class SellerProfileBuilder {
        private UUID id;
        private User user;
        private String displayName;
        private String bio;
        private KycStatus kycStatus = KycStatus.NOT_STARTED;
        private Instant createdAt;
        private Instant updatedAt;

        public SellerProfileBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public SellerProfileBuilder user(User user) {
            this.user = user;
            return this;
        }

        public SellerProfileBuilder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public SellerProfileBuilder bio(String bio) {
            this.bio = bio;
            return this;
        }

        public SellerProfileBuilder kycStatus(KycStatus kycStatus) {
            this.kycStatus = kycStatus;
            return this;
        }

        public SellerProfileBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public SellerProfileBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public SellerProfile build() {
            return new SellerProfile(id, user, displayName, bio, kycStatus, createdAt, updatedAt);
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
