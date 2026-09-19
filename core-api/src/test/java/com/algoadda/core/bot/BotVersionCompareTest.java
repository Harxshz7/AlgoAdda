package com.algoadda.core.bot;

import com.algoadda.core.bot.dto.VersionComparisonResponse;
import com.algoadda.core.bot.dto.VersionDiffLineDto;
import com.algoadda.core.user.Role;
import com.algoadda.core.user.User;
import com.algoadda.core.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BotVersionCompareTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BotRepository botRepository;

    @Autowired
    private BotVersionRepository botVersionRepository;

    @Autowired
    private BacktestResultRepository backtestResultRepository;

    @Autowired
    private BotVersionCompareService compareService;

    private User seller;
    private User otherUser;
    private Bot draftBot;
    private Bot publishedBot;
    private BotVersion draftV1;
    private BotVersion draftV2;
    private BotVersion pubV1;
    private BotVersion pubV2;

    @BeforeEach
    void setUp() {
        backtestResultRepository.deleteAll();
        botVersionRepository.deleteAll();
        botRepository.deleteAll();
        userRepository.deleteAll();

        seller = userRepository.save(User.builder()
            .email("owner_seller@algoadda.com")
            .passwordHash("password")
            .role(Role.SELLER)
            .build());

        otherUser = userRepository.save(User.builder()
            .email("other_user@algoadda.com")
            .passwordHash("password")
            .role(Role.BUYER)
            .build());

        // Draft bot with 2 versions
        draftBot = botRepository.save(Bot.builder()
            .seller(seller)
            .name("Draft Strategy")
            .description("Draft bot")
            .strategyType("GRID")
            .status(BotStatus.DRAFT)
            .build());

        draftV1 = botVersionRepository.save(BotVersion.builder()
            .bot(draftBot)
            .versionNumber("1.0.0")
            .disclosedLogic("line 1\nline 2")
            .backtestStatus(BacktestStatus.COMPLETED)
            .build());

        draftV2 = botVersionRepository.save(BotVersion.builder()
            .bot(draftBot)
            .versionNumber("1.1.0")
            .disclosedLogic("line 1\nline 2 modified\nline 3")
            .backtestStatus(BacktestStatus.COMPLETED)
            .build());

        // Published bot with 2 versions
        publishedBot = botRepository.save(Bot.builder()
            .seller(seller)
            .name("Published Strategy")
            .description("Published bot")
            .strategyType("MOMENTUM")
            .status(BotStatus.PUBLISHED)
            .build());

        pubV1 = botVersionRepository.save(BotVersion.builder()
            .bot(publishedBot)
            .versionNumber("1.0.0")
            .disclosedLogic("rsi > 70")
            .backtestStatus(BacktestStatus.COMPLETED)
            .build());

        pubV2 = botVersionRepository.save(BotVersion.builder()
            .bot(publishedBot)
            .versionNumber("2.0.0")
            .disclosedLogic("rsi > 65")
            .backtestStatus(BacktestStatus.COMPLETED)
            .build());

        backtestResultRepository.save(BacktestResult.builder()
            .botVersion(pubV1)
            .metrics("{\"win_rate\": 60.0, \"max_drawdown\": 10.0, \"sharpe_ratio\": 1.5}")
            .build());

        backtestResultRepository.save(BacktestResult.builder()
            .botVersion(pubV2)
            .metrics("{\"win_rate\": 70.0, \"max_drawdown\": 8.0, \"sharpe_ratio\": 2.0}")
            .build());
    }

    @Test
    @DisplayName("Security Check: Reject non-owner access to UNPUBLISHED version comparison")
    void testRejectNonOwnerUnpublishedVersionCompare() {
        assertThatThrownBy(() ->
            compareService.compareVersions(draftBot.getId(), draftV1.getId(), draftV2.getId(), otherUser.getEmail())
        ).isInstanceOf(AccessDeniedException.class)
         .hasMessageContaining("Access denied to unpublished version comparison");
    }

    @Test
    @DisplayName("Security Check: Allow owner access to UNPUBLISHED version comparison")
    void testAllowOwnerUnpublishedVersionCompare() {
        VersionComparisonResponse response = compareService.compareVersions(
            draftBot.getId(), draftV1.getId(), draftV2.getId(), seller.getEmail()
        );

        assertThat(response).isNotNull();
        assertThat(response.getBotId()).isEqualTo(draftBot.getId());
        assertThat(response.getLogicDiff()).isNotEmpty();
    }

    @Test
    @DisplayName("Public Access: Allow public user to compare PUBLISHED versions")
    void testAllowPublicPublishedVersionCompare() {
        VersionComparisonResponse response = compareService.compareVersions(
            publishedBot.getId(), pubV1.getId(), pubV2.getId(), otherUser.getEmail()
        );

        assertThat(response).isNotNull();
        assertThat(response.getBotId()).isEqualTo(publishedBot.getId());

        // Check metrics diff (win_rate: 60 -> 70, delta +10)
        var winRateMetric = response.getPerformanceMetrics().stream()
            .filter(m -> "win_rate".equals(m.getMetricName()))
            .findFirst().orElseThrow();

        assertThat(winRateMetric.getFromValue()).isEqualTo(60.0);
        assertThat(winRateMetric.getToValue()).isEqualTo(70.0);
        assertThat(winRateMetric.getDelta()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("Line Diff: Line additions, removals, and unchanged lines are correctly identified")
    void testLineDiffCalculation() {
        String from = "rsi > 70\nstop_loss = 2%";
        String to = "rsi > 70\nstop_loss = 1.5%\ntake_profit = 5%";

        var diff = compareService.computeLineDiff(from, to);

        assertThat(diff).extracting(VersionDiffLineDto::getText)
            .containsExactly("rsi > 70", "stop_loss = 2%", "stop_loss = 1.5%", "take_profit = 5%");

        assertThat(diff.get(0).getType()).isEqualTo(VersionDiffLineDto.DiffType.UNCHANGED);
        assertThat(diff.get(1).getType()).isEqualTo(VersionDiffLineDto.DiffType.REMOVED);
        assertThat(diff.get(2).getType()).isEqualTo(VersionDiffLineDto.DiffType.ADDED);
        assertThat(diff.get(3).getType()).isEqualTo(VersionDiffLineDto.DiffType.ADDED);
    }
}
