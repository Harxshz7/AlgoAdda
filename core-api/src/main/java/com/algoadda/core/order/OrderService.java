package com.algoadda.core.order;

import com.algoadda.core.listing.Listing;
import com.algoadda.core.listing.ListingRepository;
import com.algoadda.core.order.dto.CreateOrderRequest;
import com.algoadda.core.order.dto.OrderResponse;
import com.algoadda.core.order.dto.RefundResponse;
import com.algoadda.core.user.User;
import com.algoadda.core.user.UserRepository;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@SuppressWarnings("null")
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final LicenseRepository licenseRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final RazorpayService razorpayService;

    public OrderService(
        OrderRepository orderRepository,
        LicenseRepository licenseRepository,
        ListingRepository listingRepository,
        UserRepository userRepository,
        RazorpayService razorpayService
    ) {
        this.orderRepository = orderRepository;
        this.licenseRepository = licenseRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.razorpayService = razorpayService;
    }

    @Transactional
    public OrderResponse createOrder(UUID buyerId, CreateOrderRequest request) {
        Objects.requireNonNull(buyerId, "buyerId must not be null");
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(request.getListingId(), "listingId must not be null");

        User buyer = userRepository.findById(buyerId)
            .orElseThrow(() -> new IllegalArgumentException("Buyer not found"));

        Listing listing = listingRepository.findById(request.getListingId())
            .orElseThrow(() -> new IllegalArgumentException("Listing not found"));

        if (!listing.isActive()) {
            throw new IllegalArgumentException("Listing is not active or available for purchase");
        }

        Order order = Order.builder()
            .buyer(buyer)
            .listing(listing)
            .status(OrderStatus.PENDING)
            .build();

        order = orderRepository.save(order);

        String razorpayOrderId = razorpayService.createOrder(listing.getPrice(), order.getId().toString());
        order.setPaymentReference(razorpayOrderId);
        orderRepository.save(order);

        BigDecimal price = listing.getPrice() != null ? listing.getPrice() : BigDecimal.ZERO;
        long amountInPaise = price.multiply(new BigDecimal(100)).longValue();

        return new OrderResponse(
            order.getId(),
            razorpayOrderId,
            price,
            amountInPaise,
            "INR",
            razorpayService.getKeyId(),
            order.getStatus()
        );
    }

    @Transactional
    public void processWebhook(String payloadBody, String signatureHeader) {
        log.info("Processing Razorpay payment webhook");

        if (!razorpayService.verifyWebhookSignature(payloadBody, signatureHeader)) {
            log.warn("Razorpay webhook signature verification failed");
            throw new IllegalArgumentException("Invalid Razorpay webhook signature");
        }

        JSONObject root = new JSONObject(payloadBody);
        String event = root.optString("event");
        log.info("Razorpay webhook event received: {}", event);

        String razorpayOrderId = extractRazorpayOrderId(root);
        if (razorpayOrderId == null || razorpayOrderId.isBlank()) {
            log.warn("Could not extract razorpay_order_id from webhook payload");
            return;
        }

        List<Order> orders = orderRepository.findByStatus(OrderStatus.PENDING);
        Order order = orders.stream()
            .filter(o -> razorpayOrderId.equals(o.getPaymentReference()))
            .findFirst()
            .orElse(null);

        if (order == null) {
            // Check if order was already processed (IDEMPOTENCY CHECK)
            List<Order> paidOrders = orderRepository.findByStatus(OrderStatus.PAID);
            Order alreadyPaid = paidOrders.stream()
                .filter(o -> razorpayOrderId.equals(o.getPaymentReference()))
                .findFirst()
                .orElse(null);

            if (alreadyPaid != null) {
                log.info("Idempotency safeguard: Order {} (Razorpay ID: {}) is already PAID. Ignoring duplicate webhook.", alreadyPaid.getId(), razorpayOrderId);
                return;
            }

            log.warn("No pending order found matching Razorpay order ID '{}'", razorpayOrderId);
            return;
        }

        // Idempotency check on existing order
        if (order.getStatus() == OrderStatus.PAID) {
            log.info("Idempotency check: Order {} is already PAID. Skipping license generation.", order.getId());
            return;
        }

        if ("payment.captured".equalsIgnoreCase(event) || "order.paid".equalsIgnoreCase(event)) {
            log.info("Payment successful for Order ID {}. Updating status to PAID and issuing License.", order.getId());
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);

            // Issue license tied to exact BotVersion purchased
            List<License> existingLicenses = licenseRepository.findByOrderId(order.getId());
            if (existingLicenses.isEmpty()) {
                License license = License.builder()
                    .order(order)
                    .botVersion(order.getListing().getBotVersion())
                    .buyer(order.getBuyer())
                    .issuedAt(Instant.now())
                    .expiresAt(null) // ONE_TIME / perpetual
                    .revoked(false)
                    .build();

                licenseRepository.save(license);
                log.info("Issued License ID {} for Buyer {} and Bot Version {}", license.getId(), order.getBuyer().getId(), order.getListing().getBotVersion().getVersionNumber());
            } else {
                log.info("License already exists for Order {}", order.getId());
            }

        } else if ("payment.failed".equalsIgnoreCase(event)) {
            log.info("Payment failed for Order ID {}. Updating status to FAILED.", order.getId());
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);
        }
    }

    @Transactional
    public RefundResponse refundOrder(UUID orderId) {
        Objects.requireNonNull(orderId, "orderId must not be null");

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("Only PAID orders can be refunded. Current status: " + order.getStatus());
        }

        String refundId = razorpayService.refundPayment(order.getPaymentReference(), order.getListing().getPrice());
        order.setStatus(OrderStatus.REFUNDED);
        orderRepository.save(order);

        List<License> licenses = licenseRepository.findByOrderId(order.getId());
        boolean anyRevoked = false;
        for (License license : licenses) {
            license.setRevoked(true);
            licenseRepository.save(license);
            anyRevoked = true;
            log.info("Revoked License ID {} due to order refund", license.getId());
        }

        return new RefundResponse(order.getId(), order.getStatus(), refundId, anyRevoked);
    }

    private String extractRazorpayOrderId(JSONObject root) {
        try {
            if (root.has("payload")) {
                JSONObject payload = root.getJSONObject("payload");
                if (payload.has("payment")) {
                    JSONObject paymentEntity = payload.getJSONObject("payment").getJSONObject("entity");
                    if (paymentEntity.has("order_id")) {
                        return paymentEntity.getString("order_id");
                    }
                }
                if (payload.has("order")) {
                    JSONObject orderEntity = payload.getJSONObject("order").getJSONObject("entity");
                    if (orderEntity.has("id")) {
                        return orderEntity.getString("id");
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error parsing webhook JSON payload: {}", e.getMessage());
        }
        return root.optString("razorpay_order_id", null);
    }
}
