package com.algoadda.core.bot.dto;

public class MetricDeltaDto {
    private String metricName;
    private Double fromValue;
    private Double toValue;
    private Double delta;

    public MetricDeltaDto() {
    }

    public MetricDeltaDto(String metricName, Double fromValue, Double toValue, Double delta) {
        this.metricName = metricName;
        this.fromValue = fromValue;
        this.toValue = toValue;
        this.delta = delta;
    }

    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }

    public Double getFromValue() { return fromValue; }
    public void setFromValue(Double fromValue) { this.fromValue = fromValue; }

    public Double getToValue() { return toValue; }
    public void setToValue(Double toValue) { this.toValue = toValue; }

    public Double getDelta() { return delta; }
    public void setDelta(Double delta) { this.delta = delta; }
}
