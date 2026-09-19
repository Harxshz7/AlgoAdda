package com.algoadda.core.user;

import com.algoadda.core.bot.*;
import com.algoadda.core.listing.*;
import com.algoadda.core.order.*;
import com.algoadda.core.user.dto.SellerAnalyticsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SellerAnalyticsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BotRepository botRepository;

    @Autowired
    private BotVersionRepository botVersionRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private ListingViewRepository listingViewRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private SellerProfileRepository sellerProfileRepository;

    @Autowired
    private BacktestResultRepository backtestResultRepository;

    @Autowired
    private SellerAnalyticsService sellerAnalyticsService;

    private User seller;
    private Bot botAlpha;
    private Bot botBeta;

    @BeforeEach
    void setUp() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        listingViewRepository.deleteAll();
        listingRepository.deleteAll();
        backtestResultRepository.deleteAll();
        botVersionRepository.deleteAll();
        botRepository.deleteAll();
        sellerProfileRepository.deleteAll();
        userRepository.deleteAll();

        seller = userRepository.save(User.builder()
            .email("analytics_seller@algoadda.com")
            .passwordHash("password")
            .role(Role.SELLER)
            .build());

        User buyer = userRepository.save(User.builder()
            .email("buyer_analytics@algoadda.com")
            .passwordHash("password")
            .role(Role.BUYER)
            .build());

        // Bot Alpha with views and purchases
        botAlpha = botRepository.save(Bot.builder()
            .seller(seller)
            .name("Alpha Momentum")
            .description("Alpha bot")
            .strategyType("MOMENTUM")
            .status(BotStatus.PUBLISHED)
            .build());

        BotVersion vAlpha = botVersionRepository.save(BotVersion.builder()
            .bot(botAlpha)
            .versionNumber("1.0.0")
            .disclosedLogic("logic alpha")
            .backtestStatus(BacktestStatus.COMPLETED)
            .build());

        Listing listingAlpha = listingRepository.save(Listing.builder()
            .botVersion(vAlpha)
            .price(BigDecimal.valueOf(100.00))
            .licenseType(LicenseType.ONE_TIME)
            .active(true)
            .official(true)
            .build());

        // Bot Beta with 0 views (test divide-by-zero)
        botBeta = botRepository.save(Bot.builder()
            .seller(seller)
            .name("Beta Grid")
            .description("Beta bot")
            .strategyType("GRID")
            .status(BotStatus.PUBLISHED)
            .build());

        // Track 4 views for Bot Alpha
        for (int i = 0; i < 4; i++) {
            listingViewRepository.save(ListingView.builder()
                .listing(listingAlpha)
                .viewer(buyer)
                .build());
        }

        // Create Order 1: PAID (Price 100.00)
        Order paidOrder = orderRepository.save(Order.builder()
            .buyer(buyer)
            .status(OrderStatus.PAID)
            .build());

        orderItemRepository.save(OrderItem.builder()
            .order(paidOrder)
            .listing(listingAlpha)
            .botVersion(vAlpha)
            .priceAtPurchase(BigDecimal.valueOf(100.00))
            .build());

        // Create Order 2: PENDING (Price 100.00 - should be excluded)
        Order pendingOrder = orderRepository.save(Order.builder()
            .buyer(buyer)
            .status(OrderStatus.PENDING)
            .build());

        orderItemRepository.save(OrderItem.builder()
            .order(pendingOrder)
            .listing(listingAlpha)
            .botVersion(vAlpha)
            .priceAtPurchase(BigDecimal.valueOf(100.00))
            .build());

        // Create Order 3: REFUNDED (Price 50.00 - should subtract from net revenue)
        Order refundedOrder = orderRepository.save(Order.builder()
            .buyer(buyer)
            .status(OrderStatus.REFUNDED)
            .build());

        orderItemRepository.save(OrderItem.builder()
            .order(refundedOrder)
            .listing(listingAlpha)
            .botVersion(vAlpha)
            .priceAtPurchase(BigDecimal.valueOf(50.00))
            .build());
    }

    @Test
    @DisplayName("Analytics Service: Correctly computes views, purchases, conversion rate, and revenue")
    void testSellerAnalyticsCalculation() {
        SellerAnalyticsResponse response = sellerAnalyticsService.getSellerAnalytics(seller);

        assertThat(response.getBots()).hasSize(2);

        // Find Bot Alpha analytics
        var alphaAnalytics = response.getBots().stream()
            .filter(b -> b.getBotId().equals(botAlpha.getId()))
            .findFirst().orElseThrow();

        assertThat(alphaAnalytics.getTotalViews()).isEqualTo(4);
        assertThat(alphaAnalytics.getTotalPurchases()).isEqualTo(2); // 1 PAID + 1 REFUNDED
        assertThat(alphaAnalytics.getConversionRate()).isEqualTo(0.5); // 2 / 4 = 0.5
        assertThat(alphaAnalytics.getGrossRevenue()).isEqualByComparingTo(BigDecimal.valueOf(150.00)); // 100 + 50
        assertThat(alphaAnalytics.getRefundedAmount()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
        assertThat(alphaAnalytics.getNetRevenue()).isEqualByComparingTo(BigDecimal.valueOf(100.00)); // 150 - 50

        // Find Bot Beta analytics (0 views)
        var betaAnalytics = response.getBots().stream()
            .filter(b -> b.getBotId().equals(botBeta.getId()))
            .findFirst().orElseThrow();

        assertThat(betaAnalytics.getTotalViews()).isEqualTo(0);
        assertThat(betaAnalytics.getTotalPurchases()).isEqualTo(0);
        assertThat(betaAnalytics.getConversionRate()).isNull(); // 0 views -> null rate

        // Verify Top Summary
        var summary = response.getSummary();
        assertThat(summary.getTotalViews()).isEqualTo(4);
        assertThat(summary.getTotalPurchases()).isEqualTo(2);
        assertThat(summary.getTotalGrossRevenue()).isEqualByComparingTo(BigDecimal.valueOf(150.00));
        assertThat(summary.getTotalRefundedAmount()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
        assertThat(summary.getTotalNetRevenue()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
        assertThat(summary.getMostPopularBotId()).isEqualTo(botAlpha.getId());
    }
}
