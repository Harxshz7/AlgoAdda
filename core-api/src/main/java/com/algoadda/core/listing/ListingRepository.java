package com.algoadda.core.listing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ListingRepository extends JpaRepository<Listing, UUID> {
    List<Listing> findByActiveTrue();
    List<Listing> findByBotVersionId(UUID botVersionId);
    Optional<Listing> findFirstByBotVersion_Bot_IdAndActiveTrueOrderByCreatedAtDesc(UUID botId);
    List<Listing> findByBotVersion_Bot_IdInAndActiveTrue(Collection<UUID> botIds);
}
