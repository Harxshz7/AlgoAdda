package com.algoadda.core.bot;

import com.algoadda.core.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bots")
public class Bot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "strategy_type", nullable = false, length = 100)
    private String strategyType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BotStatus status = BotStatus.DRAFT;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Bot() {
    }

    public Bot(UUID id, User seller, String name, String description, String strategyType, BotStatus status, Instant createdAt) {
        this.id = id;
        this.seller = seller;
        this.name = name;
        this.description = description;
        this.strategyType = strategyType;
        this.status = status != null ? status : BotStatus.DRAFT;
        this.createdAt = createdAt;
    }

    public static BotBuilder builder() {
        return new BotBuilder();
    }

    public static class BotBuilder {
        private UUID id;
        private User seller;
        private String name;
        private String description;
        private String strategyType;
        private BotStatus status = BotStatus.DRAFT;
        private Instant createdAt;

        public BotBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public BotBuilder seller(User seller) {
            this.seller = seller;
            return this;
        }

        public BotBuilder name(String name) {
            this.name = name;
            return this;
        }

        public BotBuilder description(String description) {
            this.description = description;
            return this;
        }

        public BotBuilder strategyType(String strategyType) {
            this.strategyType = strategyType;
            return this;
        }

        public BotBuilder status(BotStatus status) {
            this.status = status;
            return this;
        }

        public BotBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Bot build() {
            return new Bot(id, seller, name, description, strategyType, status, createdAt);
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
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

    public BotStatus getStatus() {
        return status;
    }

    public void setStatus(BotStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
