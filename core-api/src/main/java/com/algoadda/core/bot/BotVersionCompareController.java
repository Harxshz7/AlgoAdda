package com.algoadda.core.bot;

import com.algoadda.core.bot.dto.VersionComparisonResponse;
import com.algoadda.core.config.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/bots")
public class BotVersionCompareController {

    private final BotVersionCompareService compareService;

    public BotVersionCompareController(BotVersionCompareService compareService) {
        this.compareService = compareService;
    }

    @GetMapping("/{botId}/versions/compare")
    public ResponseEntity<VersionComparisonResponse> compareVersions(
        @PathVariable UUID botId,
        @RequestParam("from") UUID fromVersionId,
        @RequestParam("to") UUID toVersionId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        String userEmail = principal != null ? principal.getEmail() : null;
        VersionComparisonResponse response = compareService.compareVersions(botId, fromVersionId, toVersionId, userEmail);
        return ResponseEntity.ok(response);
    }
}
