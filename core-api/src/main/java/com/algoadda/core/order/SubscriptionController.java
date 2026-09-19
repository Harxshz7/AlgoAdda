package com.algoadda.core.order;

import com.algoadda.core.config.UserPrincipal;
import com.algoadda.core.order.dto.CreateSubscriptionRequest;
import com.algoadda.core.order.dto.SubscriptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<?> createSubscription(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestBody CreateSubscriptionRequest request
    ) {
        try {
            SubscriptionResponse response = subscriptionService.createSubscription(principal.getId(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Subscription creation failed", "message", e.getMessage()));
        }
    }

    @PostMapping("/{subscriptionId}/cancel")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<?> cancelSubscription(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable UUID subscriptionId
    ) {
        try {
            SubscriptionResponse response = subscriptionService.cancelSubscription(principal.getId(), subscriptionId);
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Forbidden", "message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Cancellation failed", "message", e.getMessage()));
        }
    }
}
