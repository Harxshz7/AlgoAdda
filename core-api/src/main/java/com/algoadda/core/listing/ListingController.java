package com.algoadda.core.listing;

import com.algoadda.core.listing.dto.ListingDetailResponse;
import com.algoadda.core.listing.dto.ListingSummaryResponse;
import com.algoadda.core.listing.dto.PageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<ListingSummaryResponse>> getPublicListings(
        @RequestParam(value = "q", required = false) String q,
        @RequestParam(value = "strategyType", required = false) String strategyType,
        @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
        @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
        @RequestParam(value = "sortBy", required = false, defaultValue = "newest") String sortBy,
        @RequestParam(value = "page", required = false, defaultValue = "0") int page,
        @RequestParam(value = "size", required = false, defaultValue = "12") int size
    ) {
        PageResponse<ListingSummaryResponse> response = listingService.getPublicListings(
            q, strategyType, minPrice, maxPrice, sortBy, page, size
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{listingId}")
    public ResponseEntity<ListingDetailResponse> getListingDetail(@PathVariable UUID listingId) {
        ListingDetailResponse response = listingService.getListingDetail(listingId);
        return ResponseEntity.ok(response);
    }
}
