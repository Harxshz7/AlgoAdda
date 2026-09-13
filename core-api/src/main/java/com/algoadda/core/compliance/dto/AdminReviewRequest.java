package com.algoadda.core.compliance.dto;

public class AdminReviewRequest {
    private boolean passed;
    private String notes;

    public AdminReviewRequest() {
    }

    public AdminReviewRequest(boolean passed, String notes) {
        this.passed = passed;
        this.notes = notes;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
