package com.algoadda.core.config;

import com.algoadda.core.user.Role;
import com.algoadda.core.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private static final String SECRET = "very-long-and-secure-secret-key-that-satisfies-hmac-sha-256-security-requirements";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, 15);
    }

    @Test
    @DisplayName("Generate and validate valid JWT token")
    void testGenerateAndValidateToken() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
            .id(userId)
            .email("test@algoadda.com")
            .role(Role.SELLER)
            .build();

        String token = jwtTokenProvider.generateAccessToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(userId);
        assertThat(jwtTokenProvider.getEmailFromToken(token)).isEqualTo("test@algoadda.com");
        assertThat(jwtTokenProvider.getRoleFromToken(token)).isEqualTo("ROLE_SELLER");
    }

    @Test
    @DisplayName("Reject tampered / invalid JWT token")
    void testInvalidToken() {
        assertThat(jwtTokenProvider.validateToken("invalid.jwt.token")).isFalse();
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
    }
}
