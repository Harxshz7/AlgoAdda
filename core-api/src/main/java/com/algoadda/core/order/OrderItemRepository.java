package com.algoadda.core.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    List<OrderItem> findByOrderId(UUID orderId);

    @Query("SELECT i FROM OrderItem i WHERE i.botVersion.bot.id = :botId AND i.order.status = :status")
    List<OrderItem> findByBotIdAndOrderStatus(@Param("botId") UUID botId, @Param("status") OrderStatus status);

    @Query("SELECT i FROM OrderItem i WHERE i.botVersion.bot.id = :botId AND i.order.status IN (:statuses)")
    List<OrderItem> findByBotIdAndOrderStatusIn(@Param("botId") UUID botId, @Param("statuses") List<OrderStatus> statuses);
}
