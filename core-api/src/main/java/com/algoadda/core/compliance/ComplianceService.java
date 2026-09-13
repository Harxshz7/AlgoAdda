package com.algoadda.core.compliance;

import com.algoadda.core.bot.*;
import com.algoadda.core.compliance.dto.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@SuppressWarnings("null")
@Service
public class ComplianceService {

    private static final Logger log = LoggerFactory.getLogger(ComplianceService.class);

    private final ComplianceCheckRepository complianceCheckRepository;
    private final BotVersionRepository botVersionRepository;
    private final BotRepository botRepository;
    private final BacktestResultRepository backtestResultRepository;
    private final ObjectMapper objectMapper;
    private final List<String> blocklist;

    public ComplianceService(
        ComplianceCheckRepository complianceCheckRepository,
        BotVersionRepository botVersionRepository,
        BotRepository botRepository,
        BacktestResultRepository backtestResultRepository,
        ObjectMapper objectMapper,
        @Value("${algoadda.compliance.blocklist:guaranteed,assured returns,risk-free,no loss,100% profit,guaranteed profit,risk free,assured return}") List<String> blocklist
    ) {
        this.complianceCheckRepository = complianceCheckRepository;
        this.botVersionRepository = botVersionRepository;
        this.botRepository = botRepository;
        this.backtestResultRepository = backtestResultRepository;
        this.objectMapper = objectMapper;
        this.blocklist = blocklist != null ? blocklist : Collections.emptyList();
    }

    @Transactional
    public ComplianceCheckResponse runAutomatedComplianceCheck(BotVersion botVersion) {
        log.info("Running automated compliance check for botVersion {}", botVersion.getId());

        // Check 1: disclosedLogicPresent
        CheckItemResult disclosedLogicResult = checkDisclosedLogic(botVersion);

        // Check 2: noGuaranteedReturnLanguage
        CheckItemResult noGuaranteedReturnResult = checkGuaranteedReturnLanguage(botVersion);

        // Check 3: riskDisclaimerPresent
        CheckItemResult riskDisclaimerResult = checkRiskDisclaimer(botVersion);

        // Check 4: backtestCompleted
        CheckItemResult backtestCompletedResult = checkBacktestStatus(botVersion);

        boolean overallPassed = disclosedLogicResult.isPassed()
            && noGuaranteedReturnResult.isPassed()
            && riskDisclaimerResult.isPassed()
            && backtestCompletedResult.isPassed();

        ComplianceChecklistPayload checklistPayload = new ComplianceChecklistPayload(
            disclosedLogicResult,
            noGuaranteedReturnResult,
            riskDisclaimerResult,
            backtestCompletedResult
        );

        String jsonResults;
        try {
            jsonResults = objectMapper.writeValueAsString(checklistPayload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize compliance checklist payload: {}", e.getMessage());
            jsonResults = "{}";
        }

        ComplianceCheck check = ComplianceCheck.builder()
            .botVersion(botVersion)
            .reviewerType(ReviewerType.AUTO)
            .checklistResults(jsonResults)
            .passed(overallPassed)
            .build();

        ComplianceCheck savedCheck = complianceCheckRepository.save(check);
        log.info("Automated compliance check completed for botVersion {}. Passed = {}", botVersion.getId(), overallPassed);

        return mapToResponse(savedCheck);
    }

    @Transactional
    public ComplianceCheckResponse runManualReview(UUID botVersionId, boolean passed, String notes) {
        BotVersion botVersion = botVersionRepository.findById(botVersionId)
            .orElseThrow(() -> new IllegalArgumentException("Bot version not found with id: " + botVersionId));

        log.info("Executing manual compliance review for botVersion {}. Passed = {}", botVersionId, passed);

        Map<String, Object> manualPayload = new HashMap<>();
        manualPayload.put("manualReview", true);
        manualPayload.put("notes", notes != null ? notes : "");
        manualPayload.put("overridePassed", passed);

        // Preserve automated checklist if available for reference
        ComplianceCheck latestAuto = complianceCheckRepository.findFirstByBotVersionIdOrderByReviewedAtDesc(botVersionId).orElse(null);
        if (latestAuto != null && latestAuto.getChecklistResults() != null) {
            try {
                Object autoChecklist = objectMapper.readValue(latestAuto.getChecklistResults(), Object.class);
                manualPayload.put("automatedChecklist", autoChecklist);
            } catch (Exception e) {
                manualPayload.put("automatedChecklistRaw", latestAuto.getChecklistResults());
            }
        }

        String jsonResults;
        try {
            jsonResults = objectMapper.writeValueAsString(manualPayload);
        } catch (JsonProcessingException e) {
            jsonResults = "{\"notes\":\"" + (notes != null ? notes : "") + "\"}";
        }

        ComplianceCheck manualCheck = ComplianceCheck.builder()
            .botVersion(botVersion)
            .reviewerType(ReviewerType.MANUAL)
            .checklistResults(jsonResults)
            .passed(passed)
            .build();

        ComplianceCheck savedCheck = complianceCheckRepository.save(manualCheck);
        return mapToResponse(savedCheck);
    }

    @Transactional(readOnly = true)
    public Optional<ComplianceCheckResponse> getLatestComplianceCheck(UUID botVersionId) {
        return complianceCheckRepository.findFirstByBotVersionIdOrderByReviewedAtDesc(botVersionId)
            .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<ComplianceCheckResponse> getAdminReviewQueue() {
        List<ComplianceCheck> failedOrPending = complianceCheckRepository.findByPassedFalseOrderByReviewedAtDesc();
        List<ComplianceCheckResponse> queue = new ArrayList<>();
        Set<UUID> seenVersions = new HashSet<>();

        for (ComplianceCheck check : failedOrPending) {
            UUID vId = check.getBotVersion().getId();
            if (!seenVersions.contains(vId)) {
                seenVersions.add(vId);
                // Check if the latest check is still failed
                Optional<ComplianceCheck> latest = complianceCheckRepository.findFirstByBotVersionIdOrderByReviewedAtDesc(vId);
                if (latest.isPresent() && !latest.get().isPassed()) {
                    queue.add(mapToResponse(latest.get()));
                }
            }
        }
        return queue;
    }

    public CheckItemResult checkDisclosedLogic(BotVersion botVersion) {
        String logic = botVersion.getDisclosedLogic();
        if (logic != null && !logic.trim().isEmpty()) {
            return new CheckItemResult(true, "Disclosed logic provided");
        } else {
            return new CheckItemResult(false, "Disclosed logic is missing or empty");
        }
    }

    public CheckItemResult checkGuaranteedReturnLanguage(BotVersion botVersion) {
        Bot bot = resolveBot(botVersion);
        String textToScan = (
            (botVersion.getDisclosedLogic() != null ? botVersion.getDisclosedLogic() : "") + " " +
            (bot != null && bot.getDescription() != null ? bot.getDescription() : "") + " " +
            (bot != null && bot.getName() != null ? bot.getName() : "")
        ).toLowerCase();

        for (String phrase : blocklist) {
            if (phrase != null && !phrase.isBlank()) {
                String cleanPhrase = phrase.trim().toLowerCase();
                if (textToScan.contains(cleanPhrase)) {
                    return new CheckItemResult(false, "Prohibited guaranteed return phrase detected: '" + phrase.trim() + "'");
                }
            }
        }
        return new CheckItemResult(true, "No prohibited financial guarantee language found");
    }

    public CheckItemResult checkRiskDisclaimer(BotVersion botVersion) {
        Bot bot = resolveBot(botVersion);
        String disclaimer = bot != null ? bot.getRiskDisclaimer() : null;

        if (disclaimer != null && !disclaimer.trim().isEmpty()) {
            return new CheckItemResult(true, "Risk disclaimer present");
        } else {
            return new CheckItemResult(false, "Risk disclaimer text is missing");
        }
    }

    public CheckItemResult checkBacktestStatus(BotVersion botVersion) {
        boolean hasResult = backtestResultRepository.findFirstByBotVersionIdOrderByCreatedAtDesc(botVersion.getId()).isPresent();
        boolean completed = botVersion.getBacktestStatus() == BacktestStatus.COMPLETED;

        if (hasResult && completed) {
            return new CheckItemResult(true, "Backtest completed successfully");
        } else if (!hasResult) {
            return new CheckItemResult(false, "Backtest result record does not exist");
        } else {
            return new CheckItemResult(false, "Backtest status is " + botVersion.getBacktestStatus());
        }
    }

    private Bot resolveBot(BotVersion botVersion) {
        if (botVersion == null || botVersion.getBot() == null) {
            return null;
        }
        if (botRepository != null && botVersion.getBot().getId() != null) {
            return botRepository.findById(botVersion.getBot().getId()).orElse(botVersion.getBot());
        }
        return botVersion.getBot();
    }

    private ComplianceCheckResponse mapToResponse(ComplianceCheck check) {
        return ComplianceCheckResponse.builder()
            .id(check.getId())
            .botVersionId(check.getBotVersion().getId())
            .reviewerType(check.getReviewerType())
            .checklistResults(check.getChecklistResults())
            .passed(check.isPassed())
            .reviewedAt(check.getReviewedAt())
            .build();
    }
}
