package com.algoadda.core.bot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BacktestResultRepository extends JpaRepository<BacktestResult, UUID> {
    List<BacktestResult> findByBotVersionId(UUID botVersionId);
    java.util.Optional<BacktestResult> findFirstByBotVersionIdOrderByCreatedAtDesc(UUID botVersionId);
}
