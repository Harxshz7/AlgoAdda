package com.algoadda.core.report;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ListingReportRepository extends JpaRepository<ListingReport, UUID> {
    List<ListingReport> findByStatusOrderByCreatedAtDesc(ReportStatus status);

    boolean existsByListingIdAndReportedByIdAndStatus(UUID listingId, UUID reportedById, ReportStatus status);

    List<ListingReport> findByListingId(UUID listingId);
}
