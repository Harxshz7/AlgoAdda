package com.algoadda.core.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SellerOnboardRequest {

    @NotBlank(message = "Display name is required")
    @Size(min = 2, max = 150, message = "Display name must be between 2 and 150 characters")
    private String displayName;

    private String bio;

    public SellerOnboardRequest() {
    }

    public SellerOnboardRequest(String displayName, String bio) {
        this.displayName = displayName;
        this.bio = bio;
    }

    public static SellerOnboardRequestBuilder builder() {
        return new SellerOnboardRequestBuilder();
    }

    public static class SellerOnboardRequestBuilder {
        private String displayName;
        private String bio;

        public SellerOnboardRequestBuilder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public SellerOnboardRequestBuilder bio(String bio) {
            this.bio = bio;
            return this;
        }

        public SellerOnboardRequest build() {
            return new SellerOnboardRequest(displayName, bio);
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
