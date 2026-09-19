package com.algoadda.core.order;

import com.algoadda.core.cart.Cart;
import com.algoadda.core.cart.CartItem;
import com.algoadda.core.cart.CartService;
import com.algoadda.core.email.EmailService;
import com.algoadda.core.listing.Listing;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final LicenseRepository licenseRepository;

    private final UserRepository userRepository;
    private final CartService cartService;
    private final RazorpayService razorpayService;
    private final EmailService emailService;
    private final SubscriptionService subscriptionService;

    public OrderService(
        OrderRepository orderRepository,
        OrderItemRepository orderItemRepository,
        LicenseRepository licenseRepository,
        UserRepository userRepository,
        CartService cartService,
        RazorpayService razorpayService,
        EmailService emailService,
        SubscriptionService subscriptionService
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.licenseRepository = licenseRepository;
        this.userRepository = userRepository;
        this.cartService = cartService;
        this.razorpayService = razorpayService;
        this.emailService = emailService;
        this.subscriptionService = subscriptionService;
    }

    @Transactional
    public OrderResponse createOrder(UUID buyerId, CreateOrderRequest request) {
        Objects.requireNonNull(buyerId, "buyerId must not be null");

        User buyer = userRepository.findById(buyerId)
            .orElseThrow(() -> new IllegalArgumentException("Buyer not found"));

        // If request includes a specific listingId, add it to cart first
        if (request != null && request.getListingId() != null) {
            com.algoadda.core.cart.dto.AddToCartRequest addToCartRequest = new com.algoadda.core.cart.dto.AddToCartRequest(request.getListingId());
            cartService.addItemToCart(buyerId, addToCartRequest);
        }

        Cart cart = cartService.getOrCreateCartEntity(buyerId);

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty. Add items to cart before checkout.");
        }

        // Validate all cart items are active/published
        List<String> inactiveListings = cart.getItems().stream()
            .filter(item -> item.getListing() == null || !item.getListing().isActive())
            .map(item -> item.getListing() != null && item.getListing().getBotVersion() != null && item.getListing().getBotVersion().getBot() != null
                ? item.getListing().getBotVersion().getBot().getName()
                : "Unknown Strategy")
            .collect(Collectors.toList());

        if (!inactiveListings.isEmpty()) {
            throw new IllegalArgumentException("The following items in your cart are no longer active: " + String.join(", ", inactiveListings));
        }

        // Compute total amount
        BigDecimal totalAmount = cart.getItems().stream()
            .map(item -> item.getListing().getPrice())
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create single Order
        Order order = Order.builder()
            .buyer(buyer)
            .status(OrderStatus.PENDING)
            .build();
        order = orderRepository.save(order);

        // Create OrderItems
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            Listing listing = cartItem.getListing();
            OrderItem orderItem = OrderItem.builder()
                .order(order)
                .listing(listing)
                .botVersion(listing.getBotVersion())
                .priceAtPurchase(listing.getPrice() != null ? listing.getPrice() : BigDecimal.ZERO)
                .build();
            orderItems.add(orderItem);
        }
        orderItemRepository.saveAll(orderItems);
        order.setItems(orderItems);

        // Create Razorpay order for sum total
        String razorpayOrderId = razorpayService.createOrder(totalAmount, order.getId().toString());
        order.setPaymentReference(razorpayOrderId);
        orderRepository.save(order);

        long amountInPaise = totalAmount.multiply(new BigDecimal(100)).longValue();

        return new OrderResponse(
            order.getId(),
            razorpayOrderId,
            totalAmount,
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

        if (event != null && event.startsWith("subscription.")) {
            subscriptionService.processSubscriptionWebhook(event, root);
            return;
        }

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
            log.info("Payment successful for Order ID {}. Updating status to PAID and issuing Licenses.", order.getId());
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);

            List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
            List<License> existingLicenses = licenseRepository.findByOrderId(order.getId());

            if (orderItems.isEmpty() && order.getListing() != null) {
                // Fallback for legacy single-item orders
                if (existingLicenses.isEmpty()) {
                    License license = License.builder()
                        .order(order)
                        .botVersion(order.getListing().getBotVersion())
                        .buyer(order.getBuyer())
                        .issuedAt(Instant.now())
                        .expiresAt(null)
                        .revoked(false)
                        .build();
                    licenseRepository.save(license);
                    log.info("Issued legacy License ID {} for Buyer {} and Bot Version {}", license.getId(), order.getBuyer().getId(), order.getListing().getBotVersion().getVersionNumber());
                }
            } else {
                for (OrderItem item : orderItems) {
                    boolean licenseExists = existingLicenses.stream()
                        .anyMatch(l -> l.getBotVersion().getId().equals(item.getBotVersion().getId()));
                    if (!licenseExists) {
                        License license = License.builder()
                            .order(order)
                            .botVersion(item.getBotVersion())
                            .buyer(order.getBuyer())
                            .issuedAt(Instant.now())
                            .expiresAt(null)
                            .revoked(false)
                            .build();
                        licenseRepository.save(license);
                        log.info("Issued License ID {} for Buyer {} and Bot Version {}", license.getId(), order.getBuyer().getId(), item.getBotVersion().getVersionNumber());
                    }
                }
            }

            // Clear buyer's cart on successful payment
            cartService.clearCart(order.getBuyer().getId());

            // Trigger combined Purchase Confirmation & License Ready email to buyer (async)
            List<License> currentLicenses = licenseRepository.findByOrderId(order.getId());
            emailService.sendPurchaseConfirmationAndLicenseEmail(order, orderItems, currentLicenses);

        } else if ("payment.failed".equalsIgnoreCase(event)) {
            log.info("Payment failed for Order ID {}. Updating status to FAILED.", order.getId());
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);

            // Trigger Order Failed notification email to buyer (async)
            emailService.sendOrderFailedEmail(order);
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

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        BigDecimal totalRefund = orderItems.stream()
            .map(OrderItem::getPriceAtPurchase)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalRefund.compareTo(BigDecimal.ZERO) == 0 && order.getListing() != null) {
            totalRefund = order.getListing().getPrice();
        }

        String refundId = razorpayService.refundPayment(order.getPaymentReference(), totalRefund);
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

        // Trigger Order Refunded notification email to buyer (async)
        emailService.sendOrderRefundedEmail(order, totalRefund);

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
