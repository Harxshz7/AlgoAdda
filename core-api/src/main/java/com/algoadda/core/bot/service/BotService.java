package com.algoadda.core.bot.service;

import com.algoadda.core.bot.*;
import com.algoadda.core.bot.dto.*;
import com.algoadda.core.compliance.ComplianceService;
import com.algoadda.core.compliance.dto.ComplianceCheckResponse;
import com.algoadda.core.listing.LicenseType;
import com.algoadda.core.listing.Listing;
import com.algoadda.core.listing.ListingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("null")
@Service
public class BotService {

    private static final Logger log = LoggerFactory.getLogger(BotService.class);

    @Value("${algoadda.official-seller-email:official@algoadda.com}")
    private String officialSellerEmail;

    private final BotRepository botRepository;
    private final BotVersionRepository botVersionRepository;
    private final BacktestResultRepository backtestResultRepository;
    private final BacktestJobRepository backtestJobRepository;
    private final ListingRepository listingRepository;
    private final com.algoadda.core.user.UserRepository userRepository;
    private final S3StorageService s3StorageService;
    private final ComplianceService complianceService;

    public BotService(
        BotRepository botRepository,
        BotVersionRepository botVersionRepository,
        BacktestResultRepository backtestResultRepository,
        BacktestJobRepository backtestJobRepository,
        ListingRepository listingRepository,
        com.algoadda.core.user.UserRepository userRepository,
        S3StorageService s3StorageService,
        @Lazy ComplianceService complianceService
    ) {
        this.botRepository = botRepository;
        this.botVersionRepository = botVersionRepository;
        this.backtestResultRepository = backtestResultRepository;
        this.backtestJobRepository = backtestJobRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.s3StorageService = s3StorageService;
        this.complianceService = complianceService;
    }

    @Transactional
    public BotResponse createBot(UUID sellerId, BotUploadRequest request, MultipartFile file) {
        validateDisclosedLogic(request.getDisclosedLogic());

        com.algoadda.core.user.User seller = userRepository.findById(sellerId)
            .orElseThrow(() -> new IllegalArgumentException("Seller not found with id: " + sellerId));

        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Bot name is required");
        }
        if (request.getStrategyType() == null || request.getStrategyType().isBlank()) {
            throw new IllegalArgumentException("Strategy type is required");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Strategy file is required for upload");
        }

        Bot bot = Bot.builder()
            .seller(seller)
            .name(request.getName().trim())
            .description(request.getDescription())
            .riskDisclaimer(request.getRiskDisclaimer())
            .strategyType(request.getStrategyType().trim().toUpperCase())
            .status(BotStatus.DRAFT)
            .build();

        Bot savedBot = botRepository.save(bot);

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "strategy.py";
        String s3Key = "bots/" + savedBot.getId() + "/v1.0.0/" + originalFilename;

        try {
            s3StorageService.uploadFile(s3Key, file.getBytes(), file.getContentType());
        } catch (IOException e) {
            log.error("Failed to read uploaded file content for bot {}: {}", savedBot.getId(), e.getMessage());
            throw new IllegalStateException("Failed to read strategy file content: " + e.getMessage(), e);
        }

        BotVersion initialVersion = BotVersion.builder()
            .bot(savedBot)
            .versionNumber("1.0.0")
            .disclosedLogic(request.getDisclosedLogic().trim())
            .fileStorageKey(s3Key)
            .changelog("Initial bot version release")
            .backtestStatus(BacktestStatus.QUEUED)
            .build();

        BotVersion savedVersion = botVersionRepository.save(initialVersion);

        BacktestJob job = BacktestJob.builder()
            .botVersion(savedVersion)
            .status(BacktestJobStatus.QUEUED)
            .strategyConfig(request.getStrategyConfig())
            .dateRangeStart(request.getDateRangeStart())
            .dateRangeEnd(request.getDateRangeEnd())
            .build();
        backtestJobRepository.save(job);

        return mapToBotResponse(savedBot, savedVersion);
    }

    @Transactional
    public BotVersionResponse createBotVersion(UUID sellerId, UUID botId, BotVersionUploadRequest request, MultipartFile file) {
        validateDisclosedLogic(request.getDisclosedLogic());

        Bot bot = botRepository.findById(botId)
            .orElseThrow(() -> new IllegalArgumentException("Bot not found with id: " + botId));

        if (!bot.getSeller().getId().equals(sellerId)) {
            throw new IllegalArgumentException("Unauthorized: Bot belongs to another seller");
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Strategy file is required for upload");
        }

        List<BotVersion> existingVersions = botVersionRepository.findByBotId(botId);
        int nextMinor = existingVersions.size();
        String nextVersionNumber = "1." + nextMinor + ".0";

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "strategy.py";
        String s3Key = "bots/" + botId + "/v" + nextVersionNumber + "/" + originalFilename;

        try {
            s3StorageService.uploadFile(s3Key, file.getBytes(), file.getContentType());
        } catch (IOException e) {
            log.error("Failed to read uploaded file content for version {}: {}", nextVersionNumber, e.getMessage());
            throw new IllegalStateException("Failed to read strategy file content: " + e.getMessage(), e);
        }

        BotVersion newVersion = BotVersion.builder()
            .bot(bot)
            .versionNumber(nextVersionNumber)
            .disclosedLogic(request.getDisclosedLogic().trim())
            .fileStorageKey(s3Key)
            .changelog(request.getChangelog())
            .backtestStatus(BacktestStatus.QUEUED)
            .build();

        BotVersion savedVersion = botVersionRepository.save(newVersion);

        BacktestJob versionJob = BacktestJob.builder()
            .botVersion(savedVersion)
            .status(BacktestJobStatus.QUEUED)
            .strategyConfig(request.getStrategyConfig())
            .dateRangeStart(request.getDateRangeStart())
            .dateRangeEnd(request.getDateRangeEnd())
            .build();
        backtestJobRepository.save(versionJob);

        return mapToBotVersionResponse(savedVersion);
    }

    @Transactional
    public PublishResponse publishBotVersion(UUID sellerId, UUID botId, UUID versionId, PublishRequest request) {
        Bot bot = botRepository.findById(botId)
            .orElseThrow(() -> new IllegalArgumentException("Bot not found with id: " + botId));

        if (!bot.getSeller().getId().equals(sellerId)) {
            throw new IllegalArgumentException("Unauthorized: Bot belongs to another seller");
        }

        BotVersion version = botVersionRepository.findById(versionId)
            .orElseThrow(() -> new IllegalArgumentException("Bot version not found with id: " + versionId));

        if (!version.getBot().getId().equals(botId)) {
            throw new IllegalArgumentException("Version " + versionId + " does not belong to bot " + botId);
        }

        ComplianceCheckResponse latestCheck = complianceService.getLatestComplianceCheck(versionId)
            .orElseThrow(() -> new IllegalStateException("Publish blocked: No compliance check found for version " + versionId));

        if (!latestCheck.isPassed()) {
            throw new IllegalStateException("Publish blocked: Compliance check failed. Results: " + latestCheck.getChecklistResults());
        }

        if (request == null || request.getPrice() == null) {
            throw new IllegalArgumentException("Listing price is required to publish a bot version");
        }

        boolean isOfficial = bot.getSeller() != null
            && bot.getSeller().getEmail() != null
            && bot.getSeller().getEmail().equalsIgnoreCase(officialSellerEmail);

        Listing listing = Listing.builder()
            .botVersion(version)
            .price(request.getPrice())
            .licenseType(request.getLicenseType() != null ? request.getLicenseType() : LicenseType.ONE_TIME)
            .official(isOfficial)
            .active(true)
            .build();

        Listing savedListing = listingRepository.save(listing);

        bot.setStatus(BotStatus.PUBLISHED);
        botRepository.save(bot);

        return PublishResponse.builder()
            .listingId(savedListing.getId())
            .botId(botId)
            .botVersionId(versionId)
            .price(savedListing.getPrice())
            .licenseType(savedListing.getLicenseType())
            .active(savedListing.isActive())
            .isOfficial(savedListing.isOfficial())
            .botStatus(bot.getStatus())
            .build();
    }

    @Transactional(readOnly = true)
    public List<SellerDashboardBotDto> getSellerDashboardBots(UUID sellerId) {
        List<Bot> bots = botRepository.findBySellerId(sellerId);
        List<SellerDashboardBotDto> result = new ArrayList<>();

        for (Bot bot : bots) {
            BotVersion latestVersion = botVersionRepository.findFirstByBotIdOrderByCreatedAtDesc(bot.getId()).orElse(null);
            ComplianceCheckResponse latestCheck = latestVersion != null 
                ? complianceService.getLatestComplianceCheck(latestVersion.getId()).orElse(null) 
                : null;

            result.add(SellerDashboardBotDto.builder()
                .botId(bot.getId())
                .name(bot.getName())
                .strategyType(bot.getStrategyType())
                .status(bot.getStatus())
                .latestVersionId(latestVersion != null ? latestVersion.getId() : null)
                .latestVersionNumber(latestVersion != null ? latestVersion.getVersionNumber() : null)
                .backtestStatus(latestVersion != null ? latestVersion.getBacktestStatus() : null)
                .latestComplianceCheck(latestCheck)
                .createdAt(bot.getCreatedAt())
                .build());
        }

        return result;
    }

    @Transactional(readOnly = true)
    public BotResponse getBot(UUID botId) {
        Bot bot = botRepository.findById(botId)
            .orElseThrow(() -> new IllegalArgumentException("Bot not found with id: " + botId));
        BotVersion latestVersion = botVersionRepository.findFirstByBotIdOrderByCreatedAtDesc(botId).orElse(null);
        return mapToBotResponse(bot, latestVersion);
    }

    @Transactional(readOnly = true)
    public BacktestResultResponse getBacktestResult(UUID botId, UUID versionId) {
        BotVersion version = botVersionRepository.findById(versionId)
            .orElseThrow(() -> new IllegalArgumentException("Bot version not found with id: " + versionId));

        if (!version.getBot().getId().equals(botId)) {
            throw new IllegalArgumentException("Version " + versionId + " does not belong to bot " + botId);
        }

        BacktestResult backtestResult = backtestResultRepository.findFirstByBotVersionIdOrderByCreatedAtDesc(versionId)
            .orElseThrow(() -> new IllegalArgumentException("No backtest results found for version: " + versionId));

        RiskLabel riskLabel = backtestResult.getRiskLabel() != null
            ? backtestResult.getRiskLabel()
            : RiskClassifier.classify(backtestResult.getMetrics());

        return BacktestResultResponse.builder()
            .id(backtestResult.getId())
            .botVersionId(versionId)
            .dateRangeStart(backtestResult.getDateRangeStart())
            .dateRangeEnd(backtestResult.getDateRangeEnd())
            .methodologyNotes(backtestResult.getMethodologyNotes())
            .metrics(backtestResult.getMetrics())
            .riskLabel(riskLabel)
            .reportFileKey(backtestResult.getReportFileKey())
            .createdAt(backtestResult.getCreatedAt())
            .build();
    }

    private void validateDisclosedLogic(String disclosedLogic) {
        if (disclosedLogic == null || disclosedLogic.trim().isEmpty()) {
            throw new IllegalArgumentException("Disclosed logic is required for all listings under AlgoAdda's White Box compliance model");
        }
    }

    private BotResponse mapToBotResponse(Bot bot, BotVersion version) {
        return BotResponse.builder()
            .id(bot.getId())
            .sellerId(bot.getSeller().getId())
            .name(bot.getName())
            .description(bot.getDescription())
            .riskDisclaimer(bot.getRiskDisclaimer())
            .strategyType(bot.getStrategyType())
            .status(bot.getStatus())
            .createdAt(bot.getCreatedAt())
            .latestVersion(version != null ? mapToBotVersionResponse(version) : null)
            .build();
    }

    private BotVersionResponse mapToBotVersionResponse(BotVersion version) {
        ComplianceCheckResponse latestCheck = complianceService.getLatestComplianceCheck(version.getId()).orElse(null);
        return BotVersionResponse.builder()
            .id(version.getId())
            .botId(version.getBot().getId())
            .versionNumber(version.getVersionNumber())
            .disclosedLogic(version.getDisclosedLogic())
            .fileStorageKey(version.getFileStorageKey())
            .changelog(version.getChangelog())
            .backtestStatus(version.getBacktestStatus())
            .createdAt(version.getCreatedAt())
            .latestComplianceCheck(latestCheck)
            .build();
    }
}
