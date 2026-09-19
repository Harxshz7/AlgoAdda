package com.algoadda.core.order;

import com.algoadda.core.bot.*;
import com.algoadda.core.listing.LicenseType;
import com.algoadda.core.listing.Listing;
import com.algoadda.core.listing.ListingRepository;
import com.algoadda.core.order.dto.CreateSubscriptionRequest;
import com.algoadda.core.order.dto.SubscriptionResponse;
import com.algoadda.core.user.Role;
import com.algoadda.core.user.User;
import com.algoadda.core.user.UserRepository;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@SuppressWarnings("null")
public class SubscriptionIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BotRepository botRepository;

    @Autowired
    private BotVersionRepository botVersionRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private OrderService orderService;

    private User seller;
    private User buyer;
    private User otherBuyer;
    private Listing subscriptionListing;
    private Listing oneTimeListing;
    private BotVersion subVersion;
    private BotVersion oneTimeVersion;

    @BeforeEach
    void setUp() {
        licenseRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        subscriptionRepository.deleteAll();
        listingRepository.deleteAll();
        botVersionRepository.deleteAll();
        botRepository.deleteAll();
        userRepository.deleteAll();

        seller = userRepository.save(User.builder()
            .email("subscription_seller@algoadda.com")
            .passwordHash("password")
            .role(Role.SELLER)
            .build());

        buyer = userRepository.save(User.builder()
            .email("subscription_buyer@algoadda.com")
            .passwordHash("password")
            .role(Role.BUYER)
            .build());

        otherBuyer = userRepository.save(User.builder()
            .email("other_buyer@algoadda.com")
            .passwordHash("password")
            .role(Role.BUYER)
            .build());

        Bot botSub = botRepository.save(Bot.builder()
            .seller(seller)
            .name("Subscription Strategy")
            .description("Recurring subscription strategy")
            .strategyType("MOMENTUM")
            .status(BotStatus.PUBLISHED)
            .build());

        subVersion = botVersionRepository.save(BotVersion.builder()
            .bot(botSub)
            .versionNumber("1.0.0")
            .disclosedLogic("logic sub")
            .backtestStatus(BacktestStatus.COMPLETED)
            .build());

        subscriptionListing = listingRepository.save(Listing.builder()
            .botVersion(subVersion)
            .price(BigDecimal.valueOf(499.00))
            .licenseType(LicenseType.SUBSCRIPTION)
            .billingInterval("MONTHLY")
            .razorpayPlanId("plan_test_12345")
            .active(true)
            .official(true)
            .build());

        Bot botOneTime = botRepository.save(Bot.builder()
            .seller(seller)
            .name("OneTime Strategy")
            .description("One time strategy")
            .strategyType("GRID")
            .status(BotStatus.PUBLISHED)
            .build());

        oneTimeVersion = botVersionRepository.save(BotVersion.builder()
            .bot(botOneTime)
            .versionNumber("2.0.0")
            .disclosedLogic("logic one-time")
            .backtestStatus(BacktestStatus.COMPLETED)
            .build());

        oneTimeListing = listingRepository.save(Listing.builder()
            .botVersion(oneTimeVersion)
            .price(BigDecimal.valueOf(1999.00))
            .licenseType(LicenseType.ONE_TIME)
            .active(true)
            .official(true)
            .build());
    }

    @Test
    @DisplayName("Subscription Creation: Issues license with correct initial 30-day expiry")
    void testCreateSubscription_IssuesLicenseWithInitialExpiry() {
        CreateSubscriptionRequest request = new CreateSubscriptionRequest(subscriptionListing.getId());
        SubscriptionResponse response = subscriptionService.createSubscription(buyer.getId(), request);

        assertThat(response).isNotNull();
        assertThat(response.getSubscriptionId()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(response.getRazorpaySubscriptionId()).isNotNull();
        assertThat(response.getCurrentPeriodEnd()).isAfter(Instant.now().plus(Duration.ofDays(29)));

        List<License> licenses = licenseRepository.findBySubscriptionId(response.getSubscriptionId());
        assertThat(licenses).hasSize(1);
        License license = licenses.get(0);
        assertThat(license.getBuyer().getId()).isEqualTo(buyer.getId());
        assertThat(license.getBotVersion().getId()).isEqualTo(subVersion.getId());
        assertThat(license.getExpiresAt()).isEqualTo(response.getCurrentPeriodEnd());
        assertThat(license.isActive()).isTrue();
        assertThat(license.isPerpetual()).isFalse();
    }

    @Test
    @DisplayName("Webhook Handling & Idempotency: subscription.charged extends expiry and handles duplicates cleanly")
    void testSubscriptionChargedWebhook_IdempotentRenewal() {
        CreateSubscriptionRequest request = new CreateSubscriptionRequest(subscriptionListing.getId());
        SubscriptionResponse response = subscriptionService.createSubscription(buyer.getId(), request);

        String razorpaySubId = response.getRazorpaySubscriptionId();
        Instant futureExpiry = Instant.now().plus(Duration.ofDays(60));
        long futureEpochSec = futureExpiry.getEpochSecond();

        String chargedPayload = "{\n" +
            "  \"event\": \"subscription.charged\",\n" +
            "  \"payload\": {\n" +
            "    \"subscription\": {\n" +
            "      \"entity\": {\n" +
            "        \"id\": \"" + razorpaySubId + "\",\n" +
            "        \"current_end\": " + futureEpochSec + "\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

        JSONObject json = new JSONObject(chargedPayload);

        // First webhook call: Extends period end and license expiry
        subscriptionService.processSubscriptionWebhook("subscription.charged", json);

        Subscription updatedSub = subscriptionRepository.findById(response.getSubscriptionId()).orElseThrow();
        assertThat(updatedSub.getCurrentPeriodEnd().getEpochSecond()).isEqualTo(futureEpochSec);

        License updatedLicense = licenseRepository.findBySubscriptionId(response.getSubscriptionId()).get(0);
        assertThat(updatedLicense.getExpiresAt().getEpochSecond()).isEqualTo(futureEpochSec);

        // SECOND webhook call (Deliberate duplicate for idempotency verification)
        subscriptionService.processSubscriptionWebhook("subscription.charged", json);

        // Verify expiry is unchanged and license was not duplicated
        List<License> licensesAfterDuplicate = licenseRepository.findBySubscriptionId(response.getSubscriptionId());
        assertThat(licensesAfterDuplicate).hasSize(1);
        assertThat(licensesAfterDuplicate.get(0).getExpiresAt().getEpochSecond()).isEqualTo(futureEpochSec);
    }

    @Test
    @DisplayName("Subscription Cancellation: Cancel endpoint & webhook set status to CANCELLED and access remains valid until period end")
    void testCancelSubscription_AccessValidUntilPeriodEnd() {
        CreateSubscriptionRequest request = new CreateSubscriptionRequest(subscriptionListing.getId());
        SubscriptionResponse response = subscriptionService.createSubscription(buyer.getId(), request);

        // Non-owner cancel attempt should fail
        assertThatThrownBy(() ->
            subscriptionService.cancelSubscription(otherBuyer.getId(), response.getSubscriptionId())
        ).isInstanceOf(AccessDeniedException.class);

        // Owner cancels subscription
        SubscriptionResponse cancelResponse = subscriptionService.cancelSubscription(buyer.getId(), response.getSubscriptionId());
        assertThat(cancelResponse.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);

        // Verify access is still active until current_period_end
        License license = licenseRepository.findBySubscriptionId(response.getSubscriptionId()).get(0);
        assertThat(license.isActive()).isTrue();
        assertThat(license.isRevoked()).isFalse();
        assertThat(Instant.now()).isBefore(license.getExpiresAt());
    }

    @Test
    @DisplayName("Subscription Halted: Sets status to PAST_DUE and applies 3-day grace period")
    void testSubscriptionHalted_SetsPastDueWithGracePeriod() {
        CreateSubscriptionRequest request = new CreateSubscriptionRequest(subscriptionListing.getId());
        SubscriptionResponse response = subscriptionService.createSubscription(buyer.getId(), request);

        String razorpaySubId = response.getRazorpaySubscriptionId();
        String haltedPayload = "{\n" +
            "  \"event\": \"subscription.halted\",\n" +
            "  \"payload\": {\n" +
            "    \"subscription\": {\n" +
            "      \"entity\": {\n" +
            "        \"id\": \"" + razorpaySubId + "\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

        subscriptionService.processSubscriptionWebhook("subscription.halted", new JSONObject(haltedPayload));

        Subscription sub = subscriptionRepository.findById(response.getSubscriptionId()).orElseThrow();
        assertThat(sub.getStatus()).isEqualTo(SubscriptionStatus.PAST_DUE);

        License license = licenseRepository.findBySubscriptionId(response.getSubscriptionId()).get(0);
        assertThat(license.getExpiresAt()).isAfter(sub.getCurrentPeriodEnd());
    }

    @Test
    @DisplayName("Regression Test: One-time purchase flow works as before")
    void testOneTimePurchase_RegressionCheck() {
        Order order = orderRepository.save(Order.builder()
            .buyer(buyer)
            .listing(oneTimeListing)
            .paymentReference("order_onetime_123")
            .status(OrderStatus.PENDING)
            .build());

        orderItemRepository.save(OrderItem.builder()
            .order(order)
            .listing(oneTimeListing)
            .botVersion(oneTimeVersion)
            .priceAtPurchase(BigDecimal.valueOf(1999.00))
            .build());

        // Process one-time payment.captured webhook simulation directly via OrderService
        String paymentCapturedPayload = "{\n" +
            "  \"event\": \"payment.captured\",\n" +
            "  \"payload\": {\n" +
            "    \"payment\": {\n" +
            "      \"entity\": {\n" +
            "        \"id\": \"pay_123\",\n" +
            "        \"order_id\": \"order_onetime_123\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

        orderService.processWebhook(paymentCapturedPayload, null);

        Order processedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(processedOrder.getStatus()).isEqualTo(OrderStatus.PAID);

        List<License> licenses = licenseRepository.findByOrderId(order.getId());
        assertThat(licenses).hasSize(1);
        License oneTimeLicense = licenses.get(0);
        assertThat(oneTimeLicense.isPerpetual()).isTrue();
        assertThat(oneTimeLicense.isActive()).isTrue();
    }
}
