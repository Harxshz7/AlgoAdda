package com.algoadda.core.order;

import com.algoadda.core.bot.Bot;
import com.algoadda.core.bot.BotVersion;
import com.algoadda.core.listing.LicenseType;
import com.algoadda.core.listing.Listing;
import com.algoadda.core.user.Role;
import com.algoadda.core.user.User;
import com.algoadda.core.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentWebhookTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private LicenseRepository licenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RazorpayService razorpayService;

    @InjectMocks
    private OrderService orderService;

    private User buyer;
    private BotVersion purchasedVersion;
    private Listing listing;
    private Order pendingOrder;
    private String razorpayOrderId;

    @BeforeEach
    void setUp() {
        razorpayOrderId = "order_rzp_9876543210";

        buyer = new User();
        buyer.setId(UUID.randomUUID());
        buyer.setEmail("buyer@example.com");
        buyer.setRole(Role.BUYER);

        Bot bot = new Bot();
        bot.setId(UUID.randomUUID());
        bot.setName("RSI Mean Reversion");

        purchasedVersion = new BotVersion();
        purchasedVersion.setId(UUID.randomUUID());
        purchasedVersion.setBot(bot);
        purchasedVersion.setVersionNumber("1.0.0");

        listing = new Listing();
        listing.setId(UUID.randomUUID());
        listing.setBotVersion(purchasedVersion);
        listing.setPrice(new BigDecimal("9999.00"));
        listing.setLicenseType(LicenseType.ONE_TIME);
        listing.setActive(true);

        pendingOrder = new Order();
        pendingOrder.setId(UUID.randomUUID());
        pendingOrder.setBuyer(buyer);
        pendingOrder.setListing(listing);
        pendingOrder.setPaymentReference(razorpayOrderId);
        pendingOrder.setStatus(OrderStatus.PENDING);
    }

    @Test
    void testWebhook_InvalidSignature_Rejected() {
        String payload = "{\"event\":\"payment.captured\"}";
        String invalidSignature = "bad_signature";

        when(razorpayService.verifyWebhookSignature(payload, invalidSignature)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            orderService.processWebhook(payload, invalidSignature);
        });

        assertTrue(ex.getMessage().contains("Invalid Razorpay webhook signature"));
        verify(orderRepository, never()).save(any(Order.class));
        verify(licenseRepository, never()).save(any(License.class));
    }

    @Test
    void testWebhook_PaymentCaptured_IssuesLicenseForExactVersion() {
        String validSignature = "valid_sig_123";
        String payload = "{\n" +
            "  \"event\": \"payment.captured\",\n" +
            "  \"payload\": {\n" +
            "    \"payment\": {\n" +
            "      \"entity\": {\n" +
            "        \"id\": \"pay_123\",\n" +
            "        \"order_id\": \"" + razorpayOrderId + "\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

        when(razorpayService.verifyWebhookSignature(payload, validSignature)).thenReturn(true);
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(List.of(pendingOrder));
        when(licenseRepository.findByOrderId(pendingOrder.getId())).thenReturn(Collections.emptyList());

        orderService.processWebhook(payload, validSignature);

        assertEquals(OrderStatus.PAID, pendingOrder.getStatus());
        verify(orderRepository).save(pendingOrder);

        ArgumentCaptor<License> licenseCaptor = ArgumentCaptor.forClass(License.class);
        verify(licenseRepository).save(licenseCaptor.capture());

        License createdLicense = licenseCaptor.getValue();
        assertNotNull(createdLicense);
        assertEquals(pendingOrder, createdLicense.getOrder());
        assertEquals(purchasedVersion.getId(), createdLicense.getBotVersion().getId());
        assertEquals("1.0.0", createdLicense.getBotVersion().getVersionNumber());
        assertEquals(buyer, createdLicense.getBuyer());
        assertFalse(createdLicense.isRevoked());
        assertTrue(createdLicense.isPerpetual());
    }

    @Test
    void testWebhook_Idempotency_DuplicatePayloadIgnored() {
        String validSignature = "valid_sig_123";
        String payload = "{\n" +
            "  \"event\": \"payment.captured\",\n" +
            "  \"payload\": {\n" +
            "    \"payment\": {\n" +
            "      \"entity\": {\n" +
            "        \"id\": \"pay_123\",\n" +
            "        \"order_id\": \"" + razorpayOrderId + "\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

        Order paidOrder = new Order();
        paidOrder.setId(pendingOrder.getId());
        paidOrder.setBuyer(buyer);
        paidOrder.setListing(listing);
        paidOrder.setPaymentReference(razorpayOrderId);
        paidOrder.setStatus(OrderStatus.PAID);

        when(razorpayService.verifyWebhookSignature(payload, validSignature)).thenReturn(true);
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(Collections.emptyList());
        when(orderRepository.findByStatus(OrderStatus.PAID)).thenReturn(List.of(paidOrder));

        orderService.processWebhook(payload, validSignature);

        verify(orderRepository, never()).save(any(Order.class));
        verify(licenseRepository, never()).save(any(License.class));
    }

    @Test
    void testWebhook_PaymentFailed_MarksOrderFailed() {
        String validSignature = "valid_sig_123";
        String payload = "{\n" +
            "  \"event\": \"payment.failed\",\n" +
            "  \"payload\": {\n" +
            "    \"payment\": {\n" +
            "      \"entity\": {\n" +
            "        \"id\": \"pay_failed_123\",\n" +
            "        \"order_id\": \"" + razorpayOrderId + "\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

        when(razorpayService.verifyWebhookSignature(payload, validSignature)).thenReturn(true);
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(List.of(pendingOrder));

        orderService.processWebhook(payload, validSignature);

        assertEquals(OrderStatus.FAILED, pendingOrder.getStatus());
        verify(orderRepository).save(pendingOrder);
        verify(licenseRepository, never()).save(any(License.class));
    }
}
