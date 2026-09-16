package com.algoadda.core.cart.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class CartResponse {

    private UUID cartId;
    private List<CartItemResponse> items;
    private BigDecimal totalAmount;
    private int itemCount;

    public CartResponse() {
    }

    public CartResponse(UUID cartId, List<CartItemResponse> items, BigDecimal totalAmount, int itemCount) {
        this.cartId = cartId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.itemCount = itemCount;
    }

    public UUID getCartId() {
        return cartId;
    }

    public void setCartId(UUID cartId) {
        this.cartId = cartId;
    }

    public List<CartItemResponse> getItems() {
        return items;
    }

    public void setItems(List<CartItemResponse> items) {
        this.items = items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }
}
