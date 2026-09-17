package com.algoadda.core.order;

import com.algoadda.core.bot.Bot;
import com.algoadda.core.bot.BotVersion;
import com.algoadda.core.cart.Cart;
import com.algoadda.core.cart.CartItem;
import com.algoadda.core.cart.CartService;
import com.algoadda.core.listing.LicenseType;
import com.algoadda.core.listing.Listing;
import com.algoadda.core.listing.ListingRepository;
import com.algoadda.core.order.dto.CreateOrderRequest;
import com.algoadda.core.order.dto.OrderResponse;
import com.algoadda.core.user.Role;
import com.algoadda.core.user.User;
import com.algoadda.core.user.UserRepository;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private LicenseRepository licenseRepository;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartService cartService;

    @Mock
    private RazorpayService razorpayService;

    @Mock
    private com.algoadda.core.email.EmailService emailService;

    @InjectMocks
    private OrderService orderService;

    private User buyer;
    private Listing activeListing1;
    private Listing activeListing2;
    private Listing inactiveListing;
    private Cart cart;
    private UUID buyerId;
    private UUID activeListingId1;
    private UUID activeListingId2;

    @BeforeEach
    void setUp() {
        buyerId = UUID.randomUUID();
        activeListingId1 = UUID.randomUUID();
        activeListingId2 = UUID.randomUUID();

        buyer = new User();
        buyer.setId(buyerId);
        buyer.setEmail("buyer@example.com");
        buyer.setRole(Role.BUYER);

        Bot bot1 = new Bot();
        bot1.setId(UUID.randomUUID());
        bot1.setName("Golden Cross Bot");

        BotVersion botVersion1 = new BotVersion();
        botVersion1.setId(UUID.randomUUID());
        botVersion1.setBot(bot1);
        botVersion1.setVersionNumber("1.0.0");

        activeListing1 = new Listing();
        activeListing1.setId(activeListingId1);
        activeListing1.setBotVersion(botVersion1);
        activeListing1.setPrice(new BigDecimal("4999.00"));
        activeListing1.setLicenseType(LicenseType.ONE_TIME);
        activeListing1.setActive(true);

        Bot bot2 = new Bot();
        bot2.setId(UUID.randomUUID());
        bot2.setName("RSI Mean Reversion Bot");

        BotVersion botVersion2 = new BotVersion();
        botVersion2.setId(UUID.randomUUID());
        botVersion2.setBot(bot2);
        botVersion2.setVersionNumber("2.0.0");

        activeListing2 = new Listing();
        activeListing2.setId(activeListingId2);
        activeListing2.setBotVersion(botVersion2);
        activeListing2.setPrice(new BigDecimal("2999.00"));
        activeListing2.setLicenseType(LicenseType.ONE_TIME);
        activeListing2.setActive(true);

        inactiveListing = new Listing();
        inactiveListing.setId(UUID.randomUUID());
        inactiveListing.setBotVersion(botVersion1);
        inactiveListing.setPrice(new BigDecimal("1000.00"));
        inactiveListing.setActive(false);

        cart = new Cart();
        cart.setId(UUID.randomUUID());
        cart.setBuyer(buyer);
        cart.setItems(new ArrayList<>());
    }

    @Test
    void testCreateOrder_MultiItemCart_Success() {
        CartItem item1 = CartItem.builder().cart(cart).listing(activeListing1).build();
        CartItem item2 = CartItem.builder().cart(cart).listing(activeListing2).build();
        cart.getItems().add(item1);
        cart.getItems().add(item2);

        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(cartService.getOrCreateCartEntity(buyerId)).thenReturn(cart);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            if (o.getId() == null) {
                o.setId(UUID.randomUUID());
            }
            return o;
        });
        when(razorpayService.createOrder(any(BigDecimal.class), anyString())).thenReturn("order_multi_12345");
        when(razorpayService.getKeyId()).thenReturn("rzp_test_dummyKeyId");

        CreateOrderRequest request = new CreateOrderRequest();
        OrderResponse response = orderService.createOrder(buyerId, request);

        assertNotNull(response);
        assertNotNull(response.getOrderId());
        assertEquals("order_multi_12345", response.getRazorpayOrderId());
        assertEquals(new BigDecimal("7998.00"), response.getAmount());
        assertEquals(799800L, response.getAmountInPaise());
        assertEquals(OrderStatus.PENDING, response.getStatus());

        verify(orderItemRepository).saveAll(anyList());
        verify(orderRepository, times(2)).save(any(Order.class));
    }

    @Test
    void testCreateOrder_InactiveItemInCart_ThrowsClearError() {
        CartItem item1 = CartItem.builder().cart(cart).listing(inactiveListing).build();
        cart.getItems().add(item1);

        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(cartService.getOrCreateCartEntity(buyerId)).thenReturn(cart);

        CreateOrderRequest request = new CreateOrderRequest();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(buyerId, request);
        });

        assertTrue(ex.getMessage().contains("no longer active"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testProcessWebhook_MultiItemOrder_IssuesOneLicensePerItemAndClearsCart() {
        String rzpOrderId = "order_rzp_9999";
        Order pendingOrder = Order.builder()
            .id(UUID.randomUUID())
            .buyer(buyer)
            .paymentReference(rzpOrderId)
            .status(OrderStatus.PENDING)
            .build();

        OrderItem item1 = OrderItem.builder().order(pendingOrder).botVersion(activeListing1.getBotVersion()).priceAtPurchase(activeListing1.getPrice()).build();
        OrderItem item2 = OrderItem.builder().order(pendingOrder).botVersion(activeListing2.getBotVersion()).priceAtPurchase(activeListing2.getPrice()).build();

        when(razorpayService.verifyWebhookSignature(anyString(), anyString())).thenReturn(true);
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(List.of(pendingOrder));
        when(orderItemRepository.findByOrderId(pendingOrder.getId())).thenReturn(List.of(item1, item2));
        when(licenseRepository.findByOrderId(pendingOrder.getId())).thenReturn(List.of());

        JSONObject payloadJson = new JSONObject();
        payloadJson.put("event", "payment.captured");
        JSONObject entity = new JSONObject();
        entity.put("order_id", rzpOrderId);
        JSONObject payment = new JSONObject();
        payment.put("entity", entity);
        JSONObject payload = new JSONObject();
        payload.put("payment", payment);
        payloadJson.put("payload", payload);

        orderService.processWebhook(payloadJson.toString(), "valid_sig");

        assertEquals(OrderStatus.PAID, pendingOrder.getStatus());
        verify(licenseRepository, times(2)).save(any(License.class));
        verify(cartService).clearCart(buyerId);
    }

    @Test
    void testProcessWebhook_DuplicateWebhook_IdempotencySafeguardHolds() {
        String rzpOrderId = "order_rzp_paid";
        Order paidOrder = Order.builder()
            .id(UUID.randomUUID())
            .buyer(buyer)
            .paymentReference(rzpOrderId)
            .status(OrderStatus.PAID)
            .build();

        when(razorpayService.verifyWebhookSignature(anyString(), anyString())).thenReturn(true);
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(List.of());
        when(orderRepository.findByStatus(OrderStatus.PAID)).thenReturn(List.of(paidOrder));

        JSONObject payloadJson = new JSONObject();
        payloadJson.put("event", "payment.captured");
        JSONObject entity = new JSONObject();
        entity.put("order_id", rzpOrderId);
        JSONObject payment = new JSONObject();
        payment.put("entity", entity);
        JSONObject payload = new JSONObject();
        payload.put("payment", payment);
        payloadJson.put("payload", payload);

        orderService.processWebhook(payloadJson.toString(), "valid_sig");

        verify(licenseRepository, never()).save(any(License.class));
        verify(cartService, never()).clearCart(any(UUID.class));
    }
}
