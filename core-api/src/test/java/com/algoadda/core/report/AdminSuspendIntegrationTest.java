package com.algoadda.core.report;

import com.algoadda.core.bot.*;
import com.algoadda.core.compliance.ComplianceCheckRepository;
import com.algoadda.core.config.JwtTokenProvider;
import com.algoadda.core.listing.Listing;
import com.algoadda.core.listing.LicenseType;
import com.algoadda.core.listing.ListingRepository;
import com.algoadda.core.user.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("null")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminSuspendIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private SellerProfileRepository sellerProfileRepository;
    @Autowired private BotRepository botRepository;
    @Autowired private BotVersionRepository botVersionRepository;
    @Autowired private BacktestResultRepository backtestResultRepository;
    @Autowired private ComplianceCheckRepository complianceCheckRepository;
    @Autowired private ListingRepository listingRepository;
    @Autowired private com.algoadda.core.listing.ListingViewRepository listingViewRepository;
    @Autowired private ListingReportRepository reportRepository;
    @Autowired private com.algoadda.core.bot.BacktestJobRepository backtestJobRepository;
    @Autowired private com.algoadda.core.user.RefreshTokenRepository refreshTokenRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private ObjectMapper objectMapper;

    private String adminToken;
    private String buyerToken;
    private String sellerToken;
    private User seller;
    private User buyer;
    private User admin;
    private Listing listing1;
    private Listing listing2;

    @BeforeEach
    void setUp() {
        // Clean in dependency order
        reportRepository.deleteAll();
        listingViewRepository.deleteAll();
        listingRepository.deleteAll();
        complianceCheckRepository.deleteAll();
        backtestJobRepository.deleteAll();
        backtestResultRepository.deleteAll();
        botVersionRepository.deleteAll();
        botRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        sellerProfileRepository.deleteAll();
        userRepository.deleteAll();

        // Create users
        buyer = userRepository.save(User.builder()
            .email("buyer@test.com")
            .passwordHash(passwordEncoder.encode("pass"))
            .role(Role.BUYER)
            .build());

        seller = userRepository.save(User.builder()
            .email("seller@test.com")
            .passwordHash(passwordEncoder.encode("pass"))
            .role(Role.SELLER)
            .build());

        admin = userRepository.save(User.builder()
            .email("admin@test.com")
            .passwordHash(passwordEncoder.encode("pass"))
            .role(Role.ADMIN)
            .build());

        buyerToken = "Bearer " + jwtTokenProvider.generateAccessToken(buyer);
        sellerToken = "Bearer " + jwtTokenProvider.generateAccessToken(seller);
        adminToken = "Bearer " + jwtTokenProvider.generateAccessToken(admin);

        // Create seller profile
        sellerProfileRepository.save(SellerProfile.builder()
            .user(seller)
            .displayName("Test Seller")
            .build());

        // Create two published bots with listings for the seller
        Bot bot1 = botRepository.save(Bot.builder()
            .seller(seller)
            .name("Alpha Bot")
            .description("Test bot 1")
            .strategyType("MOMENTUM")
            .status(BotStatus.PUBLISHED)
            .build());

        BotVersion version1 = botVersionRepository.save(BotVersion.builder()
            .bot(bot1)
            .versionNumber("1.0.0")
            .disclosedLogic("Buy when RSI < 30")
            .backtestStatus(BacktestStatus.COMPLETED)
            .build());

        listing1 = listingRepository.save(Listing.builder()
            .botVersion(version1)
            .price(BigDecimal.valueOf(499))
            .licenseType(LicenseType.ONE_TIME)
            .active(true)
            .build());

        Bot bot2 = botRepository.save(Bot.builder()
            .seller(seller)
            .name("Beta Bot")
            .description("Test bot 2")
            .strategyType("MEAN_REVERSION")
            .status(BotStatus.PUBLISHED)
            .build());

        BotVersion version2 = botVersionRepository.save(BotVersion.builder()
            .bot(bot2)
            .versionNumber("1.0.0")
            .disclosedLogic("Sell when price > 2 std dev")
            .backtestStatus(BacktestStatus.COMPLETED)
            .build());

        listing2 = listingRepository.save(Listing.builder()
            .botVersion(version2)
            .price(BigDecimal.valueOf(799))
            .licenseType(LicenseType.ONE_TIME)
            .active(true)
            .build());
    }

    @Test
    @DisplayName("Force-delist removes listing from public GET /api/listings")
    void testForceDelistRemovesFromPublicEndpoint() throws Exception {
        // Verify listing is visible before
        mockMvc.perform(get("/api/listings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(2));

        // Admin force-delists listing1
        mockMvc.perform(post("/api/admin/listings/" + listing1.getId() + "/force-delist")
                .header("Authorization", adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        // Verify only 1 listing remains
        mockMvc.perform(get("/api/listings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1));

        // Verify the delisted listing is actually inactive in DB
        Listing reloaded = listingRepository.findById(listing1.getId()).orElseThrow();
        assertFalse(reloaded.isActive());
    }

    @Test
    @DisplayName("Suspending a seller hides ALL their listings from public endpoints")
    void testSuspendSellerHidesAllListings() throws Exception {
        // Both listings visible before suspension
        mockMvc.perform(get("/api/listings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(2));

        // Admin suspends the seller
        mockMvc.perform(post("/api/admin/sellers/" + seller.getId() + "/suspend")
                .header("Authorization", adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        // Verify ALL listings from that seller are now hidden
        mockMvc.perform(get("/api/listings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(0));

        // Verify both listings are still active in DB (not individually deactivated)
        Listing reloaded1 = listingRepository.findById(listing1.getId()).orElseThrow();
        Listing reloaded2 = listingRepository.findById(listing2.getId()).orElseThrow();
        assertTrue(reloaded1.isActive(), "Listing should still be active, suspension is on the user");
        assertTrue(reloaded2.isActive(), "Listing should still be active, suspension is on the user");

        // Verify user is actually suspended
        User reloadedSeller = userRepository.findById(seller.getId()).orElseThrow();
        assertTrue(reloadedSeller.isSuspended());
    }

    @Test
    @DisplayName("Unsuspending a seller restores all their listings")
    void testUnsuspendSellerRestoresListings() throws Exception {
        // Suspend first
        mockMvc.perform(post("/api/admin/sellers/" + seller.getId() + "/suspend")
                .header("Authorization", adminToken))
            .andExpect(status().isOk());

        // Verify hidden
        mockMvc.perform(get("/api/listings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(0));

        // Unsuspend
        mockMvc.perform(post("/api/admin/sellers/" + seller.getId() + "/unsuspend")
                .header("Authorization", adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        // Verify listings are back
        mockMvc.perform(get("/api/listings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(2));

        // Verify user unsuspended in DB
        User reloadedSeller = userRepository.findById(seller.getId()).orElseThrow();
        assertFalse(reloadedSeller.isSuspended());
    }

    @Test
    @DisplayName("RBAC: Only ADMIN can force-delist")
    void testRbacForceDelistAdminOnly() throws Exception {
        // Buyer forbidden
        mockMvc.perform(post("/api/admin/listings/" + listing1.getId() + "/force-delist")
                .header("Authorization", buyerToken))
            .andExpect(status().isForbidden());

        // Seller forbidden
        mockMvc.perform(post("/api/admin/listings/" + listing1.getId() + "/force-delist")
                .header("Authorization", sellerToken))
            .andExpect(status().isForbidden());

        // Unauthenticated
        mockMvc.perform(post("/api/admin/listings/" + listing1.getId() + "/force-delist"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("RBAC: Only ADMIN can suspend/unsuspend")
    void testRbacSuspendAdminOnly() throws Exception {
        mockMvc.perform(post("/api/admin/sellers/" + seller.getId() + "/suspend")
                .header("Authorization", buyerToken))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/admin/sellers/" + seller.getId() + "/suspend")
                .header("Authorization", sellerToken))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/admin/sellers/" + seller.getId() + "/suspend"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("RBAC: Only ADMIN can view report queue")
    void testRbacReportQueueAdminOnly() throws Exception {
        mockMvc.perform(get("/api/admin/reports/queue")
                .header("Authorization", buyerToken))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/reports/queue")
                .header("Authorization", adminToken))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Report flow: create report, duplicate rejected, resolve with FORCE_DELIST")
    void testFullReportFlow() throws Exception {
        // Buyer reports listing
        String reportBody = objectMapper.writeValueAsString(
            new com.algoadda.core.report.dto.CreateReportRequest(ReportReason.MISLEADING_CLAIMS, "This seems fake"));

        mockMvc.perform(post("/api/listings/" + listing1.getId() + "/report")
                .header("Authorization", buyerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reportBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("OPEN"))
            .andExpect(jsonPath("$.reason").value("MISLEADING_CLAIMS"));

        // Same buyer tries again — rejected
        mockMvc.perform(post("/api/listings/" + listing1.getId() + "/report")
                .header("Authorization", buyerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reportBody))
            .andExpect(status().isBadRequest());

        // Admin sees report in queue
        mockMvc.perform(get("/api/admin/reports/queue")
                .header("Authorization", adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].reason").value("MISLEADING_CLAIMS"));

        // Admin resolves with FORCE_DELIST
        UUID reportId = reportRepository.findByStatusOrderByCreatedAtDesc(ReportStatus.OPEN).get(0).getId();
        String resolveBody = objectMapper.writeValueAsString(
            new com.algoadda.core.report.dto.ResolveReportRequest(ResolveAction.FORCE_DELIST, "Confirmed"));

        mockMvc.perform(post("/api/admin/reports/" + reportId + "/resolve")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(resolveBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("REVIEWED"));

        // Listing is now delisted
        Listing reloaded = listingRepository.findById(listing1.getId()).orElseThrow();
        assertFalse(reloaded.isActive());

        // Only 1 listing remains in public endpoint
        mockMvc.perform(get("/api/listings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("Unauthenticated user cannot report a listing")
    void testUnauthenticatedCannotReport() throws Exception {
        String reportBody = objectMapper.writeValueAsString(
            new com.algoadda.core.report.dto.CreateReportRequest(ReportReason.ABUSE, "test"));

        mockMvc.perform(post("/api/listings/" + listing1.getId() + "/report")
                .contentType(MediaType.APPLICATION_JSON)
                .content(reportBody))
            .andExpect(status().isForbidden());
    }
}
