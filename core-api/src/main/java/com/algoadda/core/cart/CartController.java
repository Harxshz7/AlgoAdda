package com.algoadda.core.cart;

import com.algoadda.core.cart.dto.AddToCartRequest;
import com.algoadda.core.cart.dto.CartResponse;
import com.algoadda.core.config.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserPrincipal principal) {
        CartResponse response = cartService.getCart(principal.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<CartResponse> addItemToCart(
        @Valid @RequestBody AddToCartRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        CartResponse response = cartService.addItemToCart(principal.getId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{cartItemId}")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<CartResponse> removeItemFromCart(
        @PathVariable UUID cartItemId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        CartResponse response = cartService.removeItemFromCart(principal.getId(), cartItemId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<CartResponse> clearCart(@AuthenticationPrincipal UserPrincipal principal) {
        CartResponse response = cartService.clearCart(principal.getId());
        return ResponseEntity.ok(response);
    }
}
