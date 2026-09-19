package com.algoadda.core.listing;

import com.algoadda.core.bot.*;
import com.algoadda.core.user.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ListingViewTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BotRepository botRepository;

    @Autowired
    private BotVersionRepository botVersionRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private ListingViewRepository listingViewRepository;

    @Autowired
    private BacktestResultRepository backtestResultRepository;

    @Autowired
    private SellerProfileRepository sellerProfileRepository;

    private Listing listing;

    @BeforeEach
    void setUp() {
        listingViewRepository.deleteAll();
        listingRepository.deleteAll();
        backtestResultRepository.deleteAll();
        botVersionRepository.deleteAll();
        botRepository.deleteAll();
        sellerProfileRepository.deleteAll();
        userRepository.deleteAll();

        User seller = userRepository.save(User.builder()
            .email("seller_views@algoadda.com")
            .passwordHash("password")
            .role(Role.SELLER)
            .build());

        Bot bot = botRepository.save(Bot.builder()
            .seller(seller)
            .name("View Test Bot")
            .description("Bot for view testing")
            .strategyType("MOMENTUM")
            .status(BotStatus.PUBLISHED)
            .build());

        BotVersion version = botVersionRepository.save(BotVersion.builder()
            .bot(bot)
            .versionNumber("1.0.0")
            .disclosedLogic("rsi > 70")
            .backtestStatus(BacktestStatus.COMPLETED)
            .build());

        listing = listingRepository.save(Listing.builder()
            .botVersion(version)
            .price(BigDecimal.valueOf(99.00))
            .licenseType(LicenseType.ONE_TIME)
            .active(true)
            .official(true)
            .build());
    }

    @Test
    @DisplayName("View Tracking: Increment view count on GET /api/listings/{listingId}")
    void testIncrementViewCountPublicListing() throws Exception {
        assertThat(listingViewRepository.countByListingId(listing.getId())).isEqualTo(0);

        mockMvc.perform(get("/api/listings/" + listing.getId()))
            .andExpect(status().isOk());

        assertThat(listingViewRepository.countByListingId(listing.getId())).isEqualTo(1);

        mockMvc.perform(get("/api/listings/" + listing.getId()))
            .andExpect(status().isOk());

        assertThat(listingViewRepository.countByListingId(listing.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("View Tracking: Increment view count on GET /api/store/listings/{listingId}")
    void testIncrementViewCountOfficialStoreListing() throws Exception {
        assertThat(listingViewRepository.countByListingId(listing.getId())).isEqualTo(0);

        mockMvc.perform(get("/api/store/listings/" + listing.getId()))
            .andExpect(status().isOk());

        assertThat(listingViewRepository.countByListingId(listing.getId())).isEqualTo(1);
    }
}
