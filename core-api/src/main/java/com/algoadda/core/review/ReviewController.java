package com.algoadda.core.review;

import com.algoadda.core.config.UserPrincipal;
import com.algoadda.core.review.dto.BotReviewsSummaryResponse;
import com.algoadda.core.review.dto.ReviewRequest;
import com.algoadda.core.review.dto.ReviewResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/bots/{botId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ReviewResponse> createReview(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable UUID botId,
        @Valid @RequestBody ReviewRequest request
    ) {
        ReviewResponse response = reviewService.createOrUpdateReview(principal.getId(), botId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ReviewResponse> updateReview(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable UUID botId,
        @Valid @RequestBody ReviewRequest request
    ) {
        ReviewResponse response = reviewService.createOrUpdateReview(principal.getId(), botId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<BotReviewsSummaryResponse> getBotReviews(
        @PathVariable UUID botId,
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        UUID currentUserId = principal != null ? principal.getId() : null;
        BotReviewsSummaryResponse response = reviewService.getBotReviews(botId, currentUserId, pageable);
        return ResponseEntity.ok(response);
    }
}
