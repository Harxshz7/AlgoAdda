package com.algoadda.core.compliance;

import com.algoadda.core.bot.BotVersion;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "compliance_checks")
public class ComplianceCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bot_version_id", nullable = false)
    private BotVersion botVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "reviewer_type", nullable = false, length = 50)
    private ReviewerType reviewerType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "checklist_results")
    private String checklistResults;

    @Column(nullable = false)
    private boolean passed = false;

    @CreationTimestamp
    @Column(name = "reviewed_at", nullable = false, updatable = false)
    private Instant reviewedAt;

    public ComplianceCheck() {
    }

    public ComplianceCheck(UUID id, BotVersion botVersion, ReviewerType reviewerType, String checklistResults, boolean passed, Instant reviewedAt) {
        this.id = id;
        this.botVersion = botVersion;
        this.reviewerType = reviewerType;
        this.checklistResults = checklistResults;
        this.passed = passed;
        this.reviewedAt = reviewedAt;
    }

    public static ComplianceCheckBuilder builder() {
        return new ComplianceCheckBuilder();
    }

    public static class ComplianceCheckBuilder {
        private UUID id;
        private BotVersion botVersion;
        private ReviewerType reviewerType;
        private String checklistResults;
        private boolean passed = false;
        private Instant reviewedAt;

        public ComplianceCheckBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public ComplianceCheckBuilder botVersion(BotVersion botVersion) {
            this.botVersion = botVersion;
            return this;
        }

        public ComplianceCheckBuilder reviewerType(ReviewerType reviewerType) {
            this.reviewerType = reviewerType;
            return this;
        }

        public ComplianceCheckBuilder checklistResults(String checklistResults) {
            this.checklistResults = checklistResults;
            return this;
        }

        public ComplianceCheckBuilder passed(boolean passed) {
            this.passed = passed;
            return this;
        }

        public ComplianceCheckBuilder reviewedAt(Instant reviewedAt) {
            this.reviewedAt = reviewedAt;
            return this;
        }

        public ComplianceCheck build() {
            return new ComplianceCheck(id, botVersion, reviewerType, checklistResults, passed, reviewedAt);
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BotVersion getBotVersion() {
        return botVersion;
    }

    public void setBotVersion(BotVersion botVersion) {
        this.botVersion = botVersion;
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
