package com.algoadda.core.order.dto;

import com.algoadda.core.order.SubscriptionStatus;

import java.time.Instant;
import java.util.UUID;

public class SubscriptionResponse {
    private UUID subscriptionId;
    private UUID listingId;
    private UUID botId;
    private String botName;
    private String razorpaySubscriptionId;
    private SubscriptionStatus status;
    private Instant currentPeriodEnd;
    private Instant createdAt;

    public SubscriptionResponse() {
    }

    public SubscriptionResponse(UUID subscriptionId, UUID listingId, UUID botId, String botName, String razorpaySubscriptionId, SubscriptionStatus status, Instant currentPeriodEnd, Instant createdAt) {
        this.subscriptionId = subscriptionId;
        this.listingId = listingId;
        this.botId = botId;
        this.botName = botName;
        this.razorpaySubscriptionId = razorpaySubscriptionId;
        this.status = status;
        this.currentPeriodEnd = currentPeriodEnd;
        this.createdAt = createdAt;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public UUID getListingId() {
        return listingId;
    }

    public void setListingId(UUID listingId) {
        this.listingId = listingId;
    }

    public UUID getBotId() {
        return botId;
    }

    public void setBotId(UUID botId) {
        this.botId = botId;
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }

    public String getRazorpaySubscriptionId() {
        return razorpaySubscriptionId;
    }

    public void setRazorpaySubscriptionId(String razorpaySubscriptionId) {
        this.razorpaySubscriptionId = razorpaySubscriptionId;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public Instant getCurrentPeriodEnd() {
        return currentPeriodEnd;
    }

    public void setCurrentPeriodEnd(Instant currentPeriodEnd) {
        this.currentPeriodEnd = currentPeriodEnd;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
