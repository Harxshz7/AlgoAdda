package com.algoadda.core.order;

import com.algoadda.core.listing.LicenseType;
import com.algoadda.core.listing.Listing;
import com.algoadda.core.listing.ListingRepository;
import com.algoadda.core.order.dto.CreateSubscriptionRequest;
import com.algoadda.core.order.dto.SubscriptionResponse;
import com.algoadda.core.user.User;
import com.algoadda.core.user.UserRepository;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@SuppressWarnings("null")
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    private final SubscriptionRepository subscriptionRepository;
    private final LicenseRepository licenseRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final RazorpayService razorpayService;

    public SubscriptionService(
        SubscriptionRepository subscriptionRepository,
        LicenseRepository licenseRepository,
        ListingRepository listingRepository,
        UserRepository userRepository,
        RazorpayService razorpayService
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.licenseRepository = licenseRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.razorpayService = razorpayService;
    }

    @Transactional
    public SubscriptionResponse createSubscription(UUID buyerId, CreateSubscriptionRequest request) {
        Objects.requireNonNull(buyerId, "buyerId must not be null");
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(request.getListingId(), "listingId must not be null");

        User buyer = userRepository.findById(buyerId)
            .orElseThrow(() -> new IllegalArgumentException("Buyer not found with id: " + buyerId));

        Listing listing = listingRepository.findById(request.getListingId())
            .orElseThrow(() -> new IllegalArgumentException("Listing not found with id: " + request.getListingId()));

        if (listing.getLicenseType() != LicenseType.SUBSCRIPTION) {
            throw new IllegalArgumentException("Listing " + request.getListingId() + " is not a SUBSCRIPTION listing");
        }

        String planId = listing.getRazorpayPlanId();
        if (planId == null || planId.isBlank()) {
            planId = razorpayService.createPlan(listing.getPrice(), "Subscription - " + listing.getBotVersion().getBot().getName());
            listing.setRazorpayPlanId(planId);
            listing.setBillingInterval("MONTHLY");
            listingRepository.save(listing);
        }

        String rzpSubId = razorpayService.createSubscription(planId);

        Instant currentPeriodEnd = Instant.now().plus(Duration.ofDays(30));

        Subscription subscription = Subscription.builder()
            .buyer(buyer)
            .listing(listing)
            .razorpaySubscriptionId(rzpSubId)
            .status(SubscriptionStatus.ACTIVE)
            .currentPeriodEnd(currentPeriodEnd)
            .build();

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        // Issue initial License for the subscription
        License license = License.builder()
            .subscription(savedSubscription)
            .botVersion(listing.getBotVersion())
            .buyer(buyer)
            .issuedAt(Instant.now())
            .expiresAt(currentPeriodEnd)
            .revoked(false)
            .build();

        licenseRepository.save(license);
        log.info("Issued initial subscription License ID {} for Subscription ID {}", license.getId(), savedSubscription.getId());

        return mapToResponse(savedSubscription);
    }

    @Transactional
    public SubscriptionResponse cancelSubscription(UUID buyerId, UUID subscriptionId) {
        Objects.requireNonNull(buyerId, "buyerId must not be null");
        Objects.requireNonNull(subscriptionId, "subscriptionId must not be null");

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new IllegalArgumentException("Subscription not found with id: " + subscriptionId));

        if (!subscription.getBuyer().getId().equals(buyerId)) {
            throw new AccessDeniedException("Buyer does not own this subscription");
        }

        razorpayService.cancelSubscription(subscription.getRazorpaySubscriptionId());
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        Subscription saved = subscriptionRepository.save(subscription);
        log.info("Subscription ID {} cancelled. Access active until {}", saved.getId(), saved.getCurrentPeriodEnd());

        return mapToResponse(saved);
    }

    @Transactional
    public void processSubscriptionWebhook(String event, JSONObject root) {
        log.info("Processing subscription webhook event: {}", event);

        String razorpaySubscriptionId = extractSubscriptionId(root);
        if (razorpaySubscriptionId == null || razorpaySubscriptionId.isBlank()) {
            log.warn("Could not extract razorpay_subscription_id from webhook payload");
            return;
        }

        Subscription subscription = subscriptionRepository.findByRazorpaySubscriptionId(razorpaySubscriptionId)
            .orElse(null);

        if (subscription == null) {
            log.warn("No subscription found matching Razorpay subscription ID '{}'", razorpaySubscriptionId);
            return;
        }

        if ("subscription.charged".equalsIgnoreCase(event)) {
            Instant newPeriodEnd = extractCurrentPeriodEnd(root);
            if (newPeriodEnd == null) {
                newPeriodEnd = Instant.now().plus(Duration.ofDays(30));
            }

            // IDEMPOTENCY CHECK: If already extended to or past this period end, skip
            if (subscription.getCurrentPeriodEnd() != null && !subscription.getCurrentPeriodEnd().isBefore(newPeriodEnd)) {
                log.info("Idempotency check: Subscription {} is already renewed up to {}. Ignoring duplicate webhook.", subscription.getId(), subscription.getCurrentPeriodEnd());
                return;
            }

            subscription.setCurrentPeriodEnd(newPeriodEnd);
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscriptionRepository.save(subscription);

            List<License> licenses = licenseRepository.findBySubscriptionId(subscription.getId());
            for (License license : licenses) {
                license.setExpiresAt(newPeriodEnd);
                licenseRepository.save(license);
                log.info("Extended License ID {} expiry to {} for subscription {}", license.getId(), newPeriodEnd, subscription.getId());
            }
        } else if ("subscription.cancelled".equalsIgnoreCase(event)) {
            log.info("Setting Subscription ID {} status to CANCELLED", subscription.getId());
            subscription.setStatus(SubscriptionStatus.CANCELLED);
            subscriptionRepository.save(subscription);
            // Access continues until currentPeriodEnd (Decision 1)
        } else if ("subscription.halted".equalsIgnoreCase(event) || "subscription.paused".equalsIgnoreCase(event)) {
            log.info("Setting Subscription ID {} status to PAST_DUE", subscription.getId());
            subscription.setStatus(SubscriptionStatus.PAST_DUE);
            subscriptionRepository.save(subscription);

            // Grant 3-day grace period (Decision 2)
            List<License> licenses = licenseRepository.findBySubscriptionId(subscription.getId());
            Instant graceExpiry = subscription.getCurrentPeriodEnd() != null 
                ? subscription.getCurrentPeriodEnd().plus(Duration.ofDays(3))
                : Instant.now().plus(Duration.ofDays(3));

            for (License license : licenses) {
                license.setExpiresAt(graceExpiry);
                licenseRepository.save(license);
            }
        }
    }

    private String extractSubscriptionId(JSONObject root) {
        try {
            if (root.has("payload")) {
                JSONObject payload = root.getJSONObject("payload");
                if (payload.has("subscription")) {
                    JSONObject subEntity = payload.getJSONObject("subscription").getJSONObject("entity");
                    if (subEntity.has("id")) return subEntity.getString("id");
                }
                if (payload.has("payment")) {
                    JSONObject payEntity = payload.getJSONObject("payment").getJSONObject("entity");
                    if (payEntity.has("subscription_id")) return payEntity.getString("subscription_id");
                }
            }
        } catch (Exception e) {
            log.warn("Error parsing subscription ID from webhook payload: {}", e.getMessage());
        }
        return root.optString("subscription_id", null);
    }

    private Instant extractCurrentPeriodEnd(JSONObject root) {
        try {
            if (root.has("payload")) {
                JSONObject payload = root.getJSONObject("payload");
                if (payload.has("subscription")) {
                    JSONObject subEntity = payload.getJSONObject("subscription").getJSONObject("entity");
                    if (subEntity.has("current_end")) {
                        long endSec = subEntity.getLong("current_end");
                        return Instant.ofEpochSecond(endSec);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error parsing current_end from webhook payload: {}", e.getMessage());
        }
        return null;
    }

    private SubscriptionResponse mapToResponse(Subscription subscription) {
        return new SubscriptionResponse(
            subscription.getId(),
            subscription.getListing().getId(),
            subscription.getListing().getBotVersion().getBot().getId(),
            subscription.getListing().getBotVersion().getBot().getName(),
            subscription.getRazorpaySubscriptionId(),
            subscription.getStatus(),
            subscription.getCurrentPeriodEnd(),
            subscription.getCreatedAt()
        );
    }
}
