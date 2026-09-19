package com.algoadda.core.listing;

import com.algoadda.core.bot.BotVersion;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "listings")
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bot_version_id", nullable = false)
    private BotVersion botVersion;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "license_type", nullable = false, length = 50)
    private LicenseType licenseType;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "is_official", nullable = false)
    private boolean official = false;

    @Column(name = "billing_interval", length = 50)
    private String billingInterval;

    @Column(name = "razorpay_plan_id")
    private String razorpayPlanId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Listing() {
    }

    public Listing(UUID id, BotVersion botVersion, BigDecimal price, LicenseType licenseType, String billingInterval, String razorpayPlanId, boolean active, boolean official, Instant createdAt) {
        this.id = id;
        this.botVersion = botVersion;
        this.price = price != null ? price : BigDecimal.ZERO;
        this.licenseType = licenseType;
        this.billingInterval = billingInterval;
        this.razorpayPlanId = razorpayPlanId;
        this.active = active;
        this.official = official;
        this.createdAt = createdAt;
    }

    public static ListingBuilder builder() {
        return new ListingBuilder();
    }

    public static class ListingBuilder {
        private UUID id;
        private BotVersion botVersion;
        private BigDecimal price = BigDecimal.ZERO;
        private LicenseType licenseType;
        private String billingInterval;
        private String razorpayPlanId;
        private boolean active = true;
        private boolean official = false;
        private Instant createdAt;

        public ListingBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public ListingBuilder botVersion(BotVersion botVersion) {
            this.botVersion = botVersion;
            return this;
        }

        public ListingBuilder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public ListingBuilder licenseType(LicenseType licenseType) {
            this.licenseType = licenseType;
            return this;
        }

        public ListingBuilder billingInterval(String billingInterval) {
            this.billingInterval = billingInterval;
            return this;
        }

        public ListingBuilder razorpayPlanId(String razorpayPlanId) {
            this.razorpayPlanId = razorpayPlanId;
            return this;
        }

        public ListingBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public ListingBuilder official(boolean official) {
            this.official = official;
            return this;
        }

        public ListingBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Listing build() {
            return new Listing(id, botVersion, price, licenseType, billingInterval, razorpayPlanId, active, official, createdAt);
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BotVersion getBotVersion() {
        return botVersion;
    }

    public void setBotVersion(BotVersion botVersion) {
        this.botVersion = botVersion;
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

    public String getBillingInterval() {
        return billingInterval;
    }

    public void setBillingInterval(String billingInterval) {
        this.billingInterval = billingInterval;
    }

    public String getRazorpayPlanId() {
        return razorpayPlanId;
    }

    public void setRazorpayPlanId(String razorpayPlanId) {
        this.razorpayPlanId = razorpayPlanId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isOfficial() {
        return official;
    }

    public void setOfficial(boolean official) {
        this.official = official;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
