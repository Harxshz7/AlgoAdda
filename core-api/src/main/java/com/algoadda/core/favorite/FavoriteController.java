package com.algoadda.core.favorite;

import com.algoadda.core.config.UserPrincipal;
import com.algoadda.core.listing.dto.ListingSummaryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/bots/{botId}/favorite")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<Map<String, Object>> addFavorite(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable UUID botId
    ) {
        favoriteService.addFavorite(principal.getId(), botId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "favorited", true,
            "message", "Strategy added to watchlist"
        ));
    }

    @DeleteMapping("/bots/{botId}/favorite")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<Map<String, Object>> removeFavorite(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable UUID botId
    ) {
        favoriteService.removeFavorite(principal.getId(), botId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "favorited", false,
            "message", "Strategy removed from watchlist"
        ));
    }

    @GetMapping("/buyers/me/favorites")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<List<ListingSummaryResponse>> getBuyerFavorites(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<ListingSummaryResponse> favorites = favoriteService.getBuyerFavorites(principal.getId());
        return ResponseEntity.ok(favorites);
    }
}
