package com.algoadda.core.review;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/reviews")
public class AdminReviewController {

    private final ReviewService reviewService;

    public AdminReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteReview(@PathVariable UUID reviewId) {
        reviewService.deleteReviewByAdmin(reviewId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Review deleted successfully"
        ));
    }
}
