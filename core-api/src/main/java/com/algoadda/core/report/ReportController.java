package com.algoadda.core.report;

import com.algoadda.core.config.UserPrincipal;
import com.algoadda.core.report.dto.CreateReportRequest;
import com.algoadda.core.report.dto.ListingReportResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/listings")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/{listingId}/report")
    public ResponseEntity<ListingReportResponse> reportListing(
        @PathVariable UUID listingId,
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestBody CreateReportRequest request
    ) {
        // /api/listings/** is permitAll in SecurityConfig, so we must check auth manually
        if (principal == null) {
            throw new AccessDeniedException("You must be logged in to report a listing");
        }

        ListingReportResponse response = reportService.createReport(listingId, principal.getId(), request);
        return ResponseEntity.ok(response);
    }
}
