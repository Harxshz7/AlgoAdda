package com.algoadda.core.listing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ListingViewRepository extends JpaRepository<ListingView, UUID> {
    long countByListingId(UUID listingId);

    @Query("SELECT COUNT(v) FROM ListingView v WHERE v.listing.botVersion.bot.id = :botId")
    long countViewsByBotId(@Param("botId") UUID botId);
}
