package com.algoadda.core;

import com.algoadda.core.config.JwtTokenProvider;
import com.algoadda.core.user.RefreshTokenRepository;
import com.algoadda.core.user.Role;
import com.algoadda.core.user.SellerProfileRepository;
import com.algoadda.core.user.User;
import com.algoadda.core.user.UserRepository;
import com.algoadda.core.user.dto.LoginRequest;
import com.algoadda.core.user.dto.RegisterRequest;
import com.algoadda.core.user.dto.SellerOnboardRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Suppress Eclipse JDT null-safety false positives caused by Spring Data JPA & MockMvc generic interfaces
@SuppressWarnings("null")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RbacSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private SellerProfileRepository sellerProfileRepository;

    @Autowired
    private com.algoadda.core.bot.BotRepository botRepository;

    @Autowired
    private com.algoadda.core.bot.BotVersionRepository botVersionRepository;

    @Autowired
    private com.algoadda.core.compliance.ComplianceCheckRepository complianceCheckRepository;

    @Autowired
    private com.algoadda.core.bot.BacktestResultRepository backtestResultRepository;

    @Autowired
    private com.algoadda.core.listing.ListingRepository listingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private String buyerToken;
    private String sellerToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        listingRepository.deleteAll();
        complianceCheckRepository.deleteAll();
        backtestResultRepository.deleteAll();
        botVersionRepository.deleteAll();
        botRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        sellerProfileRepository.deleteAll();
        userRepository.deleteAll();

        User buyer = userRepository.save(User.builder()
            .email("buyer@algoadda.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .role(Role.BUYER)
            .build());

        User seller = userRepository.save(User.builder()
            .email("seller@algoadda.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .role(Role.SELLER)
            .build());

        User admin = userRepository.save(User.builder()
            .email("admin@algoadda.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .role(Role.ADMIN)
            .build());

        buyerToken = "Bearer " + jwtTokenProvider.generateAccessToken(buyer);
        sellerToken = "Bearer " + jwtTokenProvider.generateAccessToken(seller);
        adminToken = "Bearer " + jwtTokenProvider.generateAccessToken(admin);
    }

    @Test
    @DisplayName("Auth Flow: Register, Login, and Access Health Check")
    void testAuthEndpoints() throws Exception {
        // 1. Health check is public
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ok"));

        // 2. Register new user
        RegisterRequest registerReq = RegisterRequest.builder()
            .email("newuser@algoadda.com")
            .password("securePass99")
            .role(Role.BUYER)
            .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").isNotEmpty())
            .andExpect(jsonPath("$.user.email").value("newuser@algoadda.com"))
            .andExpect(cookie().exists("refreshToken"));

        // 3. Login
        LoginRequest loginReq = LoginRequest.builder()
            .email("newuser@algoadda.com")
            .password("securePass99")
            .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(cookie().exists("refreshToken"));
    }

    @Test
    @DisplayName("RBAC: Buyer Ping Guard")
    void testBuyerPingGuard() throws Exception {
        // Buyer allowed
        mockMvc.perform(get("/api/buyer/ping").header("Authorization", buyerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value("BUYER"));

        // Seller forbidden
        mockMvc.perform(get("/api/buyer/ping").header("Authorization", sellerToken))
            .andExpect(status().isForbidden());

        // Anonymous unauthorized
        mockMvc.perform(get("/api/buyer/ping"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("RBAC: Seller Ping Guard")
    void testSellerPingGuard() throws Exception {
        // Seller allowed
        mockMvc.perform(get("/api/seller/ping").header("Authorization", sellerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value("SELLER"));

        // Buyer forbidden
        mockMvc.perform(get("/api/seller/ping").header("Authorization", buyerToken))
            .andExpect(status().isForbidden());

        // Anonymous unauthorized
        mockMvc.perform(get("/api/seller/ping"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("RBAC: Admin Ping Guard")
    void testAdminPingGuard() throws Exception {
        // Admin allowed
        mockMvc.perform(get("/api/admin/ping").header("Authorization", adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value("ADMIN"));

        // Seller forbidden
        mockMvc.perform(get("/api/admin/ping").header("Authorization", sellerToken))
            .andExpect(status().isForbidden());

        // Buyer forbidden
        mockMvc.perform(get("/api/admin/ping").header("Authorization", buyerToken))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Seller Onboarding: Successfully onboard seller profile and reject buyer")
    void testSellerOnboarding() throws Exception {
        SellerOnboardRequest request = SellerOnboardRequest.builder()
            .displayName("NiftyQuant Labs")
            .bio("Systematic trading systems with verified historical performance")
            .build();

        // Seller allowed
        mockMvc.perform(post("/api/sellers/onboard")
                .header("Authorization", sellerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.displayName").value("NiftyQuant Labs"))
            .andExpect(jsonPath("$.kycStatus").value("NOT_STARTED"));

        // Buyer forbidden
        mockMvc.perform(post("/api/sellers/onboard")
                .header("Authorization", buyerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }
}
