package com.algoadda.core.order;

import com.algoadda.core.bot.Bot;
import com.algoadda.core.bot.BotVersion;
import com.algoadda.core.listing.LicenseType;
import com.algoadda.core.listing.Listing;
import com.algoadda.core.listing.ListingRepository;
import com.algoadda.core.order.dto.CreateOrderRequest;
import com.algoadda.core.order.dto.OrderResponse;
import com.algoadda.core.user.Role;
import com.algoadda.core.user.User;
import com.algoadda.core.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private LicenseRepository licenseRepository;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RazorpayService razorpayService;

    @InjectMocks
    private OrderService orderService;

    private User buyer;
    private Listing activeListing;
    private Listing inactiveListing;
    private UUID buyerId;
    private UUID activeListingId;
    private UUID inactiveListingId;

    @BeforeEach
    void setUp() {
        buyerId = UUID.randomUUID();
        activeListingId = UUID.randomUUID();
        inactiveListingId = UUID.randomUUID();

        buyer = new User();
        buyer.setId(buyerId);
        buyer.setEmail("buyer@example.com");
        buyer.setRole(Role.BUYER);

        Bot bot = new Bot();
        bot.setId(UUID.randomUUID());
        bot.setName("Golden Cross Bot");

        BotVersion botVersion = new BotVersion();
        botVersion.setId(UUID.randomUUID());
        botVersion.setBot(bot);
        botVersion.setVersionNumber("1.0.0");
        botVersion.setDisclosedLogic("SMA Cross logic");

        activeListing = new Listing();
        activeListing.setId(activeListingId);
        activeListing.setBotVersion(botVersion);
        activeListing.setPrice(new BigDecimal("4999.00"));
        activeListing.setLicenseType(LicenseType.ONE_TIME);
        activeListing.setActive(true);

        inactiveListing = new Listing();
        inactiveListing.setId(inactiveListingId);
        inactiveListing.setBotVersion(botVersion);
        inactiveListing.setPrice(new BigDecimal("2999.00"));
        inactiveListing.setLicenseType(LicenseType.ONE_TIME);
        inactiveListing.setActive(false);
    }

    @Test
    void testCreateOrder_ActiveListing_Success() {
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(listingRepository.findById(activeListingId)).thenReturn(Optional.of(activeListing));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            if (o.getId() == null) {
                o.setId(UUID.randomUUID());
            }
            return o;
        });
        when(razorpayService.createOrder(any(BigDecimal.class), anyString())).thenReturn("order_test_12345");
        when(razorpayService.getKeyId()).thenReturn("rzp_test_dummyKeyId");

        CreateOrderRequest request = new CreateOrderRequest(activeListingId);
        OrderResponse response = orderService.createOrder(buyerId, request);

        assertNotNull(response);
        assertNotNull(response.getOrderId());
        assertEquals("order_test_12345", response.getRazorpayOrderId());
        assertEquals(new BigDecimal("4999.00"), response.getAmount());
        assertEquals(499900L, response.getAmountInPaise());
        assertEquals("INR", response.getCurrency());
        assertEquals(OrderStatus.PENDING, response.getStatus());

        verify(orderRepository, times(2)).save(any(Order.class));
    }

    @Test
    void testCreateOrder_InactiveListing_ThrowsException() {
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(listingRepository.findById(inactiveListingId)).thenReturn(Optional.of(inactiveListing));

        CreateOrderRequest request = new CreateOrderRequest(inactiveListingId);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(buyerId, request);
        });

        assertTrue(exception.getMessage().contains("not active"));
        verify(orderRepository, never()).save(any(Order.class));
    }
}
