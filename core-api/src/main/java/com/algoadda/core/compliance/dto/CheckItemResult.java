package com.algoadda.core.compliance.dto;

public class CheckItemResult {
    private boolean passed;
    private String note;

    public CheckItemResult() {
    }

    public CheckItemResult(boolean passed, String note) {
        this.passed = passed;
        this.note = note;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
