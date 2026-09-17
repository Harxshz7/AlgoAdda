package com.algoadda.core.order;

import com.algoadda.core.cart.CartService;
import com.algoadda.core.email.EmailService;
import com.algoadda.core.user.User;
import com.algoadda.core.user.UserRepository;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceEmailIntegrationTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private LicenseRepository licenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartService cartService;

    @Mock
    private RazorpayService razorpayService;

    @Mock
    private EmailService emailService;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderService = new OrderService(
            orderRepository,
            orderItemRepository,
            licenseRepository,
            userRepository,
            cartService,
            razorpayService,
            emailService
        );
    }

    @Test
    @DisplayName("Webhook retry idempotency: Duplicate webhook calls fire purchase email exactly ONCE")
    void testDuplicateWebhookFiresEmailOnlyOnce() {
        String rzpOrderId = "order_test_123456";
        User buyer = User.builder().id(UUID.randomUUID()).email("buyer@example.com").build();
        Order pendingOrder = Order.builder()
            .id(UUID.randomUUID())
            .buyer(buyer)
            .status(OrderStatus.PENDING)
            .paymentReference(rzpOrderId)
            .build();

        JSONObject payloadObj = new JSONObject();
        payloadObj.put("event", "payment.captured");
        JSONObject rzpOrderObj = new JSONObject();
        rzpOrderObj.put("id", rzpOrderId);
        JSONObject entityObj = new JSONObject();
        entityObj.put("order_id", rzpOrderId);
        payloadObj.put("payload", new JSONObject().put("payment", new JSONObject().put("entity", entityObj)));

        String payload = payloadObj.toString();
        String sig = "valid_sig";

        when(razorpayService.verifyWebhookSignature(payload, sig)).thenReturn(true);
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(List.of(pendingOrder));

        // First webhook call (processes PENDING -> PAID)
        orderService.processWebhook(payload, sig);

        // Verify email sent ONCE on first webhook
        verify(emailService, times(1)).sendPurchaseConfirmationAndLicenseEmail(any(), any(), any());

        // Update pendingOrder status to simulate saved database state
        pendingOrder.setStatus(OrderStatus.PAID);
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(List.of());
        when(orderRepository.findByStatus(OrderStatus.PAID)).thenReturn(List.of(pendingOrder));

        // Duplicate retried webhook call
        orderService.processWebhook(payload, sig);

        // Verify email service was still called ONLY ONCE (no duplicate email on retries)
        verify(emailService, times(1)).sendPurchaseConfirmationAndLicenseEmail(any(), any(), any());
    }

    @Test
    @DisplayName("Order Refund trigger fires refund email exactly once")
    void testRefundFiresEmailOnce() {
        UUID orderId = UUID.randomUUID();
        User buyer = User.builder().id(UUID.randomUUID()).email("buyer@example.com").build();
        Order paidOrder = Order.builder()
            .id(orderId)
            .buyer(buyer)
            .status(OrderStatus.PAID)
            .paymentReference("order_rzp_pay_99")
            .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(paidOrder));
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(List.of());
        when(razorpayService.refundPayment(eq("order_rzp_pay_99"), any())).thenReturn("rfnd_12345");

        orderService.refundOrder(orderId);

        verify(emailService, times(1)).sendOrderRefundedEmail(eq(paidOrder), any(BigDecimal.class));
    }
}
