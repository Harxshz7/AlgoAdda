package com.algoadda.core.order;

import com.algoadda.core.bot.BotVersion;
import com.algoadda.core.cart.CartService;
import com.algoadda.core.listing.Listing;
import com.algoadda.core.order.dto.RefundResponse;
import com.algoadda.core.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
public class AdminRefundTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private LicenseRepository licenseRepository;

    @Mock
    private CartService cartService;

    @Mock
    private RazorpayService razorpayService;

    @InjectMocks
    private OrderService orderService;

    private Order paidOrder;
    private License activeLicense;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();

        User buyer = new User();
        buyer.setId(UUID.randomUUID());

        Listing listing = new Listing();
        listing.setId(UUID.randomUUID());
        listing.setPrice(new BigDecimal("4999.00"));

        BotVersion version = new BotVersion();
        version.setId(UUID.randomUUID());

        paidOrder = new Order();
        paidOrder.setId(orderId);
        paidOrder.setBuyer(buyer);
        paidOrder.setListing(listing);
        paidOrder.setPaymentReference("order_rzp_9999");
        paidOrder.setStatus(OrderStatus.PAID);

        activeLicense = new License();
        activeLicense.setId(UUID.randomUUID());
        activeLicense.setOrder(paidOrder);
        activeLicense.setBotVersion(version);
        activeLicense.setBuyer(buyer);
        activeLicense.setRevoked(false);
    }

    @Test
    void testRefundOrder_Success_RevokesLicenseAndUpdatesOrderStatus() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(paidOrder));
        when(razorpayService.refundPayment(eq("order_rzp_9999"), any(BigDecimal.class))).thenReturn("rfnd_12345678");
        when(licenseRepository.findByOrderId(orderId)).thenReturn(List.of(activeLicense));

        RefundResponse response = orderService.refundOrder(orderId);

        assertNotNull(response);
        assertEquals(orderId, response.getOrderId());
        assertEquals(OrderStatus.REFUNDED, response.getStatus());
        assertEquals("rfnd_12345678", response.getRefundId());
        assertTrue(response.isRevoked());

        assertEquals(OrderStatus.REFUNDED, paidOrder.getStatus());
        assertTrue(activeLicense.isRevoked());
        assertFalse(activeLicense.isActive());

        verify(orderRepository).save(paidOrder);
        verify(licenseRepository).save(activeLicense);
    }
}
