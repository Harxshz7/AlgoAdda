package com.algoadda.core.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    List<Subscription> findByBuyerId(UUID buyerId);
    Optional<Subscription> findByRazorpaySubscriptionId(String razorpaySubscriptionId);
    Optional<Subscription> findByBuyerIdAndListingId(UUID buyerId, UUID listingId);
}
