package com.algoadda.core.user.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class AnalyticsSummaryDto {
    private long totalViews;
    private long totalPurchases;
    private BigDecimal totalGrossRevenue;
    private BigDecimal totalRefundedAmount;
    private BigDecimal totalNetRevenue;
    private UUID mostPopularBotId;
    private String mostPopularBotName;
    private long mostPopularBotPurchases;

    public AnalyticsSummaryDto() {
    }

    public AnalyticsSummaryDto(long totalViews, long totalPurchases, BigDecimal totalGrossRevenue,
                               BigDecimal totalRefundedAmount, BigDecimal totalNetRevenue,
                               UUID mostPopularBotId, String mostPopularBotName, long mostPopularBotPurchases) {
        this.totalViews = totalViews;
        this.totalPurchases = totalPurchases;
        this.totalGrossRevenue = totalGrossRevenue != null ? totalGrossRevenue : BigDecimal.ZERO;
        this.totalRefundedAmount = totalRefundedAmount != null ? totalRefundedAmount : BigDecimal.ZERO;
        this.totalNetRevenue = totalNetRevenue != null ? totalNetRevenue : BigDecimal.ZERO;
        this.mostPopularBotId = mostPopularBotId;
        this.mostPopularBotName = mostPopularBotName;
        this.mostPopularBotPurchases = mostPopularBotPurchases;
    }

    public long getTotalViews() { return totalViews; }
    public void setTotalViews(long totalViews) { this.totalViews = totalViews; }

    public long getTotalPurchases() { return totalPurchases; }
    public void setTotalPurchases(long totalPurchases) { this.totalPurchases = totalPurchases; }

    public BigDecimal getTotalGrossRevenue() { return totalGrossRevenue; }
    public void setTotalGrossRevenue(BigDecimal totalGrossRevenue) { this.totalGrossRevenue = totalGrossRevenue; }

    public BigDecimal getTotalRefundedAmount() { return totalRefundedAmount; }
    public void setTotalRefundedAmount(BigDecimal totalRefundedAmount) { this.totalRefundedAmount = totalRefundedAmount; }

    public BigDecimal getTotalNetRevenue() { return totalNetRevenue; }
    public void setTotalNetRevenue(BigDecimal totalNetRevenue) { this.totalNetRevenue = totalNetRevenue; }

    public UUID getMostPopularBotId() { return mostPopularBotId; }
    public void setMostPopularBotId(UUID mostPopularBotId) { this.mostPopularBotId = mostPopularBotId; }

    public String getMostPopularBotName() { return mostPopularBotName; }
    public void setMostPopularBotName(String mostPopularBotName) { this.mostPopularBotName = mostPopularBotName; }

    public long getMostPopularBotPurchases() { return mostPopularBotPurchases; }
    public void setMostPopularBotPurchases(long mostPopularBotPurchases) { this.mostPopularBotPurchases = mostPopularBotPurchases; }
}
