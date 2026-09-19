package com.algoadda.core.review;

import com.algoadda.core.bot.Bot;
import com.algoadda.core.bot.BotRepository;
import com.algoadda.core.order.LicenseRepository;
import com.algoadda.core.review.dto.BotReviewsSummaryResponse;
import com.algoadda.core.review.dto.ReviewRequest;
import com.algoadda.core.review.dto.ReviewResponse;
import com.algoadda.core.user.User;
import com.algoadda.core.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final BotRepository botRepository;
    private final UserRepository userRepository;
    private final LicenseRepository licenseRepository;

    public ReviewService(
        ReviewRepository reviewRepository,
        BotRepository botRepository,
        UserRepository userRepository,
        LicenseRepository licenseRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.botRepository = botRepository;
        this.userRepository = userRepository;
        this.licenseRepository = licenseRepository;
    }

    @Transactional
    public ReviewResponse createOrUpdateReview(UUID buyerId, UUID botId, ReviewRequest request) {
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        User buyer = userRepository.findById(buyerId)
            .orElseThrow(() -> new NoSuchElementException("User not found with id: " + buyerId));

        Bot bot = botRepository.findById(botId)
            .orElseThrow(() -> new NoSuchElementException("Bot not found with id: " + botId));

        // Verified buyer check: must own at least one valid, non-revoked license for this bot
        boolean hasActiveLicense = licenseRepository.existsByBuyerIdAndBotVersion_Bot_IdAndRevokedFalse(buyerId, botId);
        if (!hasActiveLicense) {
            throw new IllegalArgumentException("Only verified buyers who own a valid license for this bot can submit reviews");
        }

        Review review = reviewRepository.findByBotIdAndBuyerId(botId, buyerId)
            .map(existing -> {
                existing.setRating(request.getRating());
                existing.setComment(request.getComment() != null ? request.getComment().trim() : null);
                log.info("Updated existing review for buyer {} on bot {}", buyerId, botId);
                return existing;
            })
            .orElseGet(() -> {
                log.info("Created new review for buyer {} on bot {}", buyerId, botId);
                return Review.builder()
                    .bot(bot)
                    .buyer(buyer)
                    .rating(request.getRating())
                    .comment(request.getComment() != null ? request.getComment().trim() : null)
                    .build();
            });

        Review saved = reviewRepository.save(review);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public BotReviewsSummaryResponse getBotReviews(UUID botId, UUID currentUserId, Pageable pageable) {
        if (!botRepository.existsById(botId)) {
            throw new NoSuchElementException("Bot not found with id: " + botId);
        }

        Double rawAvg = reviewRepository.getAverageRatingByBotId(botId);
        double roundedAvg = rawAvg != null ? Math.round(rawAvg * 10.0) / 10.0 : 0.0;
        long totalCount = reviewRepository.countByBotId(botId);

        Page<Review> reviewPage = reviewRepository.findByBotIdOrderByCreatedAtDesc(botId, pageable);
        List<ReviewResponse> reviews = reviewPage.getContent().stream()
            .map(this::mapToResponse)
            .toList();

        ReviewResponse userReview = null;
        boolean isVerifiedBuyer = false;

        if (currentUserId != null) {
            isVerifiedBuyer = licenseRepository.existsByBuyerIdAndBotVersion_Bot_IdAndRevokedFalse(currentUserId, botId);
            userReview = reviewRepository.findByBotIdAndBuyerId(botId, currentUserId)
                .map(this::mapToResponse)
                .orElse(null);
        }

        return BotReviewsSummaryResponse.builder()
            .botId(botId)
            .averageRating(roundedAvg)
            .reviewCount(totalCount)
            .reviews(reviews)
            .userReview(userReview)
            .isVerifiedBuyer(isVerifiedBuyer)
            .build();
    }

    @Transactional
    public void deleteReviewByAdmin(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new NoSuchElementException("Review not found with id: " + reviewId));

        reviewRepository.delete(review);
        log.info("Admin deleted review {}", reviewId);
    }

    public ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
            .id(review.getId())
            .botId(review.getBot() != null ? review.getBot().getId() : null)
            .buyerId(review.getBuyer() != null ? review.getBuyer().getId() : null)
            .buyerDisplayName(formatBuyerDisplayName(review.getBuyer()))
            .rating(review.getRating())
            .comment(review.getComment())
            .createdAt(review.getCreatedAt())
            .updatedAt(review.getUpdatedAt())
            .build();
    }

    private String formatBuyerDisplayName(User buyer) {
        if (buyer == null) return "Verified Buyer";
        String email = buyer.getEmail();
        if (email == null || !email.contains("@")) return "Verified Buyer";
        String local = email.substring(0, email.indexOf("@"));
        if (local.length() <= 2) return "Verified Buyer (" + local + "***)";
        return "Verified Buyer (" + local.substring(0, 2) + "***" + local.charAt(local.length() - 1) + ")";
    }
}
