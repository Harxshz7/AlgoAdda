package com.algoadda.core.listing;

import com.algoadda.core.bot.Bot;
import com.algoadda.core.bot.BotStatus;
import com.algoadda.core.bot.BotVersion;
import com.algoadda.core.bot.BacktestResult;
import com.algoadda.core.bot.BacktestResultRepository;
import com.algoadda.core.listing.dto.ListingSummaryResponse;
import com.algoadda.core.listing.dto.PublicSellerProfileResponse;
import com.algoadda.core.user.SellerProfile;
import com.algoadda.core.user.SellerProfileRepository;
import com.algoadda.core.user.User;
import com.algoadda.core.user.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("null")
@Service
public class PublicSellerService {

    private static final Logger log = LoggerFactory.getLogger(PublicSellerService.class);

    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final ListingRepository listingRepository;
    private final BacktestResultRepository backtestResultRepository;
    private final ObjectMapper objectMapper;

    public PublicSellerService(
        UserRepository userRepository,
        SellerProfileRepository sellerProfileRepository,
        ListingRepository listingRepository,
        BacktestResultRepository backtestResultRepository,
        ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.sellerProfileRepository = sellerProfileRepository;
        this.listingRepository = listingRepository;
        this.backtestResultRepository = backtestResultRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public PublicSellerProfileResponse getPublicSellerProfile(UUID sellerId) {
        User sellerUser = userRepository.findById(sellerId)
            .orElseThrow(() -> new IllegalArgumentException("Seller not found with id: " + sellerId));

        SellerProfile profile = sellerProfileRepository.findByUserId(sellerId).orElse(null);

        String displayName = profile != null ? profile.getDisplayName() : "Seller_" + sellerId.toString().substring(0, 8);
        String bio = profile != null ? profile.getBio() : "Quantitative strategy developer on AlgoAdda.";

        List<Listing> activeListings = listingRepository.findByActiveTrue();
        List<ListingSummaryResponse> sellerListings = new ArrayList<>();

        for (Listing listing : activeListings) {
            BotVersion version = listing.getBotVersion();
            if (version == null) continue;
            Bot bot = version.getBot();
            if (bot == null || bot.getStatus() != BotStatus.PUBLISHED) continue;
            if (!bot.getSeller().getId().equals(sellerId)) continue;

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
                    log.warn("Failed to parse backtest metrics: {}", e.getMessage());
                }
            }

            sellerListings.add(ListingSummaryResponse.builder()
                .listingId(listing.getId())
                .botId(bot.getId())
                .botVersionId(version.getId())
                .name(bot.getName())
                .description(bot.getDescription())
                .strategyType(bot.getStrategyType())
                .price(listing.getPrice())
                .licenseType(listing.getLicenseType())
                .sellerId(sellerId)
                .sellerDisplayName(displayName)
                .winRate(winRate)
                .maxDrawdown(maxDrawdown)
                .sharpeRatio(sharpeRatio)
                .createdAt(listing.getCreatedAt())
                .build());
        }

        return PublicSellerProfileResponse.builder()
            .sellerId(sellerId)
            .displayName(displayName)
            .bio(bio)
            .createdAt(profile != null ? profile.getCreatedAt() : sellerUser.getCreatedAt())
            .activeListings(sellerListings)
            .build();
    }
}
