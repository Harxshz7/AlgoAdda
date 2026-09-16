package com.algoadda.core.order.dto;

import java.time.Instant;

public class DownloadLicenseResponse {

    private String downloadUrl;
    private Instant expiresAt;

    public DownloadLicenseResponse() {
    }

    public DownloadLicenseResponse(String downloadUrl, Instant expiresAt) {
        this.downloadUrl = downloadUrl;
        this.expiresAt = expiresAt;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
