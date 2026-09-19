package com.algoadda.core.user.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class BotAnalyticsDto {
    private UUID botId;
    private String botName;
    private String strategyType;
    private String status;
    private long totalViews;
    private long totalPurchases;
    private Double conversionRate;
    private BigDecimal grossRevenue;
    private BigDecimal refundedAmount;
    private BigDecimal netRevenue;
    private Double averageRating;

    public BotAnalyticsDto() {
    }

    public BotAnalyticsDto(UUID botId, String botName, String strategyType, String status,
                           long totalViews, long totalPurchases, Double conversionRate,
                           BigDecimal grossRevenue, BigDecimal refundedAmount, BigDecimal netRevenue,
                           Double averageRating) {
        this.botId = botId;
        this.botName = botName;
        this.strategyType = strategyType;
        this.status = status;
        this.totalViews = totalViews;
        this.totalPurchases = totalPurchases;
        this.conversionRate = conversionRate;
        this.grossRevenue = grossRevenue != null ? grossRevenue : BigDecimal.ZERO;
        this.refundedAmount = refundedAmount != null ? refundedAmount : BigDecimal.ZERO;
        this.netRevenue = netRevenue != null ? netRevenue : BigDecimal.ZERO;
        this.averageRating = averageRating;
    }

    public UUID getBotId() { return botId; }
    public void setBotId(UUID botId) { this.botId = botId; }

    public String getBotName() { return botName; }
    public void setBotName(String botName) { this.botName = botName; }

    public String getStrategyType() { return strategyType; }
    public void setStrategyType(String strategyType) { this.strategyType = strategyType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getTotalViews() { return totalViews; }
    public void setTotalViews(long totalViews) { this.totalViews = totalViews; }

    public long getTotalPurchases() { return totalPurchases; }
    public void setTotalPurchases(long totalPurchases) { this.totalPurchases = totalPurchases; }

    public Double getConversionRate() { return conversionRate; }
    public void setConversionRate(Double conversionRate) { this.conversionRate = conversionRate; }

    public BigDecimal getGrossRevenue() { return grossRevenue; }
    public void setGrossRevenue(BigDecimal grossRevenue) { this.grossRevenue = grossRevenue; }

    public BigDecimal getRefundedAmount() { return refundedAmount; }
    public void setRefundedAmount(BigDecimal refundedAmount) { this.refundedAmount = refundedAmount; }

    public BigDecimal getNetRevenue() { return netRevenue; }
    public void setNetRevenue(BigDecimal netRevenue) { this.netRevenue = netRevenue; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
}
