package com.algoadda.core.order.dto;

import com.algoadda.core.order.OrderStatus;
import java.math.BigDecimal;
import java.util.UUID;

public class OrderResponse {

    private UUID orderId;
    private String razorpayOrderId;
    private BigDecimal amount;
    private Long amountInPaise;
    private String currency;
    private String razorpayKeyId;
    private OrderStatus status;

    public OrderResponse() {
    }

    public OrderResponse(UUID orderId, String razorpayOrderId, BigDecimal amount, Long amountInPaise, String currency, String razorpayKeyId, OrderStatus status) {
        this.orderId = orderId;
        this.razorpayOrderId = razorpayOrderId;
        this.amount = amount;
        this.amountInPaise = amountInPaise;
        this.currency = currency;
        this.razorpayKeyId = razorpayKeyId;
        this.status = status;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getAmountInPaise() {
        return amountInPaise;
    }

    public void setAmountInPaise(Long amountInPaise) {
        this.amountInPaise = amountInPaise;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getRazorpayKeyId() {
        return razorpayKeyId;
    }

    public void setRazorpayKeyId(String razorpayKeyId) {
        this.razorpayKeyId = razorpayKeyId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
