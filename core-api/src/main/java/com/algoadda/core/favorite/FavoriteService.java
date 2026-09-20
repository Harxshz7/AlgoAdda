package com.algoadda.core.favorite;

import com.algoadda.core.bot.*;
import com.algoadda.core.bot.service.RiskClassifier;
import com.algoadda.core.listing.Listing;
import com.algoadda.core.listing.ListingRepository;
import com.algoadda.core.listing.dto.ListingSummaryResponse;
import com.algoadda.core.review.ReviewRepository;
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

import java.util.*;

@SuppressWarnings("null")
@Service
public class FavoriteService {

    private static final Logger log = LoggerFactory.getLogger(FavoriteService.class);

    private final FavoriteRepository favoriteRepository;
    private final BotRepository botRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final BacktestResultRepository backtestResultRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final ReviewRepository reviewRepository;
    private final ObjectMapper objectMapper;

    public FavoriteService(
        FavoriteRepository favoriteRepository,
        BotRepository botRepository,
        UserRepository userRepository,
        ListingRepository listingRepository,
        BacktestResultRepository backtestResultRepository,
        SellerProfileRepository sellerProfileRepository,
        ReviewRepository reviewRepository,
        ObjectMapper objectMapper
    ) {
        this.favoriteRepository = favoriteRepository;
        this.botRepository = botRepository;
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
        this.backtestResultRepository = backtestResultRepository;
        this.sellerProfileRepository = sellerProfileRepository;
        this.reviewRepository = reviewRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public boolean addFavorite(UUID buyerId, UUID botId) {
        Bot bot = botRepository.findById(botId)
            .orElseThrow(() -> new NoSuchElementException("Bot not found with id: " + botId));

        if (bot.getStatus() != BotStatus.PUBLISHED) {
            throw new IllegalArgumentException("Only published bots can be added to favorites");
        }

        User buyer = userRepository.findById(buyerId)
            .orElseThrow(() -> new NoSuchElementException("User not found with id: " + buyerId));

        // Idempotent: if already favorited, return success without error or duplicate
        if (favoriteRepository.existsByBuyerIdAndBotId(buyerId, botId)) {
            log.debug("Bot {} is already favorited by buyer {}", botId, buyerId);
            return true;
        }

        Favorite favorite = Favorite.builder()
            .buyer(buyer)
            .bot(bot)
            .build();

        favoriteRepository.save(favorite);
        log.info("Buyer {} favorited bot {}", buyerId, botId);
        return true;
    }

    @Transactional
    public boolean removeFavorite(UUID buyerId, UUID botId) {
        // Idempotent: if not favorited, return success without error
        if (!favoriteRepository.existsByBuyerIdAndBotId(buyerId, botId)) {
            log.debug("Bot {} is not favorited by buyer {}, no-op unfavorite", botId, buyerId);
            return true;
        }

        favoriteRepository.deleteByBuyerIdAndBotId(buyerId, botId);
        log.info("Buyer {} unfavorited bot {}", buyerId, botId);
        return true;
    }

    @Transactional(readOnly = true)
    public List<ListingSummaryResponse> getBuyerFavorites(UUID buyerId) {
        List<Favorite> favorites = favoriteRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId);
        List<ListingSummaryResponse> results = new ArrayList<>();

        for (Favorite fav : favorites) {
            Bot bot = fav.getBot();
            if (bot == null) continue;

            // Only return published bots (skip suspended seller or draft bots)
            if (bot.getStatus() != BotStatus.PUBLISHED) continue;
            if (bot.getSeller() != null && bot.getSeller().isSuspended()) continue;

            Listing listing = listingRepository.findFirstByBotVersion_Bot_IdAndActiveTrueOrderByCreatedAtDesc(bot.getId())
                .orElse(null);

            BotVersion version = listing != null ? listing.getBotVersion() : null;
            UUID versionId = version != null ? version.getId() : null;

            Double winRate = null;
            Double maxDrawdown = null;
            Double sharpeRatio = null;
            RiskLabel riskLabel = null;

            if (versionId != null) {
                BacktestResult backtest = backtestResultRepository
                    .findFirstByBotVersionIdOrderByCreatedAtDesc(versionId)
                    .orElse(null);

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

                if (backtest != null) {
                    riskLabel = backtest.getRiskLabel() != null
                        ? backtest.getRiskLabel()
                        : RiskClassifier.classify(backtest.getMetrics());
                }
            }

            SellerProfile sellerProfile = bot.getSeller() != null
                ? sellerProfileRepository.findByUserId(bot.getSeller().getId()).orElse(null)
                : null;
            String sellerDisplayName = sellerProfile != null
                ? sellerProfile.getDisplayName()
                : (bot.getSeller() != null ? bot.getSeller().getEmail() : "Seller");

            Double avgRating = reviewRepository.getAverageRatingByBotId(bot.getId());
            Double roundedAvgRating = avgRating != null && avgRating > 0.0 ? Math.round(avgRating * 10.0) / 10.0 : null;
            Long reviewCount = reviewRepository.countByBotId(bot.getId());

            results.add(ListingSummaryResponse.builder()
                .listingId(listing != null ? listing.getId() : null)
                .botId(bot.getId())
                .botVersionId(versionId)
                .name(bot.getName())
                .description(bot.getDescription())
                .strategyType(bot.getStrategyType())
                .price(listing != null ? listing.getPrice() : null)
                .licenseType(listing != null ? listing.getLicenseType() : null)
                .sellerId(bot.getSeller() != null ? bot.getSeller().getId() : null)
                .sellerDisplayName(sellerDisplayName)
                .winRate(winRate)
                .maxDrawdown(maxDrawdown)
                .sharpeRatio(sharpeRatio)
                .riskLabel(riskLabel)
                .isOfficial(listing != null && listing.isOfficial())
                .averageRating(roundedAvgRating)
                .reviewCount(reviewCount)
                .isFavorited(true)
                .createdAt(fav.getCreatedAt())
                .build());
        }

        return results;
    }

    @Transactional(readOnly = true)
    public boolean isBotFavorited(UUID buyerId, UUID botId) {
        if (buyerId == null || botId == null) return false;
        return favoriteRepository.existsByBuyerIdAndBotId(buyerId, botId);
    }

    @Transactional(readOnly = true)
    public Set<UUID> getFavoritedBotIds(UUID buyerId) {
        if (buyerId == null) return Collections.emptySet();
        return favoriteRepository.findBotIdsByBuyerId(buyerId);
    }
}
