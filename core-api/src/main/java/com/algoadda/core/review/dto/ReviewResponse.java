package com.algoadda.core.review.dto;

import java.time.Instant;
import java.util.UUID;

public class ReviewResponse {

    private UUID id;
    private UUID botId;
    private UUID buyerId;
    private String buyerDisplayName;
    private int rating;
    private String comment;
    private Instant createdAt;
    private Instant updatedAt;

    public ReviewResponse() {
    }

    public ReviewResponse(UUID id, UUID botId, UUID buyerId, String buyerDisplayName, int rating, String comment, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.botId = botId;
        this.buyerId = buyerId;
        this.buyerDisplayName = buyerDisplayName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ReviewResponseBuilder builder() {
        return new ReviewResponseBuilder();
    }

    public static class ReviewResponseBuilder {
        private UUID id;
        private UUID botId;
        private UUID buyerId;
        private String buyerDisplayName;
        private int rating;
        private String comment;
        private Instant createdAt;
        private Instant updatedAt;

        public ReviewResponseBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public ReviewResponseBuilder botId(UUID botId) {
            this.botId = botId;
            return this;
        }

        public ReviewResponseBuilder buyerId(UUID buyerId) {
            this.buyerId = buyerId;
            return this;
        }

        public ReviewResponseBuilder buyerDisplayName(String buyerDisplayName) {
            this.buyerDisplayName = buyerDisplayName;
            return this;
        }

        public ReviewResponseBuilder rating(int rating) {
            this.rating = rating;
            return this;
        }

        public ReviewResponseBuilder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public ReviewResponseBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ReviewResponseBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public ReviewResponse build() {
            return new ReviewResponse(id, botId, buyerId, buyerDisplayName, rating, comment, createdAt, updatedAt);
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getBotId() {
        return botId;
    }

    public void setBotId(UUID botId) {
        this.botId = botId;
    }

    public UUID getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(UUID buyerId) {
        this.buyerId = buyerId;
    }

    public String getBuyerDisplayName() {
        return buyerDisplayName;
    }

    public void setBuyerDisplayName(String buyerDisplayName) {
        this.buyerDisplayName = buyerDisplayName;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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
