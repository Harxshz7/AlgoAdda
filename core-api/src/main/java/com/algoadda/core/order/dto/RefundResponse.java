package com.algoadda.core.order.dto;

import com.algoadda.core.order.OrderStatus;
import java.util.UUID;

public class RefundResponse {

    private UUID orderId;
    private OrderStatus status;
    private String refundId;
    private boolean revoked;

    public RefundResponse() {
    }

    public RefundResponse(UUID orderId, OrderStatus status, String refundId, boolean revoked) {
        this.orderId = orderId;
        this.status = status;
        this.refundId = refundId;
        this.revoked = revoked;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getRefundId() {
        return refundId;
    }

    public void setRefundId(String refundId) {
        this.refundId = refundId;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }
}
