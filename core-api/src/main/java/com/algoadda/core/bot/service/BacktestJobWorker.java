package com.algoadda.core.bot.service;

import com.algoadda.core.bot.*;
import com.algoadda.core.compliance.ComplianceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;

@SuppressWarnings("null")
@Component
@EnableScheduling
public class BacktestJobWorker {

    private static final Logger log = LoggerFactory.getLogger(BacktestJobWorker.class);

    private final BacktestJobRepository backtestJobRepository;
    private final BotVersionRepository botVersionRepository;
    private final BacktestServiceClient backtestServiceClient;
    private final ComplianceService complianceService;

    // Max 2 concurrent worker tasks to avoid overwhelming backtest-service
    private final Semaphore concurrencySemaphore = new Semaphore(2);

    public BacktestJobWorker(
        BacktestJobRepository backtestJobRepository,
        BotVersionRepository botVersionRepository,
        BacktestServiceClient backtestServiceClient,
        ComplianceService complianceService
    ) {
        this.backtestJobRepository = backtestJobRepository;
        this.botVersionRepository = botVersionRepository;
        this.backtestServiceClient = backtestServiceClient;
        this.complianceService = complianceService;
    }

    @Scheduled(fixedDelayString = "${algoadda.backtest.poll-interval-ms:2000}")
    public void processQueuedJobs() {
        List<BacktestJob> queuedJobs = backtestJobRepository.findByStatusOrderByCreatedAtAsc(BacktestJobStatus.QUEUED);
        if (queuedJobs.isEmpty()) {
            return;
        }

        for (BacktestJob job : queuedJobs) {
            if (!concurrencySemaphore.tryAcquire()) {
                log.debug("Concurrency limit reached for backtest worker. Will process remaining queued jobs in next cycle.");
                break;
            }
            try {
                processSingleJob(job.getId());
            } finally {
                concurrencySemaphore.release();
            }
        }
    }

    @Transactional
    public void processSingleJob(UUID jobId) {
        BacktestJob job = backtestJobRepository.findById(jobId).orElse(null);
        if (job == null || job.getStatus() != BacktestJobStatus.QUEUED) {
            return;
        }

        job.setStatus(BacktestJobStatus.RUNNING);
        job.setStartedAt(Instant.now());
        backtestJobRepository.save(job);

        BotVersion botVersion = job.getBotVersion();
        botVersion.setBacktestStatus(BacktestStatus.RUNNING);
        botVersionRepository.save(botVersion);

        log.info("Started background backtest job {} for botVersion {}", job.getId(), botVersion.getId());

        try {
            BacktestServiceClient.ExecutionResult result = backtestServiceClient.runBacktestJob(
                botVersion,
                job.getStrategyConfig(),
                job.getDateRangeStart(),
                job.getDateRangeEnd()
            );

            if (result.isSuccess()) {
                job.setStatus(BacktestJobStatus.COMPLETED);
                job.setCompletedAt(Instant.now());
                job.setErrorMessage(null);
                backtestJobRepository.save(job);

                botVersion.setBacktestStatus(BacktestStatus.COMPLETED);
                BotVersion savedVersion = botVersionRepository.save(botVersion);

                log.info("Backtest job {} COMPLETED successfully for botVersion {}", job.getId(), botVersion.getId());

                try {
                    complianceService.runAutomatedComplianceCheck(savedVersion);
                } catch (Exception e) {
                    log.error("Automated compliance check failed following backtest job completion for botVersion {}: {}", botVersion.getId(), e.getMessage());
                }
            } else {
                String errorMsg = result.getErrorMessage() != null ? result.getErrorMessage() : "Backtest execution failed";
                job.setStatus(BacktestJobStatus.FAILED);
                job.setCompletedAt(Instant.now());
                job.setErrorMessage(errorMsg);
                backtestJobRepository.save(job);

                botVersion.setBacktestStatus(BacktestStatus.FAILED);
                botVersionRepository.save(botVersion);

                log.warn("Backtest job {} FAILED for botVersion {}: {}", job.getId(), botVersion.getId(), errorMsg);
            }
        } catch (Exception e) {
            String errorMsg = "Backtest execution error: " + e.getMessage();
            job.setStatus(BacktestJobStatus.FAILED);
            job.setCompletedAt(Instant.now());
            job.setErrorMessage(errorMsg);
            backtestJobRepository.save(job);

            botVersion.setBacktestStatus(BacktestStatus.FAILED);
            botVersionRepository.save(botVersion);

            log.error("Exception during backtest job {} for botVersion {}: {}", job.getId(), botVersion.getId(), e.getMessage(), e);
        }
    }
}
