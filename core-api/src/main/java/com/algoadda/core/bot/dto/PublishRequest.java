package com.algoadda.core.bot.dto;

import com.algoadda.core.listing.LicenseType;
import java.math.BigDecimal;

public class PublishRequest {
    private BigDecimal price;
    private LicenseType licenseType;

    public PublishRequest() {
    }

    public PublishRequest(BigDecimal price, LicenseType licenseType) {
        this.price = price;
        this.licenseType = licenseType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LicenseType getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(LicenseType licenseType) {
        this.licenseType = licenseType;
    }
}
