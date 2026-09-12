package com.algoadda.core.bot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BotVersionRepository extends JpaRepository<BotVersion, UUID> {
    List<BotVersion> findByBotId(UUID botId);
    Optional<BotVersion> findByBotIdAndVersionNumber(UUID botId, String versionNumber);
}
