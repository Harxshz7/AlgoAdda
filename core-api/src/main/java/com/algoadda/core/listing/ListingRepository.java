package com.algoadda.core.listing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ListingRepository extends JpaRepository<Listing, UUID> {
    List<Listing> findByActiveTrue();
    List<Listing> findByBotVersionId(UUID botVersionId);
}
