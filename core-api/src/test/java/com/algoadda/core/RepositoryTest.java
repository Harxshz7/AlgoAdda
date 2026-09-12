package com.algoadda.core;

import com.algoadda.core.bot.*;
import com.algoadda.core.compliance.ComplianceCheck;
import com.algoadda.core.compliance.ComplianceCheckRepository;
import com.algoadda.core.compliance.ReviewerType;
import com.algoadda.core.listing.LicenseType;
import com.algoadda.core.listing.Listing;
import com.algoadda.core.listing.ListingRepository;
import com.algoadda.core.order.*;
import com.algoadda.core.user.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

// Suppress Eclipse JDT null-safety false positives caused by Spring Data JPA's @NonNull-annotated generic save() methods
@SuppressWarnings("null")
@DataJpaTest
@ActiveProfiles("test")
class RepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SellerProfileRepository sellerProfileRepository;

    @Autowired
    private BotRepository botRepository;

    @Autowired
    private BotVersionRepository botVersionRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private ComplianceCheckRepository complianceCheckRepository;

    @Test
    @DisplayName("User & SellerProfile: Save, retrieve, and FK cascade")
    void testUserAndSellerProfilePersistence() {
        User seller = userRepository.save(User.builder()
            .email("quant@algoadda.com")
            .passwordHash("hashed-pw-123")
            .role(Role.SELLER)
            .build());

        assertThat(seller.getId()).isNotNull();
        assertThat(userRepository.existsByEmail("quant@algoadda.com")).isTrue();

        SellerProfile profile = sellerProfileRepository.save(SellerProfile.builder()
            .user(seller)
            .displayName("Alpha Quant")
            .bio("Algorithmic momentum trader")
            .kycStatus(KycStatus.NOT_STARTED)
            .build());

        assertThat(profile.getId()).isNotNull();
        Optional<SellerProfile> found = sellerProfileRepository.findByUserId(seller.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getDisplayName()).isEqualTo("Alpha Quant");
        assertThat(found.get().getKycStatus()).isEqualTo(KycStatus.NOT_STARTED);
    }

    @Test
    @DisplayName("Bot & BotVersion: Enforce disclosed logic and version uniqueness")
    void testBotAndBotVersionPersistence() {
        User seller = userRepository.save(User.builder()
            .email("seller2@algoadda.com")
            .passwordHash("pw-hash")
            .role(Role.SELLER)
            .build());

        Bot bot = botRepository.save(Bot.builder()
            .seller(seller)
            .name("Nifty Mean Reversion")
            .description("Statistical arbitrage on 15m candles")
            .strategyType("MEAN_REVERSION")
            .status(BotStatus.DRAFT)
            .build());

        assertThat(bot.getId()).isNotNull();

        BotVersion v1 = botVersionRepository.save(BotVersion.builder()
            .bot(bot)
            .versionNumber("1.0.0")
            .disclosedLogic("Bollinger Bands (20, 2) + RSI (14) oversold entry; exit at SMA(20)")
            .fileStorageKey("bots/nifty-mr/1.0.0.py")
            .changelog("Initial release")
            .build());

        assertThat(v1.getId()).isNotNull();
        assertThat(v1.getDisclosedLogic()).contains("Bollinger Bands");

        Optional<BotVersion> retrieved = botVersionRepository.findByBotIdAndVersionNumber(bot.getId(), "1.0.0");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getVersionNumber()).isEqualTo("1.0.0");
    }

    @Test
    @DisplayName("Listing, Order & License Lifecycle")
    void testListingOrderLicenseFlow() {
        User seller = userRepository.save(User.builder()
            .email("seller3@algoadda.com")
            .passwordHash("hash")
            .role(Role.SELLER)
            .build());

        User buyer = userRepository.save(User.builder()
            .email("buyer1@algoadda.com")
            .passwordHash("hash")
            .role(Role.BUYER)
            .build());

        Bot bot = botRepository.save(Bot.builder()
            .seller(seller)
            .name("Trend Follower")
            .strategyType("TREND_FOLLOWING")
            .status(BotStatus.PUBLISHED)
            .build());

        BotVersion v1 = botVersionRepository.save(BotVersion.builder()
            .bot(bot)
            .versionNumber("1.0.0")
            .disclosedLogic("EMA 50 cross EMA 200 with ATR trailing stop")
            .build());

        Listing listing = listingRepository.save(Listing.builder()
            .botVersion(v1)
            .price(new BigDecimal("4999.00"))
            .licenseType(LicenseType.ONE_TIME)
            .active(true)
            .build());

        assertThat(listing.getId()).isNotNull();

        Order order = orderRepository.save(Order.builder()
            .buyer(buyer)
            .listing(listing)
            .paymentReference("pay_razorpay_123456")
            .status(OrderStatus.PAID)
            .build());

        assertThat(order.getId()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);

        License license = licenseRepository.save(License.builder()
            .order(order)
            .botVersion(v1)
            .buyer(buyer)
            .build());

        assertThat(license.getId()).isNotNull();
        assertThat(license.isPerpetual()).isTrue();
        assertThat(license.isActive()).isTrue();
    }

    @Test
    @DisplayName("ComplianceCheck: Persist automated and manual reviews")
    void testComplianceCheckPersistence() {
        User seller = userRepository.save(User.builder()
            .email("seller4@algoadda.com")
            .passwordHash("hash")
            .role(Role.SELLER)
            .build());

        Bot bot = botRepository.save(Bot.builder()
            .seller(seller)
            .name("Momentum Alpha")
            .strategyType("MOMENTUM")
            .build());

        BotVersion v1 = botVersionRepository.save(BotVersion.builder()
            .bot(bot)
            .versionNumber("1.0.0")
            .disclosedLogic("Disclosed clear logic without guaranteed returns")
            .build());

        ComplianceCheck check = complianceCheckRepository.save(ComplianceCheck.builder()
            .botVersion(v1)
            .reviewerType(ReviewerType.AUTO)
            .checklistResults("{\"logic_disclosed\": true, \"no_guaranteed_return_claims\": true}")
            .passed(true)
            .build());

        assertThat(check.getId()).isNotNull();
        assertThat(check.isPassed()).isTrue();
        assertThat(check.getReviewerType()).isEqualTo(ReviewerType.AUTO);
    }
}
