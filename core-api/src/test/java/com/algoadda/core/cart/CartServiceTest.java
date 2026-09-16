package com.algoadda.core.cart;

import com.algoadda.core.bot.Bot;
import com.algoadda.core.bot.BotVersion;
import com.algoadda.core.cart.dto.AddToCartRequest;
import com.algoadda.core.cart.dto.CartResponse;
import com.algoadda.core.listing.LicenseType;
import com.algoadda.core.listing.Listing;
import com.algoadda.core.listing.ListingRepository;
import com.algoadda.core.order.License;
import com.algoadda.core.order.LicenseRepository;
import com.algoadda.core.user.Role;
import com.algoadda.core.user.SellerProfileRepository;
import com.algoadda.core.user.User;
import com.algoadda.core.user.UserRepository;
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
public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private LicenseRepository licenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SellerProfileRepository sellerProfileRepository;

    @InjectMocks
    private CartService cartService;

    private User buyer;
    private Listing activeListing;
    private Listing inactiveListing;
    private Cart cart;
    private UUID buyerId;
    private UUID activeListingId;
    private UUID botVersionId;

    @BeforeEach
    void setUp() {
        buyerId = UUID.randomUUID();
        activeListingId = UUID.randomUUID();
        botVersionId = UUID.randomUUID();

        buyer = new User();
        buyer.setId(buyerId);
        buyer.setEmail("buyer@example.com");
        buyer.setRole(Role.BUYER);

        Bot seller = new Bot();
        User sellerUser = new User();
        sellerUser.setId(UUID.randomUUID());
        sellerUser.setEmail("seller@example.com");

        Bot bot = new Bot();
        bot.setId(UUID.randomUUID());
        bot.setName("Alpha Momentum Bot");
        bot.setSeller(sellerUser);
        bot.setStrategyType("MOMENTUM");

        BotVersion botVersion = new BotVersion();
        botVersion.setId(botVersionId);
        botVersion.setBot(bot);
        botVersion.setVersionNumber("1.0.0");

        activeListing = new Listing();
        activeListing.setId(activeListingId);
        activeListing.setBotVersion(botVersion);
        activeListing.setPrice(new BigDecimal("1999.00"));
        activeListing.setLicenseType(LicenseType.ONE_TIME);
        activeListing.setActive(true);

        inactiveListing = new Listing();
        inactiveListing.setId(UUID.randomUUID());
        inactiveListing.setBotVersion(botVersion);
        inactiveListing.setPrice(new BigDecimal("1999.00"));
        inactiveListing.setActive(false);

        cart = new Cart();
        cart.setId(UUID.randomUUID());
        cart.setBuyer(buyer);
        cart.setItems(new ArrayList<>());
    }

    @Test
    void testAddToCart_Success() {
        when(cartRepository.findByBuyerId(buyerId)).thenReturn(Optional.of(cart));
        when(listingRepository.findById(activeListingId)).thenReturn(Optional.of(activeListing));
        when(licenseRepository.findByBuyerId(buyerId)).thenReturn(List.of());
        when(cartItemRepository.findByCartIdAndListingId(cart.getId(), activeListingId)).thenReturn(Optional.empty());

        AddToCartRequest request = new AddToCartRequest(activeListingId);
        CartResponse response = cartService.addItemToCart(buyerId, request);

        assertNotNull(response);
        assertEquals(1, response.getItemCount());
        assertEquals(new BigDecimal("1999.00"), response.getTotalAmount());
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void testAddToCart_DuplicateListing_IsNoOp() {
        CartItem existingItem = CartItem.builder()
            .cart(cart)
            .listing(activeListing)
            .build();
        cart.getItems().add(existingItem);

        when(cartRepository.findByBuyerId(buyerId)).thenReturn(Optional.of(cart));
        when(listingRepository.findById(activeListingId)).thenReturn(Optional.of(activeListing));
        when(licenseRepository.findByBuyerId(buyerId)).thenReturn(List.of());
        when(cartItemRepository.findByCartIdAndListingId(cart.getId(), activeListingId)).thenReturn(Optional.of(existingItem));

        AddToCartRequest request = new AddToCartRequest(activeListingId);
        CartResponse response = cartService.addItemToCart(buyerId, request);

        assertNotNull(response);
        assertEquals(1, response.getItemCount());
    }

    @Test
    void testAddToCart_AlreadyOwnedLicense_ThrowsException() {
        License existingLicense = License.builder()
            .buyer(buyer)
            .botVersion(activeListing.getBotVersion())
            .revoked(false)
            .build();

        when(listingRepository.findById(activeListingId)).thenReturn(Optional.of(activeListing));
        when(licenseRepository.findByBuyerId(buyerId)).thenReturn(List.of(existingLicense));

        AddToCartRequest request = new AddToCartRequest(activeListingId);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            cartService.addItemToCart(buyerId, request);
        });

        assertTrue(ex.getMessage().contains("already own an active license"));
    }

    @Test
    void testAddToCart_InactiveListing_ThrowsException() {
        when(listingRepository.findById(inactiveListing.getId())).thenReturn(Optional.of(inactiveListing));

        AddToCartRequest request = new AddToCartRequest(inactiveListing.getId());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            cartService.addItemToCart(buyerId, request);
        });

        assertTrue(ex.getMessage().contains("not active"));
    }
}
