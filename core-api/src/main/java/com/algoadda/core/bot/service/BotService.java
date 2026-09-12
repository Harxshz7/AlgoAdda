package com.algoadda.core.bot.service;

import com.algoadda.core.bot.*;
import com.algoadda.core.bot.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final BotRepository botRepository;
    private final BotVersionRepository botVersionRepository;
    private final BacktestResultRepository backtestResultRepository;
    private final com.algoadda.core.user.UserRepository userRepository;
    private final S3StorageService s3StorageService;
    private final BacktestServiceClient backtestServiceClient;

    public BotService(
        BotRepository botRepository,
        BotVersionRepository botVersionRepository,
        BacktestResultRepository backtestResultRepository,
        com.algoadda.core.user.UserRepository userRepository,
        S3StorageService s3StorageService,
        BacktestServiceClient backtestServiceClient
    ) {
        this.botRepository = botRepository;
        this.botVersionRepository = botVersionRepository;
        this.backtestResultRepository = backtestResultRepository;
        this.userRepository = userRepository;
        this.s3StorageService = s3StorageService;
        this.backtestServiceClient = backtestServiceClient;
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
            .backtestStatus(BacktestStatus.PENDING)
            .build();

        BotVersion savedVersion = botVersionRepository.save(initialVersion);

        // Run backtest asynchronously or inline with graceful failure handling
        backtestServiceClient.runBacktest(
            savedVersion,
            request.getStrategyConfig(),
            request.getDateRangeStart(),
            request.getDateRangeEnd()
        );

        // Re-read version for updated backtest status
        BotVersion updatedVersion = botVersionRepository.findById(savedVersion.getId()).orElse(savedVersion);

        return mapToBotResponse(savedBot, updatedVersion);
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
            .backtestStatus(BacktestStatus.PENDING)
            .build();

        BotVersion savedVersion = botVersionRepository.save(newVersion);

        backtestServiceClient.runBacktest(
            savedVersion,
            request.getStrategyConfig(),
            request.getDateRangeStart(),
            request.getDateRangeEnd()
        );

        BotVersion updatedVersion = botVersionRepository.findById(savedVersion.getId()).orElse(savedVersion);
        return mapToBotVersionResponse(updatedVersion);
    }

    @Transactional(readOnly = true)
    public List<SellerDashboardBotDto> getSellerDashboardBots(UUID sellerId) {
        List<Bot> bots = botRepository.findBySellerId(sellerId);
        List<SellerDashboardBotDto> result = new ArrayList<>();

        for (Bot bot : bots) {
            BotVersion latestVersion = botVersionRepository.findFirstByBotIdOrderByCreatedAtDesc(bot.getId()).orElse(null);
            result.add(SellerDashboardBotDto.builder()
                .botId(bot.getId())
                .name(bot.getName())
                .strategyType(bot.getStrategyType())
                .status(bot.getStatus())
                .latestVersionId(latestVersion != null ? latestVersion.getId() : null)
                .latestVersionNumber(latestVersion != null ? latestVersion.getVersionNumber() : null)
                .backtestStatus(latestVersion != null ? latestVersion.getBacktestStatus() : null)
                .createdAt(bot.getCreatedAt())
                .build());
        }

        return result;
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

        return BacktestResultResponse.builder()
            .id(backtestResult.getId())
            .botVersionId(versionId)
            .dateRangeStart(backtestResult.getDateRangeStart())
            .dateRangeEnd(backtestResult.getDateRangeEnd())
            .methodologyNotes(backtestResult.getMethodologyNotes())
            .metrics(backtestResult.getMetrics())
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
            .strategyType(bot.getStrategyType())
            .status(bot.getStatus())
            .createdAt(bot.getCreatedAt())
            .latestVersion(version != null ? mapToBotVersionResponse(version) : null)
            .build();
    }

    private BotVersionResponse mapToBotVersionResponse(BotVersion version) {
        return BotVersionResponse.builder()
            .id(version.getId())
            .botId(version.getBot().getId())
            .versionNumber(version.getVersionNumber())
            .disclosedLogic(version.getDisclosedLogic())
            .fileStorageKey(version.getFileStorageKey())
            .changelog(version.getChangelog())
            .backtestStatus(version.getBacktestStatus())
            .createdAt(version.getCreatedAt())
            .build();
    }
}
