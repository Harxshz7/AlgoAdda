package com.algoadda.core.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LicenseRepository extends JpaRepository<License, UUID> {
    List<License> findByBuyerId(UUID buyerId);
    List<License> findByBotVersionId(UUID botVersionId);
    List<License> findByOrderId(UUID orderId);
    List<License> findBySubscriptionId(UUID subscriptionId);
}
