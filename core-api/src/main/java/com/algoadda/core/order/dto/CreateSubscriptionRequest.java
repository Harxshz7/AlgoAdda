package com.algoadda.core.order.dto;

import java.util.UUID;

public class CreateSubscriptionRequest {
    private UUID listingId;

    public CreateSubscriptionRequest() {
    }

    public CreateSubscriptionRequest(UUID listingId) {
        this.listingId = listingId;
    }

    public UUID getListingId() {
        return listingId;
    }

    public void setListingId(UUID listingId) {
        this.listingId = listingId;
    }
}
