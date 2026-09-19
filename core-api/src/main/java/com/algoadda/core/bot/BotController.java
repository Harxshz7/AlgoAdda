package com.algoadda.core.bot;

import com.algoadda.core.bot.dto.*;
import com.algoadda.core.bot.service.BotService;
import com.algoadda.core.config.UserPrincipal;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class BotController {

    private final BotService botService;

    public BotController(BotService botService) {
        this.botService = botService;
    }

    @PostMapping(value = "/bots", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<BotResponse> createBot(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam("name") String name,
        @RequestParam(value = "description", required = false) String description,
        @RequestParam(value = "riskDisclaimer", required = false) String riskDisclaimer,
        @RequestParam("strategyType") String strategyType,
        @RequestParam("disclosedLogic") String disclosedLogic,
        @RequestParam(value = "strategyConfig", required = false) String strategyConfig,
        @RequestParam(value = "dateRangeStart", required = false) Instant dateRangeStart,
        @RequestParam(value = "dateRangeEnd", required = false) Instant dateRangeEnd,
        @RequestPart("file") MultipartFile file
    ) {
        BotUploadRequest request = BotUploadRequest.builder()
            .name(name)
            .description(description)
            .riskDisclaimer(riskDisclaimer)
            .strategyType(strategyType)
            .disclosedLogic(disclosedLogic)
            .strategyConfig(strategyConfig)
            .dateRangeStart(dateRangeStart)
            .dateRangeEnd(dateRangeEnd)
            .build();

        BotResponse response = botService.createBot(principal.getId(), request, file);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/bots/{botId}/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<BotVersionResponse> createBotVersion(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable UUID botId,
        @RequestParam("disclosedLogic") String disclosedLogic,
        @RequestParam(value = "changelog", required = false) String changelog,
        @RequestParam(value = "strategyConfig", required = false) String strategyConfig,
        @RequestParam(value = "dateRangeStart", required = false) Instant dateRangeStart,
        @RequestParam(value = "dateRangeEnd", required = false) Instant dateRangeEnd,
        @RequestPart("file") MultipartFile file
    ) {
        BotVersionUploadRequest request = BotVersionUploadRequest.builder()
            .disclosedLogic(disclosedLogic)
            .changelog(changelog)
            .strategyConfig(strategyConfig)
            .dateRangeStart(dateRangeStart)
            .dateRangeEnd(dateRangeEnd)
            .build();

        BotVersionResponse response = botService.createBotVersion(principal.getId(), botId, request, file);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/bots/{botId}/versions/{versionId}/publish")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<PublishResponse> publishBotVersion(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable UUID botId,
        @PathVariable UUID versionId,
        @RequestBody PublishRequest request
    ) {
        PublishResponse response = botService.publishBotVersion(principal.getId(), botId, versionId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sellers/me/bots")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<SellerDashboardBotDto>> getSellerDashboardBots(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<SellerDashboardBotDto> bots = botService.getSellerDashboardBots(principal.getId());
        return ResponseEntity.ok(bots);
    }

    @GetMapping("/bots/{botId}")
    public ResponseEntity<BotResponse> getBot(@PathVariable UUID botId) {
        BotResponse response = botService.getBot(botId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/bots/{botId}/versions")
    public ResponseEntity<List<BotVersionResponse>> getBotVersions(@PathVariable UUID botId) {
        List<BotVersionResponse> responses = botService.getBotVersions(botId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/bots/{botId}/versions/{versionId}/backtest")
    public ResponseEntity<BacktestResultResponse> getBacktestResult(
        @PathVariable UUID botId,
        @PathVariable UUID versionId
    ) {
        BacktestResultResponse response = botService.getBacktestResult(botId, versionId);
        return ResponseEntity.ok(response);
    }
}
