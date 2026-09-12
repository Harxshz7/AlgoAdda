package com.algoadda.core.user;

import com.algoadda.core.config.JwtTokenProvider;
import com.algoadda.core.user.dto.AuthResponse;
import com.algoadda.core.user.dto.LoginRequest;
import com.algoadda.core.user.dto.RegisterRequest;
import com.algoadda.core.user.dto.UserDto;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Suppress Eclipse JDT null-safety false positives caused by Spring Data JPA generic interfaces
@SuppressWarnings("null")
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtTokenProvider jwtTokenProvider,
        RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        Role role = request.getRole() != null ? request.getRole() : Role.BUYER;

        User user = User.builder()
            .email(request.getEmail().trim().toLowerCase())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .role(role)
            .build();

        User savedUser = userRepository.save(user);

        String accessToken = jwtTokenProvider.generateAccessToken(savedUser);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);

        return buildAuthResponse(savedUser, accessToken, refreshToken.getToken());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
            .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenValue) {
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            throw new IllegalArgumentException("Refresh token is required");
        }

        RefreshToken newRefreshToken = refreshTokenService.verifyAndRotateRefreshToken(refreshTokenValue);
        User user = newRefreshToken.getUser();
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);

        return buildAuthResponse(user, newAccessToken, newRefreshToken.getToken());
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        if (refreshTokenValue != null && !refreshTokenValue.isBlank()) {
            refreshTokenService.revokeRefreshToken(refreshTokenValue);
        }
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtTokenProvider.getAccessTokenValiditySeconds())
            .user(UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build())
            .build();
    }
}
