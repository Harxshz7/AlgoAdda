package com.algoadda.core.report;

import com.algoadda.core.listing.Listing;
import com.algoadda.core.listing.ListingRepository;
import com.algoadda.core.listing.LicenseType;
import com.algoadda.core.report.dto.CreateReportRequest;
import com.algoadda.core.report.dto.ListingReportResponse;
import com.algoadda.core.report.dto.ResolveReportRequest;
import com.algoadda.core.user.Role;
import com.algoadda.core.user.User;
import com.algoadda.core.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ReportServiceTest {

    @Mock
    private ListingReportRepository reportRepository;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReportService reportService;

    private User buyer;
    private Listing listing;
    private UUID listingId;
    private UUID buyerId;

    @BeforeEach
    void setUp() {
        buyerId = UUID.randomUUID();
        listingId = UUID.randomUUID();

        buyer = new User();
        buyer.setId(buyerId);
        buyer.setEmail("buyer@test.com");
        buyer.setRole(Role.BUYER);

        listing = new Listing();
        listing.setId(listingId);
        listing.setActive(true);
        listing.setPrice(BigDecimal.valueOf(999));
        listing.setLicenseType(LicenseType.ONE_TIME);
    }

    @Test
    @DisplayName("Create report — sets status OPEN")
    void testCreateReportSetsStatusOpen() {
        CreateReportRequest request = new CreateReportRequest(ReportReason.ABUSE, "Suspicious listing");

        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(reportRepository.existsByListingIdAndReportedByIdAndStatus(listingId, buyerId, ReportStatus.OPEN))
            .thenReturn(false);
        when(reportRepository.save(any(ListingReport.class))).thenAnswer(invocation -> {
            ListingReport saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        ListingReportResponse response = reportService.createReport(listingId, buyerId, request);

        assertNotNull(response);
        assertEquals(ReportStatus.OPEN, response.getStatus());
        assertEquals(ReportReason.ABUSE, response.getReason());
        assertEquals("Suspicious listing", response.getComment());
        verify(reportRepository).save(any(ListingReport.class));
    }

    @Test
    @DisplayName("Duplicate open report from same user is rejected")
    void testDuplicateOpenReportRejected() {
        CreateReportRequest request = new CreateReportRequest(ReportReason.MISLEADING_CLAIMS, null);

        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(reportRepository.existsByListingIdAndReportedByIdAndStatus(listingId, buyerId, ReportStatus.OPEN))
            .thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            reportService.createReport(listingId, buyerId, request));

        assertTrue(ex.getMessage().contains("already have an open report"));
        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("Resolve FORCE_DELIST — listing deactivated")
    void testResolveForceDelistDeactivatesListing() {
        UUID reportId = UUID.randomUUID();
        ListingReport report = ListingReport.builder()
            .listing(listing)
            .reportedBy(buyer)
            .reason(ReportReason.GUARANTEED_RETURN_LANGUAGE)
            .status(ReportStatus.OPEN)
            .build();
        report.setId(reportId);

        ResolveReportRequest request = new ResolveReportRequest(ResolveAction.FORCE_DELIST, "Confirmed violation");

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(listingRepository.save(any(Listing.class))).thenAnswer(i -> i.getArgument(0));
        when(reportRepository.save(any(ListingReport.class))).thenAnswer(i -> i.getArgument(0));

        ListingReportResponse response = reportService.resolveReport(reportId, request);

        assertFalse(listing.isActive(), "Listing should be deactivated after FORCE_DELIST");
        assertEquals(ReportStatus.REVIEWED, response.getStatus());
        assertEquals("Confirmed violation", response.getAdminNotes());
        verify(listingRepository).save(listing);
    }

    @Test
    @DisplayName("Resolve DISMISS — listing unchanged")
    void testResolveDismissLeavesListingUnchanged() {
        UUID reportId = UUID.randomUUID();
        ListingReport report = ListingReport.builder()
            .listing(listing)
            .reportedBy(buyer)
            .reason(ReportReason.OTHER)
            .status(ReportStatus.OPEN)
            .build();
        report.setId(reportId);

        ResolveReportRequest request = new ResolveReportRequest(ResolveAction.DISMISS, "Not a real issue");

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(reportRepository.save(any(ListingReport.class))).thenAnswer(i -> i.getArgument(0));

        ListingReportResponse response = reportService.resolveReport(reportId, request);

        assertTrue(listing.isActive(), "Listing should remain active after DISMISS");
        assertEquals(ReportStatus.DISMISSED, response.getStatus());
        verify(listingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Resolve already-resolved report — throws")
    void testResolveAlreadyResolvedThrows() {
        UUID reportId = UUID.randomUUID();
        ListingReport report = ListingReport.builder()
            .listing(listing)
            .reportedBy(buyer)
            .reason(ReportReason.ABUSE)
            .status(ReportStatus.REVIEWED)
            .build();
        report.setId(reportId);

        ResolveReportRequest request = new ResolveReportRequest(ResolveAction.DISMISS, null);

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        assertThrows(IllegalStateException.class, () ->
            reportService.resolveReport(reportId, request));
    }
}
