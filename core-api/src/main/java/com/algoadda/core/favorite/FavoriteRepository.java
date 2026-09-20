package com.algoadda.core.favorite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    Optional<Favorite> findByBuyerIdAndBotId(UUID buyerId, UUID botId);

    boolean existsByBuyerIdAndBotId(UUID buyerId, UUID botId);

    void deleteByBuyerIdAndBotId(UUID buyerId, UUID botId);

    List<Favorite> findByBuyerIdOrderByCreatedAtDesc(UUID buyerId);

    @Query("SELECT f.bot.id FROM Favorite f WHERE f.buyer.id = :buyerId")
    Set<UUID> findBotIdsByBuyerId(@Param("buyerId") UUID buyerId);
}
