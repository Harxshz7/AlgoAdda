package com.algoadda.core.user;

import com.algoadda.core.config.JwtTokenProvider;
import com.algoadda.core.user.dto.AuthResponse;
import com.algoadda.core.user.dto.LoginRequest;
import com.algoadda.core.user.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Suppress Eclipse JDT null-safety false positives caused by Mockito & Spring Data generic interfaces
@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenService refreshTokenService;

    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider jwtTokenProvider;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        jwtTokenProvider = new JwtTokenProvider(
            "test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha-256-testing-purposes",
            15
        );
        authService = new AuthService(userRepository, passwordEncoder, jwtTokenProvider, refreshTokenService);
    }

    @Test
    @DisplayName("Register: Creates user and returns tokens")
    void testRegisterSuccess() {
        RegisterRequest request = RegisterRequest.builder()
            .email("trader@algoadda.com")
            .password("secret123")
            .role(Role.SELLER)
            .build();

        UUID userId = UUID.randomUUID();
        User savedUser = User.builder()
            .id(userId)
            .email("trader@algoadda.com")
            .passwordHash(passwordEncoder.encode("secret123"))
            .role(Role.SELLER)
            .createdAt(Instant.now())
            .build();

        RefreshToken refreshToken = RefreshToken.builder()
            .token("valid-refresh-token")
            .user(savedUser)
            .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
            .revoked(false)
            .build();

        when(userRepository.existsByEmail("trader@algoadda.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(refreshTokenService.createRefreshToken(savedUser)).thenReturn(refreshToken);

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getRefreshToken()).isEqualTo("valid-refresh-token");
        assertThat(response.getUser().getEmail()).isEqualTo("trader@algoadda.com");
        assertThat(response.getUser().getRole()).isEqualTo(Role.SELLER);
    }

    @Test
    @DisplayName("Register: Throws exception when email already registered")
    void testRegisterDuplicateEmail() {
        RegisterRequest request = RegisterRequest.builder()
            .email("duplicate@algoadda.com")
            .password("password")
            .build();

        when(userRepository.existsByEmail("duplicate@algoadda.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already registered");
    }

    @Test
    @DisplayName("Login: Returns tokens on valid credentials")
    void testLoginSuccess() {
        LoginRequest request = LoginRequest.builder()
            .email("buyer@algoadda.com")
            .password("correct-password")
            .build();

        User user = User.builder()
            .id(UUID.randomUUID())
            .email("buyer@algoadda.com")
            .passwordHash(passwordEncoder.encode("correct-password"))
            .role(Role.BUYER)
            .createdAt(Instant.now())
            .build();

        RefreshToken refreshToken = RefreshToken.builder()
            .token("refresh-token-xyz")
            .user(user)
            .build();

        when(userRepository.findByEmail("buyer@algoadda.com")).thenReturn(Optional.of(user));
        when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshToken);

        AuthResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token-xyz");
        assertThat(response.getUser().getEmail()).isEqualTo("buyer@algoadda.com");
    }

    @Test
    @DisplayName("Login: Throws BadCredentialsException on invalid password")
    void testLoginInvalidPassword() {
        LoginRequest request = LoginRequest.builder()
            .email("buyer@algoadda.com")
            .password("wrong-password")
            .build();

        User user = User.builder()
            .id(UUID.randomUUID())
            .email("buyer@algoadda.com")
            .passwordHash(passwordEncoder.encode("correct-password"))
            .build();

        when(userRepository.findByEmail("buyer@algoadda.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("Refresh: Rotates refresh token and returns new access token")
    void testRefreshTokenRotation() {
        User user = User.builder()
            .id(UUID.randomUUID())
            .email("buyer@algoadda.com")
            .role(Role.BUYER)
            .build();

        RefreshToken rotatedRefreshToken = RefreshToken.builder()
            .token("new-rotated-refresh-token")
            .user(user)
            .build();

        when(refreshTokenService.verifyAndRotateRefreshToken("old-token")).thenReturn(rotatedRefreshToken);

        AuthResponse response = authService.refresh("old-token");

        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getRefreshToken()).isEqualTo("new-rotated-refresh-token");
    }
}
