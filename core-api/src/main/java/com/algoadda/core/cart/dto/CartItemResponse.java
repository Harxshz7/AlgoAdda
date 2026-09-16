package com.algoadda.core.cart.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class CartItemResponse {

    private UUID id;
    private UUID listingId;
    private String botName;
    private String strategyType;
    private String versionNumber;
    private String sellerName;
    private boolean official;
    private BigDecimal price;
    private Instant addedAt;

    public CartItemResponse() {
    }

    public CartItemResponse(UUID id, UUID listingId, String botName, String strategyType, String versionNumber, String sellerName, boolean official, BigDecimal price, Instant addedAt) {
        this.id = id;
        this.listingId = listingId;
        this.botName = botName;
        this.strategyType = strategyType;
        this.versionNumber = versionNumber;
        this.sellerName = sellerName;
        this.official = official;
        this.price = price;
        this.addedAt = addedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getListingId() {
        return listingId;
    }

    public void setListingId(UUID listingId) {
        this.listingId = listingId;
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }

    public String getStrategyType() {
        return strategyType;
    }

    public void setStrategyType(String strategyType) {
        this.strategyType = strategyType;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public boolean isOfficial() {
        return official;
    }

    public void setOfficial(boolean official) {
        this.official = official;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Instant getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Instant addedAt) {
        this.addedAt = addedAt;
    }
}
