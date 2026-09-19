package com.algoadda.core.review.dto;

import java.util.List;
import java.util.UUID;

public class BotReviewsSummaryResponse {

    private UUID botId;
    private Double averageRating;
    private Long reviewCount;
    private List<ReviewResponse> reviews;
    private ReviewResponse userReview;
    @com.fasterxml.jackson.annotation.JsonProperty("isVerifiedBuyer")
    private boolean isVerifiedBuyer;

    public BotReviewsSummaryResponse() {
    }

    public BotReviewsSummaryResponse(UUID botId, Double averageRating, Long reviewCount, List<ReviewResponse> reviews, ReviewResponse userReview, boolean isVerifiedBuyer) {
        this.botId = botId;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
        this.reviews = reviews;
        this.userReview = userReview;
        this.isVerifiedBuyer = isVerifiedBuyer;
    }

    public static BotReviewsSummaryResponseBuilder builder() {
        return new BotReviewsSummaryResponseBuilder();
    }

    public static class BotReviewsSummaryResponseBuilder {
        private UUID botId;
        private Double averageRating;
        private Long reviewCount;
        private List<ReviewResponse> reviews;
        private ReviewResponse userReview;
        private boolean isVerifiedBuyer;

        public BotReviewsSummaryResponseBuilder botId(UUID botId) {
            this.botId = botId;
            return this;
        }

        public BotReviewsSummaryResponseBuilder averageRating(Double averageRating) {
            this.averageRating = averageRating;
            return this;
        }

        public BotReviewsSummaryResponseBuilder reviewCount(Long reviewCount) {
            this.reviewCount = reviewCount;
            return this;
        }

        public BotReviewsSummaryResponseBuilder reviews(List<ReviewResponse> reviews) {
            this.reviews = reviews;
            return this;
        }

        public BotReviewsSummaryResponseBuilder userReview(ReviewResponse userReview) {
            this.userReview = userReview;
            return this;
        }

        public BotReviewsSummaryResponseBuilder isVerifiedBuyer(boolean isVerifiedBuyer) {
            this.isVerifiedBuyer = isVerifiedBuyer;
            return this;
        }

        public BotReviewsSummaryResponse build() {
            return new BotReviewsSummaryResponse(botId, averageRating, reviewCount, reviews, userReview, isVerifiedBuyer);
        }
    }

    public UUID getBotId() {
        return botId;
    }

    public void setBotId(UUID botId) {
        this.botId = botId;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Long getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Long reviewCount) {
        this.reviewCount = reviewCount;
    }

    public List<ReviewResponse> getReviews() {
        return reviews;
    }

    public void setReviews(List<ReviewResponse> reviews) {
        this.reviews = reviews;
    }

    public ReviewResponse getUserReview() {
        return userReview;
    }

    public void setUserReview(ReviewResponse userReview) {
        this.userReview = userReview;
    }

    public boolean isVerifiedBuyer() {
        return isVerifiedBuyer;
    }

    public void setVerifiedBuyer(boolean verifiedBuyer) {
        isVerifiedBuyer = verifiedBuyer;
    }
}
