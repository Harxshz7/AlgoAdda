package com.algoadda.core.report;

import com.algoadda.core.listing.Listing;
import com.algoadda.core.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "listing_reports")
public class ListingReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by", nullable = false)
    private User reportedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ReportReason reason;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ReportStatus status = ReportStatus.OPEN;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    public ListingReport() {
    }

    public ListingReport(UUID id, Listing listing, User reportedBy, ReportReason reason,
                         String comment, ReportStatus status, String adminNotes,
                         Instant createdAt, Instant reviewedAt) {
        this.id = id;
        this.listing = listing;
        this.reportedBy = reportedBy;
        this.reason = reason;
        this.comment = comment;
        this.status = status != null ? status : ReportStatus.OPEN;
        this.adminNotes = adminNotes;
        this.createdAt = createdAt;
        this.reviewedAt = reviewedAt;
    }

    public static ListingReportBuilder builder() {
        return new ListingReportBuilder();
    }

    public static class ListingReportBuilder {
        private UUID id;
        private Listing listing;
        private User reportedBy;
        private ReportReason reason;
        private String comment;
        private ReportStatus status = ReportStatus.OPEN;
        private String adminNotes;
        private Instant createdAt;
        private Instant reviewedAt;

        public ListingReportBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public ListingReportBuilder listing(Listing listing) {
            this.listing = listing;
            return this;
        }

        public ListingReportBuilder reportedBy(User reportedBy) {
            this.reportedBy = reportedBy;
            return this;
        }

        public ListingReportBuilder reason(ReportReason reason) {
            this.reason = reason;
            return this;
        }

        public ListingReportBuilder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public ListingReportBuilder status(ReportStatus status) {
            this.status = status;
            return this;
        }

        public ListingReportBuilder adminNotes(String adminNotes) {
            this.adminNotes = adminNotes;
            return this;
        }

        public ListingReportBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ListingReportBuilder reviewedAt(Instant reviewedAt) {
            this.reviewedAt = reviewedAt;
            return this;
        }

        public ListingReport build() {
            return new ListingReport(id, listing, reportedBy, reason, comment, status, adminNotes, createdAt, reviewedAt);
        }
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Listing getListing() {
        return listing;
    }

    public void setListing(Listing listing) {
        this.listing = listing;
    }

    public User getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(User reportedBy) {
        this.reportedBy = reportedBy;
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
