package com.algoadda.core.order;

import com.algoadda.core.bot.BotVersion;
import com.algoadda.core.bot.service.S3StorageService;
import com.algoadda.core.order.dto.BuyerLicenseResponse;
import com.algoadda.core.order.dto.DownloadLicenseResponse;
import com.algoadda.core.user.SellerProfile;
import com.algoadda.core.user.SellerProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LicenseService {

    private static final Logger log = LoggerFactory.getLogger(LicenseService.class);

    private final LicenseRepository licenseRepository;
    private final OrderItemRepository orderItemRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final S3StorageService s3StorageService;

    public LicenseService(
        LicenseRepository licenseRepository,
        OrderItemRepository orderItemRepository,
        SellerProfileRepository sellerProfileRepository,
        S3StorageService s3StorageService
    ) {
        this.licenseRepository = licenseRepository;
        this.orderItemRepository = orderItemRepository;
        this.sellerProfileRepository = sellerProfileRepository;
        this.s3StorageService = s3StorageService;
    }

    @Transactional(readOnly = true)
    public List<BuyerLicenseResponse> getBuyerLicenses(UUID buyerId) {
        log.info("Fetching licenses for buyer ID: {}", buyerId);
        List<License> licenses = licenseRepository.findByBuyerId(buyerId);

        return licenses.stream().map(license -> {
            BotVersion botVersion = license.getBotVersion();
            UUID sellerId = botVersion.getBot().getSeller().getId();
            String sellerName = sellerProfileRepository.findByUserId(sellerId)
                .map((SellerProfile profile) -> profile.getDisplayName())
                .orElse(botVersion.getBot().getSeller().getEmail());

            boolean isOfficial = false;
            if (license.getOrder() != null) {
                List<OrderItem> items = orderItemRepository.findByOrderId(license.getOrder().getId());
                boolean officialInItems = items.stream().anyMatch(item -> item.getListing() != null && item.getListing().isOfficial());
                if (officialInItems) {
                    isOfficial = true;
                } else if (license.getOrder().getListing() != null) {
                    isOfficial = license.getOrder().getListing().isOfficial();
                }
            }

            return new BuyerLicenseResponse(
                license.getId(),
                license.getOrder() != null ? license.getOrder().getId() : null,
                botVersion.getBot().getId(),
                botVersion.getBot().getName(),
                botVersion.getVersionNumber(),
                sellerName,
                isOfficial,
                license.getIssuedAt(),
                license.getExpiresAt(),
                license.isPerpetual(),
                license.isActive(),
                license.isRevoked()
            );
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DownloadLicenseResponse generateDownloadUrl(UUID buyerId, UUID licenseId) {
        log.info("Generating strategy download URL for buyer ID {} and license ID {}", buyerId, licenseId);

        Objects.requireNonNull(licenseId, "licenseId must not be null");
        License license = licenseRepository.findById(licenseId)
            .orElseThrow(() -> new IllegalArgumentException("License not found"));

        if (license.getBuyer() == null || license.getBuyer().getId() == null || !license.getBuyer().getId().equals(buyerId)) {
            throw new AccessDeniedException("Buyer does not own this license");
        }

        if (!license.isActive()) {
            throw new IllegalStateException("License is expired or revoked. Access to strategy download denied.");
        }

        String fileStorageKey = license.getBotVersion().getFileStorageKey();
        if (fileStorageKey == null || fileStorageKey.isBlank()) {
            fileStorageKey = "bots/" + license.getBotVersion().getBot().getId() + "/v" + license.getBotVersion().getVersionNumber() + "/strategy.py";
        }

        Duration expiryDuration = Duration.ofMinutes(15);
        String presignedUrl = s3StorageService.generatePresignedUrl(fileStorageKey, expiryDuration);
        Instant expiresAt = Instant.now().plus(expiryDuration);

        return new DownloadLicenseResponse(presignedUrl, expiresAt);
    }
}
