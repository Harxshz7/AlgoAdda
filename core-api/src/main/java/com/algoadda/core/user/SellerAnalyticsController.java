package com.algoadda.core.user;

import com.algoadda.core.config.UserPrincipal;
import com.algoadda.core.user.dto.SellerAnalyticsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sellers")
@SuppressWarnings("null")
public class SellerAnalyticsController {

    private final SellerAnalyticsService sellerAnalyticsService;
    private final UserRepository userRepository;

    public SellerAnalyticsController(
        SellerAnalyticsService sellerAnalyticsService,
        UserRepository userRepository
    ) {
        this.sellerAnalyticsService = sellerAnalyticsService;
        this.userRepository = userRepository;
    }

    @GetMapping("/me/analytics")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<SellerAnalyticsResponse> getSellerAnalytics(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        User seller = userRepository.findById(principal.getId())
            .orElseThrow(() -> new java.util.NoSuchElementException("Seller not found"));

        SellerAnalyticsResponse response = sellerAnalyticsService.getSellerAnalytics(seller);
        return ResponseEntity.ok(response);
    }
}
