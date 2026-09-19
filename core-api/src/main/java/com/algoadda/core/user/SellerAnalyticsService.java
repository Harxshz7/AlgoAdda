package com.algoadda.core.user;

import com.algoadda.core.bot.Bot;
import com.algoadda.core.bot.BotRepository;
import com.algoadda.core.listing.ListingViewRepository;
import com.algoadda.core.order.OrderItem;
import com.algoadda.core.order.OrderItemRepository;
import com.algoadda.core.order.OrderStatus;
import com.algoadda.core.user.dto.AnalyticsSummaryDto;
import com.algoadda.core.user.dto.BotAnalyticsDto;
import com.algoadda.core.user.dto.SellerAnalyticsResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@SuppressWarnings("null")
public class SellerAnalyticsService {

    private final BotRepository botRepository;
    private final ListingViewRepository listingViewRepository;
    private final OrderItemRepository orderItemRepository;

    public SellerAnalyticsService(
        BotRepository botRepository,
        ListingViewRepository listingViewRepository,
        OrderItemRepository orderItemRepository
    ) {
        this.botRepository = botRepository;
        this.listingViewRepository = listingViewRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional(readOnly = true)
    public SellerAnalyticsResponse getSellerAnalytics(User seller) {
        List<Bot> bots = botRepository.findBySellerId(seller.getId());

        List<BotAnalyticsDto> botAnalyticsList = new ArrayList<>();

        long overallViews = 0;
        long overallPurchases = 0;
        BigDecimal overallGrossRevenue = BigDecimal.ZERO;
        BigDecimal overallRefundedAmount = BigDecimal.ZERO;
        BigDecimal overallNetRevenue = BigDecimal.ZERO;

        Bot mostPopularBot = null;
        long maxPurchases = 0;

        for (Bot bot : bots) {
            long views = listingViewRepository.countViewsByBotId(bot.getId());

            List<OrderItem> paidItems = orderItemRepository.findByBotIdAndOrderStatus(bot.getId(), OrderStatus.PAID);
            List<OrderItem> refundedItems = orderItemRepository.findByBotIdAndOrderStatus(bot.getId(), OrderStatus.REFUNDED);

            BigDecimal paidRevenue = paidItems.stream()
                .map(OrderItem::getPriceAtPurchase)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal refundedRevenue = refundedItems.stream()
                .map(OrderItem::getPriceAtPurchase)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal grossRevenue = paidRevenue.add(refundedRevenue);
            BigDecimal netRevenue = paidRevenue;
            long botPurchases = paidItems.size() + refundedItems.size();

            Double conversionRate = views > 0 ? ((double) botPurchases / views) : null;
            Double averageRating = null; // Phase 6 Review entity safe fallback

            BotAnalyticsDto dto = new BotAnalyticsDto(
                bot.getId(),
                bot.getName(),
                bot.getStrategyType(),
                bot.getStatus().name(),
                views,
                botPurchases,
                conversionRate,
                grossRevenue,
                refundedRevenue,
                netRevenue,
                averageRating
            );

            botAnalyticsList.add(dto);

            overallViews += views;
            overallPurchases += botPurchases;
            overallGrossRevenue = overallGrossRevenue.add(grossRevenue);
            overallRefundedAmount = overallRefundedAmount.add(refundedRevenue);
            overallNetRevenue = overallNetRevenue.add(netRevenue);

            if (botPurchases > maxPurchases) {
                maxPurchases = botPurchases;
                mostPopularBot = bot;
            }
        }

        AnalyticsSummaryDto summary = new AnalyticsSummaryDto(
            overallViews,
            overallPurchases,
            overallGrossRevenue,
            overallRefundedAmount,
            overallNetRevenue,
            mostPopularBot != null ? mostPopularBot.getId() : null,
            mostPopularBot != null ? mostPopularBot.getName() : null,
            maxPurchases
        );

        return new SellerAnalyticsResponse(summary, botAnalyticsList);
    }
}
