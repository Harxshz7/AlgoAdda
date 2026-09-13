package com.algoadda.core.compliance.dto;

import com.algoadda.core.compliance.ReviewerType;

import java.time.Instant;
import java.util.UUID;

public class ComplianceCheckResponse {
    private UUID id;
    private UUID botVersionId;
    private ReviewerType reviewerType;
    private String checklistResults;
    private boolean passed;
    private Instant reviewedAt;

    public ComplianceCheckResponse() {
    }

    public ComplianceCheckResponse(UUID id, UUID botVersionId, ReviewerType reviewerType, String checklistResults, boolean passed, Instant reviewedAt) {
        this.id = id;
        this.botVersionId = botVersionId;
        this.reviewerType = reviewerType;
        this.checklistResults = checklistResults;
        this.passed = passed;
        this.reviewedAt = reviewedAt;
    }

    public static ComplianceCheckResponseBuilder builder() {
        return new ComplianceCheckResponseBuilder();
    }

    public static class ComplianceCheckResponseBuilder {
        private UUID id;
        private UUID botVersionId;
        private ReviewerType reviewerType;
        private String checklistResults;
        private boolean passed;
        private Instant reviewedAt;

        public ComplianceCheckResponseBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public ComplianceCheckResponseBuilder botVersionId(UUID botVersionId) {
            this.botVersionId = botVersionId;
            return this;
        }

        public ComplianceCheckResponseBuilder reviewerType(ReviewerType reviewerType) {
            this.reviewerType = reviewerType;
            return this;
        }

        public ComplianceCheckResponseBuilder checklistResults(String checklistResults) {
            this.checklistResults = checklistResults;
            return this;
        }

        public ComplianceCheckResponseBuilder passed(boolean passed) {
            this.passed = passed;
            return this;
        }

        public ComplianceCheckResponseBuilder reviewedAt(Instant reviewedAt) {
            this.reviewedAt = reviewedAt;
            return this;
        }

        public ComplianceCheckResponse build() {
            return new ComplianceCheckResponse(id, botVersionId, reviewerType, checklistResults, passed, reviewedAt);
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getBotVersionId() {
        return botVersionId;
    }

    public void setBotVersionId(UUID botVersionId) {
        this.botVersionId = botVersionId;
    }

    public ReviewerType getReviewerType() {
        return reviewerType;
    }

    public void setReviewerType(ReviewerType reviewerType) {
        this.reviewerType = reviewerType;
    }

    public String getChecklistResults() {
        return checklistResults;
    }

    public void setChecklistResults(String checklistResults) {
        this.checklistResults = checklistResults;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}
