package com.algoadda.core.order.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class CreateOrderRequest {

    @NotNull(message = "listingId is required")
    private UUID listingId;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(UUID listingId) {
        this.listingId = listingId;
    }

    public UUID getListingId() {
        return listingId;
    }

    public void setListingId(UUID listingId) {
        this.listingId = listingId;
    }
}
