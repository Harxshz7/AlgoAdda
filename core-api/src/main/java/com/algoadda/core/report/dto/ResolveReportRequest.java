package com.algoadda.core.report.dto;

import com.algoadda.core.report.ResolveAction;

public class ResolveReportRequest {
    private ResolveAction action;
    private String notes;

    public ResolveReportRequest() {
    }

    public ResolveReportRequest(ResolveAction action, String notes) {
        this.action = action;
        this.notes = notes;
    }

    public ResolveAction getAction() {
        return action;
    }

    public void setAction(ResolveAction action) {
        this.action = action;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
