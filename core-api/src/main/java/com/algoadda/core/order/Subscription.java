package com.algoadda.core.order;

import com.algoadda.core.listing.Listing;
import com.algoadda.core.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @Column(name = "razorpay_subscription_id")
    private String razorpaySubscriptionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Column(name = "current_period_end")
    private Instant currentPeriodEnd;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Subscription() {
    }

    public Subscription(UUID id, User buyer, Listing listing, String razorpaySubscriptionId, SubscriptionStatus status, Instant currentPeriodEnd, Instant createdAt) {
        this.id = id;
        this.buyer = buyer;
        this.listing = listing;
        this.razorpaySubscriptionId = razorpaySubscriptionId;
        this.status = status != null ? status : SubscriptionStatus.ACTIVE;
        this.currentPeriodEnd = currentPeriodEnd;
        this.createdAt = createdAt;
    }

    public static SubscriptionBuilder builder() {
        return new SubscriptionBuilder();
    }

    public static class SubscriptionBuilder {
        private UUID id;
        private User buyer;
        private Listing listing;
        private String razorpaySubscriptionId;
        private SubscriptionStatus status = SubscriptionStatus.ACTIVE;
        private Instant currentPeriodEnd;
        private Instant createdAt;

        public SubscriptionBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public SubscriptionBuilder buyer(User buyer) {
            this.buyer = buyer;
            return this;
        }

        public SubscriptionBuilder listing(Listing listing) {
            this.listing = listing;
            return this;
        }

        public SubscriptionBuilder razorpaySubscriptionId(String razorpaySubscriptionId) {
            this.razorpaySubscriptionId = razorpaySubscriptionId;
            return this;
        }

        public SubscriptionBuilder status(SubscriptionStatus status) {
            this.status = status;
            return this;
        }

        public SubscriptionBuilder currentPeriodEnd(Instant currentPeriodEnd) {
            this.currentPeriodEnd = currentPeriodEnd;
            return this;
        }

        public SubscriptionBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Subscription build() {
            return new Subscription(id, buyer, listing, razorpaySubscriptionId, status, currentPeriodEnd, createdAt);
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getBuyer() {
        return buyer;
    }

    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }

    public Listing getListing() {
        return listing;
    }

    public void setListing(Listing listing) {
        this.listing = listing;
    }

    public String getRazorpaySubscriptionId() {
        return razorpaySubscriptionId;
    }

    public void setRazorpaySubscriptionId(String razorpaySubscriptionId) {
        this.razorpaySubscriptionId = razorpaySubscriptionId;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public Instant getCurrentPeriodEnd() {
        return currentPeriodEnd;
    }

    public void setCurrentPeriodEnd(Instant currentPeriodEnd) {
        this.currentPeriodEnd = currentPeriodEnd;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
