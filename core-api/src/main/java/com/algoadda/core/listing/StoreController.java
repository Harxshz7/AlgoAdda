package com.algoadda.core.listing;

import com.algoadda.core.listing.dto.ListingDetailResponse;
import com.algoadda.core.listing.dto.ListingSummaryResponse;
import com.algoadda.core.listing.dto.PageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/store")
public class StoreController {

    private final ListingService listingService;

    public StoreController(ListingService listingService) {
        this.listingService = listingService;
    }

    @GetMapping("/listings")
    public ResponseEntity<PageResponse<ListingSummaryResponse>> getOfficialStoreListings(
        @RequestParam(value = "q", required = false) String q,
        @RequestParam(value = "strategyType", required = false) String strategyType,
        @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
        @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
        @RequestParam(value = "sortBy", required = false, defaultValue = "newest") String sortBy,
        @RequestParam(value = "page", required = false, defaultValue = "0") int page,
        @RequestParam(value = "size", required = false, defaultValue = "12") int size
    ) {
        PageResponse<ListingSummaryResponse> response = listingService.getPublicListings(
            q, strategyType, minPrice, maxPrice, sortBy, page, size, true
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/listings/{listingId}")
    public ResponseEntity<ListingDetailResponse> getOfficialStoreListingDetail(@PathVariable UUID listingId) {
        ListingDetailResponse response = listingService.getListingDetail(listingId, true);
        return ResponseEntity.ok(response);
    }
}
