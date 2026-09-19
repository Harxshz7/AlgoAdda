package com.algoadda.core.user.dto;

import java.util.List;

public class SellerAnalyticsResponse {
    private AnalyticsSummaryDto summary;
    private List<BotAnalyticsDto> bots;

    public SellerAnalyticsResponse() {
    }

    public SellerAnalyticsResponse(AnalyticsSummaryDto summary, List<BotAnalyticsDto> bots) {
        this.summary = summary;
        this.bots = bots;
    }

    public AnalyticsSummaryDto getSummary() { return summary; }
    public void setSummary(AnalyticsSummaryDto summary) { this.summary = summary; }

    public List<BotAnalyticsDto> getBots() { return bots; }
    public void setBots(List<BotAnalyticsDto> bots) { this.bots = bots; }
}
