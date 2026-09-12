package com.algoadda.core.config;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class PingController {

    @GetMapping("/buyer/ping")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<Map<String, Object>> buyerPing(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(Map.of(
            "status", "ok",
            "role", "BUYER",
            "userId", principal != null ? principal.getId().toString() : "unknown",
            "message", "Buyer access granted"
        ));
    }

    @GetMapping("/seller/ping")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Map<String, Object>> sellerPing(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(Map.of(
            "status", "ok",
            "role", "SELLER",
            "userId", principal != null ? principal.getId().toString() : "unknown",
            "message", "Seller access granted"
        ));
    }

    @GetMapping("/admin/ping")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> adminPing(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(Map.of(
            "status", "ok",
            "role", "ADMIN",
            "userId", principal != null ? principal.getId().toString() : "unknown",
            "message", "Admin access granted"
        ));
    }
}
