package com.algoadda.core.compliance;

import com.algoadda.core.compliance.dto.AdminReviewRequest;
import com.algoadda.core.compliance.dto.ComplianceCheckResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/compliance")
public class AdminComplianceController {

    private final ComplianceService complianceService;

    public AdminComplianceController(ComplianceService complianceService) {
        this.complianceService = complianceService;
    }

    @GetMapping("/queue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ComplianceCheckResponse>> getComplianceQueue() {
        List<ComplianceCheckResponse> queue = complianceService.getAdminReviewQueue();
        return ResponseEntity.ok(queue);
    }

    @PostMapping("/{botVersionId}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComplianceCheckResponse> reviewCompliance(
        @PathVariable UUID botVersionId,
        @RequestBody AdminReviewRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException("Review decision request body is required");
        }
        ComplianceCheckResponse response = complianceService.runManualReview(
            botVersionId,
            request.isPassed(),
            request.getNotes()
        );
        return ResponseEntity.ok(response);
    }
}
