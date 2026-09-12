package com.algoadda.core.bot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BotRepository extends JpaRepository<Bot, UUID> {
    List<Bot> findBySellerId(UUID sellerId);
    List<Bot> findByStatus(BotStatus status);
}
