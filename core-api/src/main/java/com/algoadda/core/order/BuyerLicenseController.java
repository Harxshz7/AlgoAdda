package com.algoadda.core.order;

import com.algoadda.core.config.UserPrincipal;
import com.algoadda.core.order.dto.BuyerLicenseResponse;
import com.algoadda.core.order.dto.DownloadLicenseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class BuyerLicenseController {

    private final LicenseService licenseService;

    public BuyerLicenseController(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @GetMapping("/api/buyers/me/licenses")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<List<BuyerLicenseResponse>> getBuyerLicenses(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<BuyerLicenseResponse> response = licenseService.getBuyerLicenses(principal.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/licenses/{licenseId}/download")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<?> downloadLicense(
        @PathVariable UUID licenseId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        try {
            DownloadLicenseResponse response = licenseService.generateDownloadUrl(principal.getId(), licenseId);
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Forbidden", "message", e.getMessage()));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Download failed", "message", e.getMessage()));
        }
    }
}
