package com.algoadda.core.order;

import com.algoadda.core.bot.BotVersion;
import com.algoadda.core.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "licenses")
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bot_version_id", nullable = false)
    private BotVersion botVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @CreationTimestamp
    @Column(name = "issued_at", nullable = false, updatable = false)
    private Instant issuedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    public License() {
    }

    public License(UUID id, Order order, BotVersion botVersion, User buyer, Instant issuedAt, Instant expiresAt) {
        this.id = id;
        this.order = order;
        this.botVersion = botVersion;
        this.buyer = buyer;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    public static LicenseBuilder builder() {
        return new LicenseBuilder();
    }

    public static class LicenseBuilder {
        private UUID id;
        private Order order;
        private BotVersion botVersion;
        private User buyer;
        private Instant issuedAt;
        private Instant expiresAt;

        public LicenseBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public LicenseBuilder order(Order order) {
            this.order = order;
            return this;
        }

        public LicenseBuilder botVersion(BotVersion botVersion) {
            this.botVersion = botVersion;
            return this;
        }

        public LicenseBuilder buyer(User buyer) {
            this.buyer = buyer;
            return this;
        }

        public LicenseBuilder issuedAt(Instant issuedAt) {
            this.issuedAt = issuedAt;
            return this;
        }

        public LicenseBuilder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public License build() {
            return new License(id, order, botVersion, buyer, issuedAt, expiresAt);
        }
    }

    public boolean isPerpetual() {
        return this.expiresAt == null;
    }

    public boolean isActive() {
        return isPerpetual() || Instant.now().isBefore(this.expiresAt);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public BotVersion getBotVersion() {
        return botVersion;
    }

    public void setBotVersion(BotVersion botVersion) {
        this.botVersion = botVersion;
    }

    public User getBuyer() {
        return buyer;
    }

    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
