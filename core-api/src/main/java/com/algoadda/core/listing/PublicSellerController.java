package com.algoadda.core.listing;

import com.algoadda.core.listing.dto.PublicSellerProfileResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/sellers")
public class PublicSellerController {

    private final PublicSellerService publicSellerService;

    public PublicSellerController(PublicSellerService publicSellerService) {
        this.publicSellerService = publicSellerService;
    }

    @GetMapping("/{sellerId}")
    public ResponseEntity<PublicSellerProfileResponse> getPublicSellerProfile(@PathVariable UUID sellerId) {
        PublicSellerProfileResponse response = publicSellerService.getPublicSellerProfile(sellerId);
        return ResponseEntity.ok(response);
    }
}
