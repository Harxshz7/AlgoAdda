package com.algoadda.core.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Optional<Review> findByBotIdAndBuyerId(UUID botId, UUID buyerId);

    Page<Review> findByBotIdOrderByCreatedAtDesc(UUID botId, Pageable pageable);

    List<Review> findByBotIdOrderByCreatedAtDesc(UUID botId);

    @Query("SELECT COALESCE(AVG(CAST(r.rating AS double)), 0.0) FROM Review r WHERE r.bot.id = :botId")
    Double getAverageRatingByBotId(@Param("botId") UUID botId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.bot.id = :botId")
    Long countByBotId(@Param("botId") UUID botId);
}
