package com.algoadda.core.user;

import com.algoadda.core.config.UserPrincipal;
import com.algoadda.core.user.dto.SellerOnboardRequest;
import com.algoadda.core.user.dto.SellerProfileResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sellers")
public class SellerController {

    private final SellerService sellerService;

    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @PostMapping("/onboard")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<SellerProfileResponse> onboard(
        @Valid @RequestBody SellerOnboardRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        SellerProfileResponse response = sellerService.onboardSeller(principal.getId(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<SellerProfileResponse> getProfile(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        SellerProfileResponse response = sellerService.getSellerProfile(principal.getId());
        return ResponseEntity.ok(response);
    }
}
