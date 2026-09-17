package com.algoadda.core.report;

import com.algoadda.core.report.dto.ListingReportResponse;
import com.algoadda.core.report.dto.ResolveReportRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    private final ReportService reportService;

    public AdminReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/queue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ListingReportResponse>> getReportQueue() {
        List<ListingReportResponse> queue = reportService.getOpenReportsQueue();
        return ResponseEntity.ok(queue);
    }

    @PostMapping("/{reportId}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ListingReportResponse> resolveReport(
        @PathVariable UUID reportId,
        @RequestBody ResolveReportRequest request
    ) {
        ListingReportResponse response = reportService.resolveReport(reportId, request);
        return ResponseEntity.ok(response);
    }
}
