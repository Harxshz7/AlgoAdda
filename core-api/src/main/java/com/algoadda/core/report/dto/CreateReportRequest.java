package com.algoadda.core.report.dto;

import com.algoadda.core.report.ReportReason;

public class CreateReportRequest {
    private ReportReason reason;
    private String comment;

    public CreateReportRequest() {
    }

    public CreateReportRequest(ReportReason reason, String comment) {
        this.reason = reason;
        this.comment = comment;
    }

    public ReportReason getReason() {
        return reason;
    }

    public void setReason(ReportReason reason) {
        this.reason = reason;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
