package com.algoadda.core.report;

import com.algoadda.core.listing.Listing;
import com.algoadda.core.listing.ListingRepository;
import com.algoadda.core.report.dto.CreateReportRequest;
import com.algoadda.core.report.dto.ListingReportResponse;
import com.algoadda.core.report.dto.ResolveReportRequest;
import com.algoadda.core.user.User;
import com.algoadda.core.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("null")
@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final ListingReportRepository reportRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public ReportService(
        ListingReportRepository reportRepository,
        ListingRepository listingRepository,
        UserRepository userRepository
    ) {
        this.reportRepository = reportRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ListingReportResponse createReport(UUID listingId, UUID reporterId, CreateReportRequest request) {
        Objects.requireNonNull(listingId, "listingId must not be null");
        Objects.requireNonNull(reporterId, "reporterId must not be null");
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(request.getReason(), "reason must not be null");

        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new IllegalArgumentException("Listing not found with id: " + listingId));

        User reporter = userRepository.findById(reporterId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + reporterId));

        // Prevent duplicate open reports from the same user on the same listing
        boolean alreadyReported = reportRepository.existsByListingIdAndReportedByIdAndStatus(
            listingId, reporterId, ReportStatus.OPEN);

        if (alreadyReported) {
            throw new IllegalArgumentException(
                "You already have an open report on this listing. Please wait for it to be reviewed.");
        }

        ListingReport report = ListingReport.builder()
            .listing(listing)
            .reportedBy(reporter)
            .reason(request.getReason())
            .comment(request.getComment())
            .status(ReportStatus.OPEN)
            .build();

        ListingReport saved = reportRepository.save(report);
        log.info("Created listing report {} for listing {} by user {}", saved.getId(), listingId, reporterId);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ListingReportResponse> getOpenReportsQueue() {
        List<ListingReport> openReports = reportRepository.findByStatusOrderByCreatedAtDesc(ReportStatus.OPEN);
        return openReports.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public ListingReportResponse resolveReport(UUID reportId, ResolveReportRequest request) {
        Objects.requireNonNull(reportId, "reportId must not be null");
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(request.getAction(), "action must not be null");

        ListingReport report = reportRepository.findById(reportId)
            .orElseThrow(() -> new IllegalArgumentException("Report not found with id: " + reportId));

        if (report.getStatus() != ReportStatus.OPEN) {
            throw new IllegalStateException("Report has already been resolved. Current status: " + report.getStatus());
        }

        if (request.getAction() == ResolveAction.FORCE_DELIST) {
            // Reuse existing soft-deactivation mechanism
            Listing listing = report.getListing();
            listing.setActive(false);
            listingRepository.save(listing);
            report.setStatus(ReportStatus.REVIEWED);
            log.info("Force-delisted listing {} via report {}", listing.getId(), reportId);
        } else if (request.getAction() == ResolveAction.DISMISS) {
            report.setStatus(ReportStatus.DISMISSED);
            log.info("Dismissed report {} for listing {}", reportId, report.getListing().getId());
        }

        report.setAdminNotes(request.getNotes());
        report.setReviewedAt(Instant.now());
        ListingReport saved = reportRepository.save(report);

        return mapToResponse(saved);
    }

    @Transactional
    public void forceDelistListing(UUID listingId) {
        Objects.requireNonNull(listingId, "listingId must not be null");

        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new IllegalArgumentException("Listing not found with id: " + listingId));

        listing.setActive(false);
        listingRepository.save(listing);
        log.info("Admin force-delisted listing {} (outside report flow)", listingId);
    }

    @Transactional
    public void suspendSeller(UUID sellerId) {
        Objects.requireNonNull(sellerId, "sellerId must not be null");

        User seller = userRepository.findById(sellerId)
            .orElseThrow(() -> new IllegalArgumentException("Seller not found with id: " + sellerId));

        seller.setSuspended(true);
        userRepository.save(seller);
        log.info("Suspended seller {} ({})", sellerId, seller.getEmail());
    }

    @Transactional
    public void unsuspendSeller(UUID sellerId) {
        Objects.requireNonNull(sellerId, "sellerId must not be null");

        User seller = userRepository.findById(sellerId)
            .orElseThrow(() -> new IllegalArgumentException("Seller not found with id: " + sellerId));

        seller.setSuspended(false);
        userRepository.save(seller);
        log.info("Unsuspended seller {} ({})", sellerId, seller.getEmail());
    }

    private ListingReportResponse mapToResponse(ListingReport report) {
        Listing listing = report.getListing();
        String listingName = "Unknown";
        try {
            if (listing.getBotVersion() != null && listing.getBotVersion().getBot() != null) {
                listingName = listing.getBotVersion().getBot().getName();
            }
        } catch (Exception e) {
            // Lazy loading might fail in edge cases
        }

        return new ListingReportResponse(
            report.getId(),
            listing.getId(),
            listingName,
            listing.isActive(),
            report.getReportedBy().getEmail(),
            report.getReason(),
            report.getComment(),
            report.getStatus(),
            report.getAdminNotes(),
            report.getCreatedAt(),
            report.getReviewedAt()
        );
    }
}
