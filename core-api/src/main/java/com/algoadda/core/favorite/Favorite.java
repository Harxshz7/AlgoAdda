package com.algoadda.core.favorite;

import com.algoadda.core.bot.Bot;
import com.algoadda.core.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "favorites",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_favorite_buyer_bot", columnNames = {"buyer_id", "bot_id"})
    }
)
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bot_id", nullable = false)
    private Bot bot;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Favorite() {
    }

    public Favorite(UUID id, User buyer, Bot bot, Instant createdAt) {
        this.id = id;
        this.buyer = buyer;
        this.bot = bot;
        this.createdAt = createdAt;
    }

    public static FavoriteBuilder builder() {
        return new FavoriteBuilder();
    }

    public static class FavoriteBuilder {
        private UUID id;
        private User buyer;
        private Bot bot;
        private Instant createdAt;

        public FavoriteBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public FavoriteBuilder buyer(User buyer) {
            this.buyer = buyer;
            return this;
        }

        public FavoriteBuilder bot(Bot bot) {
            this.bot = bot;
            return this;
        }

        public FavoriteBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Favorite build() {
            return new Favorite(id, buyer, bot, createdAt);
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

    public Bot getBot() {
        return bot;
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
