package com.algoadda.core.user;

import com.algoadda.core.user.dto.SellerOnboardRequest;
import com.algoadda.core.user.dto.SellerProfileResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// Suppress Eclipse JDT null-safety false positives caused by Spring Data JPA generic interfaces
@SuppressWarnings("null")
@Service
public class SellerService {

    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;

    public SellerService(UserRepository userRepository, SellerProfileRepository sellerProfileRepository) {
        this.userRepository = userRepository;
        this.sellerProfileRepository = sellerProfileRepository;
    }

    @Transactional
    public SellerProfileResponse onboardSeller(UUID userId, SellerOnboardRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        if (user.getRole() == Role.BUYER) {
            user.setRole(Role.SELLER);
            userRepository.save(user);
        }

        SellerProfile profile = sellerProfileRepository.findByUserId(userId)
            .map(existing -> {
                existing.setDisplayName(request.getDisplayName());
                existing.setBio(request.getBio());
                return existing;
            })
            .orElseGet(() -> SellerProfile.builder()
                .user(user)
                .displayName(request.getDisplayName())
                .bio(request.getBio())
                .kycStatus(KycStatus.NOT_STARTED)
                .build());

        SellerProfile saved = sellerProfileRepository.save(profile);

        return SellerProfileResponse.builder()
            .id(saved.getId())
            .userId(user.getId())
            .displayName(saved.getDisplayName())
            .bio(saved.getBio())
            .kycStatus(saved.getKycStatus())
            .createdAt(saved.getCreatedAt())
            .updatedAt(saved.getUpdatedAt())
            .build();
    }

    @Transactional(readOnly = true)
    public SellerProfileResponse getSellerProfile(UUID userId) {
        SellerProfile profile = sellerProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("Seller profile not found for user: " + userId));

        return SellerProfileResponse.builder()
            .id(profile.getId())
            .userId(userId)
            .displayName(profile.getDisplayName())
            .bio(profile.getBio())
            .kycStatus(profile.getKycStatus())
            .createdAt(profile.getCreatedAt())
            .updatedAt(profile.getUpdatedAt())
            .build();
    }
}
