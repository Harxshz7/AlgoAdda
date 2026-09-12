package com.algoadda.core.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

// Suppress Eclipse JDT null-safety false positives caused by Spring Data JPA generic interfaces
@SuppressWarnings("null")
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenValidityDays;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
        RefreshTokenRepository refreshTokenRepository,
        @Value("${algoadda.jwt.refresh-token-validity-days:7}") long refreshTokenValidityDays
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenValidityDays = refreshTokenValidityDays;
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        refreshTokenRepository.revokeAllByUserId(user.getId());

        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);
        String tokenValue = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        RefreshToken refreshToken = RefreshToken.builder()
            .user(user)
            .token(tokenValue)
            .expiresAt(Instant.now().plus(refreshTokenValidityDays, ChronoUnit.DAYS))
            .revoked(false)
            .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken verifyAndRotateRefreshToken(String tokenValue) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenValue)
            .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (token.isRevoked() || token.isExpired()) {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            throw new IllegalArgumentException("Refresh token is expired or revoked");
        }

        token.setRevoked(true);
        refreshTokenRepository.save(token);

        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);
        String newTokenValue = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        RefreshToken newToken = RefreshToken.builder()
            .user(token.getUser())
            .token(newTokenValue)
            .expiresAt(Instant.now().plus(refreshTokenValidityDays, ChronoUnit.DAYS))
            .revoked(false)
            .build();

        return refreshTokenRepository.save(newToken);
    }

    @Transactional
    public void revokeRefreshToken(String tokenValue) {
        refreshTokenRepository.findByToken(tokenValue).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    public long getRefreshTokenValiditySeconds() {
        return refreshTokenValidityDays * 24 * 60 * 60;
    }
}
