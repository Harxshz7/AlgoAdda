package com.algoadda.core.report;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/sellers")
public class AdminSellerController {

    private final ReportService reportService;

    public AdminSellerController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/{sellerId}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> suspendSeller(@PathVariable UUID sellerId) {
        reportService.suspendSeller(sellerId);
        return ResponseEntity.ok(Map.of(
            "sellerId", sellerId,
            "action", "SUSPEND",
            "success", true
        ));
    }

    @PostMapping("/{sellerId}/unsuspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> unsuspendSeller(@PathVariable UUID sellerId) {
        reportService.unsuspendSeller(sellerId);
        return ResponseEntity.ok(Map.of(
            "sellerId", sellerId,
            "action", "UNSUSPEND",
            "success", true
        ));
    }
}
