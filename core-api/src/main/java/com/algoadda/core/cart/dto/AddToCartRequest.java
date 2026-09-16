package com.algoadda.core.cart.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class AddToCartRequest {

    @NotNull(message = "listingId is required")
    private UUID listingId;

    public AddToCartRequest() {
    }

    public AddToCartRequest(UUID listingId) {
        this.listingId = listingId;
    }

    public UUID getListingId() {
        return listingId;
    }

    public void setListingId(UUID listingId) {
        this.listingId = listingId;
    }
}
