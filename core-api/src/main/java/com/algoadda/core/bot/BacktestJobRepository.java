package com.algoadda.core.bot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BacktestJobRepository extends JpaRepository<BacktestJob, UUID> {
    List<BacktestJob> findByStatusOrderByCreatedAtAsc(BacktestJobStatus status);
    Optional<BacktestJob> findFirstByBotVersionIdOrderByCreatedAtDesc(UUID botVersionId);
}
