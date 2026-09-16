package com.algoadda.core.order;

import com.algoadda.core.bot.Bot;
import com.algoadda.core.bot.BotVersion;
import com.algoadda.core.bot.service.S3StorageService;
import com.algoadda.core.order.dto.DownloadLicenseResponse;
import com.algoadda.core.user.SellerProfileRepository;
import com.algoadda.core.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LicenseServiceTest {

    @Mock
    private LicenseRepository licenseRepository;

    @Mock
    private SellerProfileRepository sellerProfileRepository;

    @Mock
    private S3StorageService s3StorageService;

    @InjectMocks
    private LicenseService licenseService;

    private User buyer;
    private User otherBuyer;
    private License activeLicense;
    private License revokedLicense;
    private UUID buyerId;
    private UUID licenseId;
    private UUID revokedLicenseId;

    @BeforeEach
    void setUp() {
        buyerId = UUID.randomUUID();
        licenseId = UUID.randomUUID();
        revokedLicenseId = UUID.randomUUID();

        buyer = new User();
        buyer.setId(buyerId);
        buyer.setEmail("buyer@example.com");

        otherBuyer = new User();
        otherBuyer.setId(UUID.randomUUID());
        otherBuyer.setEmail("other@example.com");

        Bot bot = new Bot();
        bot.setId(UUID.randomUUID());

        BotVersion version = new BotVersion();
        version.setId(UUID.randomUUID());
        version.setBot(bot);
        version.setVersionNumber("1.0.0");
        version.setFileStorageKey("bots/bot-123/v1.0.0/strategy.py");

        activeLicense = new License();
        activeLicense.setId(licenseId);
        activeLicense.setBuyer(buyer);
        activeLicense.setBotVersion(version);
        activeLicense.setIssuedAt(Instant.now());
        activeLicense.setExpiresAt(null);
        activeLicense.setRevoked(false);

        revokedLicense = new License();
        revokedLicense.setId(revokedLicenseId);
        revokedLicense.setBuyer(buyer);
        revokedLicense.setBotVersion(version);
        revokedLicense.setIssuedAt(Instant.now());
        revokedLicense.setExpiresAt(null);
        revokedLicense.setRevoked(true);
    }

    @Test
    void testDownloadUrl_ActiveLicense_GeneratesPresignedUrl() {
        when(licenseRepository.findById(licenseId)).thenReturn(Optional.of(activeLicense));
        when(s3StorageService.generatePresignedUrl(eq("bots/bot-123/v1.0.0/strategy.py"), any(Duration.class)))
            .thenReturn("https://algoadda-bucket.s3.amazonaws.com/bots/bot-123/v1.0.0/strategy.py?X-Amz-Signature=xyz");

        DownloadLicenseResponse response = licenseService.generateDownloadUrl(buyerId, licenseId);

        assertNotNull(response);
        assertTrue(response.getDownloadUrl().contains("X-Amz-Signature=xyz"));
        assertNotNull(response.getExpiresAt());
    }

    @Test
    void testDownloadUrl_RevokedOrExpiredLicense_ThrowsException() {
        when(licenseRepository.findById(revokedLicenseId)).thenReturn(Optional.of(revokedLicense));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            licenseService.generateDownloadUrl(buyerId, revokedLicenseId);
        });

        assertTrue(exception.getMessage().contains("expired or revoked"));
    }

    @Test
    void testDownloadUrl_UnauthorizedBuyer_ThrowsException() {
        when(licenseRepository.findById(licenseId)).thenReturn(Optional.of(activeLicense));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            licenseService.generateDownloadUrl(otherBuyer.getId(), licenseId);
        });

        assertTrue(exception.getMessage().contains("does not own this license"));
    }
}
