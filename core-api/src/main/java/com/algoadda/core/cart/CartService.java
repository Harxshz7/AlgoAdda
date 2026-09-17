package com.algoadda.core.cart;

import com.algoadda.core.cart.dto.AddToCartRequest;
import com.algoadda.core.cart.dto.CartItemResponse;
import com.algoadda.core.cart.dto.CartResponse;
import com.algoadda.core.listing.Listing;
import com.algoadda.core.listing.ListingRepository;
import com.algoadda.core.order.License;
import com.algoadda.core.order.LicenseRepository;
import com.algoadda.core.user.SellerProfile;
import com.algoadda.core.user.SellerProfileRepository;
import com.algoadda.core.user.User;
import com.algoadda.core.user.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class CartService {


    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ListingRepository listingRepository;
    private final LicenseRepository licenseRepository;
    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;

    public CartService(
        CartRepository cartRepository,
        CartItemRepository cartItemRepository,
        ListingRepository listingRepository,
        LicenseRepository licenseRepository,
        UserRepository userRepository,
        SellerProfileRepository sellerProfileRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.listingRepository = listingRepository;
        this.licenseRepository = licenseRepository;
        this.userRepository = userRepository;
        this.sellerProfileRepository = sellerProfileRepository;
    }

    @Transactional
    public Cart getOrCreateCartEntity(UUID buyerId) {
        Objects.requireNonNull(buyerId, "buyerId must not be null");
        return cartRepository.findByBuyerId(buyerId).orElseGet(() -> {
            User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new IllegalArgumentException("Buyer not found"));
            Cart newCart = Cart.builder().buyer(buyer).build();
            return cartRepository.save(newCart);
        });
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(UUID buyerId) {
        Cart cart = getOrCreateCartEntity(buyerId);
        return mapToCartResponse(cart);
    }

    @Transactional
    public CartResponse addItemToCart(UUID buyerId, AddToCartRequest request) {
        Objects.requireNonNull(buyerId, "buyerId must not be null");
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(request.getListingId(), "listingId must not be null");

        Listing listing = listingRepository.findById(request.getListingId())
            .orElseThrow(() -> new IllegalArgumentException("Listing not found"));

        if (!listing.isActive()) {
            throw new IllegalArgumentException("Listing is not active or available for purchase");
        }

        // Check if buyer already owns an active license for this exact BotVersion
        List<License> existingLicenses = licenseRepository.findByBuyerId(buyerId);
        boolean alreadyOwned = existingLicenses.stream()
            .anyMatch(l -> l.isActive() && l.getBotVersion().getId().equals(listing.getBotVersion().getId()));

        if (alreadyOwned) {
            throw new IllegalArgumentException("You already own an active license for this algorithm version");
        }

        Cart cart = getOrCreateCartEntity(buyerId);

        // Enforce uniqueness (cart_id, listing_id)
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndListingId(cart.getId(), listing.getId());
        if (existingItem.isEmpty()) {
            CartItem cartItem = CartItem.builder()
                .cart(cart)
                .listing(listing)
                .build();
            cart.getItems().add(cartItem);
            cartRepository.save(cart);
        }

        return mapToCartResponse(cart);
    }

    @Transactional
    public CartResponse removeItemFromCart(UUID buyerId, UUID cartItemId) {
        Objects.requireNonNull(buyerId, "buyerId must not be null");
        Objects.requireNonNull(cartItemId, "cartItemId must not be null");

        Cart cart = getOrCreateCartEntity(buyerId);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new AccessDeniedException("Cart item does not belong to buyer's cart");
        }

        cart.getItems().removeIf(item -> item.getId().equals(cartItemId));
        cartItemRepository.delete(cartItem);
        cartRepository.save(cart);

        return mapToCartResponse(cart);
    }

    @Transactional
    public CartResponse clearCart(UUID buyerId) {
        Objects.requireNonNull(buyerId, "buyerId must not be null");
        Cart cart = getOrCreateCartEntity(buyerId);
        cart.getItems().clear();
        cartRepository.save(cart);
        return mapToCartResponse(cart);
    }

    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream().map(item -> {
            Listing listing = item.getListing();
            UUID sellerId = listing.getBotVersion().getBot().getSeller().getId();
            String sellerName = sellerProfileRepository.findByUserId(sellerId)
                .map(SellerProfile::getDisplayName)
                .orElse(listing.getBotVersion().getBot().getSeller().getEmail());

            return new CartItemResponse(
                item.getId(),
                listing.getId(),
                listing.getBotVersion().getBot().getName(),
                listing.getBotVersion().getBot().getStrategyType(),
                listing.getBotVersion().getVersionNumber(),
                sellerName,
                listing.isOfficial(),
                listing.getPrice(),
                item.getAddedAt()
            );
        }).collect(Collectors.toList());

        BigDecimal totalAmount = itemResponses.stream()
            .map(CartItemResponse::getPrice)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), itemResponses, totalAmount, itemResponses.size());
    }
}
