package com.algoadda.core.listing;

import com.algoadda.core.bot.*;
import com.algoadda.core.listing.dto.*;
import com.algoadda.core.user.SellerProfile;
import com.algoadda.core.user.SellerProfileRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@SuppressWarnings("null")
@Service
public class ListingService {

    private static final Logger log = LoggerFactory.getLogger(ListingService.class);

    private final ListingRepository listingRepository;
    private final BacktestResultRepository backtestResultRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final ObjectMapper objectMapper;

    public ListingService(
        ListingRepository listingRepository,
        BacktestResultRepository backtestResultRepository,
        SellerProfileRepository sellerProfileRepository,
        ObjectMapper objectMapper
    ) {
        this.listingRepository = listingRepository;
        this.backtestResultRepository = backtestResultRepository;
        this.sellerProfileRepository = sellerProfileRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<ListingSummaryResponse> getPublicListings(
        String q,
        String strategyType,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String sortBy,
        int page,
        int size
    ) {
        List<Listing> activeListings = listingRepository.findByActiveTrue();

        // Filter by published bot status & criteria
        List<ListingSummaryResponse> summaries = new ArrayList<>();

        for (Listing listing : activeListings) {
            BotVersion version = listing.getBotVersion();
            if (version == null) continue;
            Bot bot = version.getBot();
            if (bot == null || bot.getStatus() != BotStatus.PUBLISHED) continue;

            // Filter strategyType
            if (strategyType != null && !strategyType.isBlank()) {
                if (!bot.getStrategyType().equalsIgnoreCase(strategyType.trim())) {
                    continue;
                }
            }

            // Filter price
            if (minPrice != null && listing.getPrice().compareTo(minPrice) < 0) {
                continue;
            }
            if (maxPrice != null && listing.getPrice().compareTo(maxPrice) > 0) {
                continue;
            }

            // Filter search q (full text / substring match)
            if (q != null && !q.isBlank()) {
                String searchTerm = q.trim().toLowerCase();
                String botName = bot.getName() != null ? bot.getName().toLowerCase() : "";
                String botDesc = bot.getDescription() != null ? bot.getDescription().toLowerCase() : "";
                if (!botName.contains(searchTerm) && !botDesc.contains(searchTerm)) {
                    continue;
                }
            }

            // Fetch Backtest metrics
            BacktestResult backtest = backtestResultRepository
                .findFirstByBotVersionIdOrderByCreatedAtDesc(version.getId())
                .orElse(null);

            Double winRate = null;
            Double maxDrawdown = null;
            Double sharpeRatio = null;

            if (backtest != null && backtest.getMetrics() != null) {
                try {
                    JsonNode metricsNode = objectMapper.readTree(backtest.getMetrics());
                    if (metricsNode.isTextual()) {
                        metricsNode = objectMapper.readTree(metricsNode.asText());
                    }
                    if (metricsNode.has("win_rate")) winRate = metricsNode.get("win_rate").asDouble();
                    if (metricsNode.has("max_drawdown")) maxDrawdown = metricsNode.get("max_drawdown").asDouble();
                    if (metricsNode.has("sharpe_ratio")) sharpeRatio = metricsNode.get("sharpe_ratio").asDouble();
                } catch (Exception e) {
                    log.warn("Failed to parse backtest metrics for version {}: {}", version.getId(), e.getMessage());
                }
            }

            // Fetch Seller Profile
            SellerProfile sellerProfile = sellerProfileRepository.findByUserId(bot.getSeller().getId()).orElse(null);
            String sellerDisplayName = sellerProfile != null ? sellerProfile.getDisplayName() : bot.getSeller().getEmail();

            summaries.add(ListingSummaryResponse.builder()
                .listingId(listing.getId())
                .botId(bot.getId())
                .botVersionId(version.getId())
                .name(bot.getName())
                .description(bot.getDescription())
                .strategyType(bot.getStrategyType())
                .price(listing.getPrice())
                .licenseType(listing.getLicenseType())
                .sellerId(bot.getSeller().getId())
                .sellerDisplayName(sellerDisplayName)
                .winRate(winRate)
                .maxDrawdown(maxDrawdown)
                .sharpeRatio(sharpeRatio)
                .createdAt(listing.getCreatedAt())
                .build());
        }

        // Sorting
        if (sortBy != null && !sortBy.isBlank()) {
            switch (sortBy.toLowerCase()) {
                case "price_asc":
                case "price":
                    summaries.sort(Comparator.comparing(ListingSummaryResponse::getPrice));
                    break;
                case "price_desc":
                    summaries.sort(Comparator.comparing(ListingSummaryResponse::getPrice).reversed());
                    break;
                case "winrate":
                case "win_rate":
                    summaries.sort(Comparator.comparing(ListingSummaryResponse::getWinRate, Comparator.nullsLast(Comparator.reverseOrder())));
                    break;
                case "newest":
                default:
                    summaries.sort(Comparator.comparing(ListingSummaryResponse::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
                    break;
            }
        } else {
            summaries.sort(Comparator.comparing(ListingSummaryResponse::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        }

        // Pagination
        int totalElements = summaries.size();
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, size);
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);

        int fromIndex = Math.min(safePage * safeSize, totalElements);
        int toIndex = Math.min(fromIndex + safeSize, totalElements);

        List<ListingSummaryResponse> pageContent = summaries.subList(fromIndex, toIndex);

        return new PageResponse<>(pageContent, safePage, safeSize, totalElements, totalPages);
    }

    @Transactional(readOnly = true)
    public ListingDetailResponse getListingDetail(UUID listingId) {
        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new IllegalArgumentException("Listing not found with id: " + listingId));

        if (!listing.isActive()) {
            throw new IllegalArgumentException("Listing is no longer active");
        }

        BotVersion version = listing.getBotVersion();
        if (version == null || version.getBot() == null || version.getBot().getStatus() != BotStatus.PUBLISHED) {
            throw new IllegalArgumentException("Listing belongs to an unpublished or invalid bot");
        }

        Bot bot = version.getBot();

        // Fetch Backtest Result
        BacktestResult backtest = backtestResultRepository
            .findFirstByBotVersionIdOrderByCreatedAtDesc(version.getId())
            .orElse(null);

        // Fetch Seller Profile
        SellerProfile sellerProfile = sellerProfileRepository.findByUserId(bot.getSeller().getId()).orElse(null);

        return ListingDetailResponse.builder()
            .listingId(listing.getId())
            .botId(bot.getId())
            .botVersionId(version.getId())
            .name(bot.getName())
            .description(bot.getDescription())
            .strategyType(bot.getStrategyType())
            .price(listing.getPrice())
            .licenseType(listing.getLicenseType())
            .disclosedLogic(version.getDisclosedLogic())
            .riskDisclaimer(bot.getRiskDisclaimer())
            .sellerId(bot.getSeller().getId())
            .sellerDisplayName(sellerProfile != null ? sellerProfile.getDisplayName() : bot.getSeller().getEmail())
            .sellerBio(sellerProfile != null ? sellerProfile.getBio() : null)
            .sellerCreatedAt(sellerProfile != null ? sellerProfile.getCreatedAt() : bot.getSeller().getCreatedAt())
            .methodologyNotes(backtest != null ? backtest.getMethodologyNotes() : null)
            .metrics(backtest != null ? backtest.getMetrics() : null)
            .reportFileKey(backtest != null ? backtest.getReportFileKey() : null)
            .createdAt(listing.getCreatedAt())
            .build();
    }
}
