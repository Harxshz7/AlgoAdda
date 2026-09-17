package com.algoadda.core.report.dto;

import com.algoadda.core.report.ReportReason;
import com.algoadda.core.report.ReportStatus;

import java.time.Instant;
import java.util.UUID;

public class ListingReportResponse {
    private UUID id;
    private UUID listingId;
    private String listingName;
    private boolean listingActive;
    private String reporterEmail;
    private ReportReason reason;
    private String comment;
    private ReportStatus status;
    private String adminNotes;
    private Instant createdAt;
    private Instant reviewedAt;

    public ListingReportResponse() {
    }

    public ListingReportResponse(UUID id, UUID listingId, String listingName, boolean listingActive,
                                  String reporterEmail, ReportReason reason, String comment,
                                  ReportStatus status, String adminNotes,
                                  Instant createdAt, Instant reviewedAt) {
        this.id = id;
        this.listingId = listingId;
        this.listingName = listingName;
        this.listingActive = listingActive;
        this.reporterEmail = reporterEmail;
        this.reason = reason;
        this.comment = comment;
        this.status = status;
        this.adminNotes = adminNotes;
        this.createdAt = createdAt;
        this.reviewedAt = reviewedAt;
    }

    // Getters and Setters

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

    public String getListingName() {
        return listingName;
    }

    public void setListingName(String listingName) {
        this.listingName = listingName;
    }

    public boolean isListingActive() {
        return listingActive;
    }

    public void setListingActive(boolean listingActive) {
        this.listingActive = listingActive;
    }

    public String getReporterEmail() {
        return reporterEmail;
    }

    public void setReporterEmail(String reporterEmail) {
        this.reporterEmail = reporterEmail;
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

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}
