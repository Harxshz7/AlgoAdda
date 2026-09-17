package com.algoadda.core.report;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/listings")
public class AdminListingController {

    private final ReportService reportService;

    public AdminListingController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/{listingId}/force-delist")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> forceDelistListing(@PathVariable UUID listingId) {
        reportService.forceDelistListing(listingId);
        return ResponseEntity.ok(Map.of(
            "listingId", listingId,
            "action", "FORCE_DELIST",
            "success", true
        ));
    }
}
