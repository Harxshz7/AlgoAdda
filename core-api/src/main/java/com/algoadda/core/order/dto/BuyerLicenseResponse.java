package com.algoadda.core.order.dto;

import java.time.Instant;
import java.util.UUID;

public class BuyerLicenseResponse {

    private UUID licenseId;
    private UUID orderId;
    private UUID subscriptionId;
    private String subscriptionStatus;
    private String licenseType;
    private UUID botId;
    private String botName;
    private String versionNumber;
    private String sellerName;
    private boolean isOfficial;
    private Instant issuedAt;
    private Instant expiresAt;
    private boolean isPerpetual;
    private boolean isActive;
    private boolean revoked;

    public BuyerLicenseResponse() {
    }

    public BuyerLicenseResponse(UUID licenseId, UUID orderId, UUID botId, String botName, String versionNumber, String sellerName, boolean isOfficial, Instant issuedAt, Instant expiresAt, boolean isPerpetual, boolean isActive, boolean revoked) {
        this(licenseId, orderId, null, null, "ONE_TIME", botId, botName, versionNumber, sellerName, isOfficial, issuedAt, expiresAt, isPerpetual, isActive, revoked);
    }

    public BuyerLicenseResponse(UUID licenseId, UUID orderId, UUID subscriptionId, String subscriptionStatus, String licenseType, UUID botId, String botName, String versionNumber, String sellerName, boolean isOfficial, Instant issuedAt, Instant expiresAt, boolean isPerpetual, boolean isActive, boolean revoked) {
        this.licenseId = licenseId;
        this.orderId = orderId;
        this.subscriptionId = subscriptionId;
        this.subscriptionStatus = subscriptionStatus;
        this.licenseType = licenseType;
        this.botId = botId;
        this.botName = botName;
        this.versionNumber = versionNumber;
        this.sellerName = sellerName;
        this.isOfficial = isOfficial;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.isPerpetual = isPerpetual;
        this.isActive = isActive;
        this.revoked = revoked;
    }

    public UUID getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(UUID licenseId) {
        this.licenseId = licenseId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(String subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
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
        return isOfficial;
    }

    public void setOfficial(boolean official) {
        isOfficial = official;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isPerpetual() {
        return isPerpetual;
    }

    public void setPerpetual(boolean perpetual) {
        isPerpetual = perpetual;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }
}
