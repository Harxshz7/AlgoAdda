package com.algoadda.core.bot.service;

import com.algoadda.core.bot.RiskLabel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RiskClassifier {

    private static final Logger log = LoggerFactory.getLogger(RiskClassifier.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Named threshold constants (in percentage e.g. 15.0 = 15.0%)
    public static final double CONSERVATIVE_MAX_DRAWDOWN_THRESHOLD = 15.0;
    public static final double MODERATE_MAX_DRAWDOWN_THRESHOLD = 30.0;

    public static final double CONSERVATIVE_MAX_VOLATILITY_THRESHOLD = 15.0;
    public static final double MODERATE_MAX_VOLATILITY_THRESHOLD = 30.0;

    private RiskClassifier() {
        // Utility class
    }

    public static RiskLabel classify(String metricsJson) {
        if (metricsJson == null || metricsJson.isBlank()) {
            return RiskLabel.MODERATE;
        }

        try {
            JsonNode root = objectMapper.readTree(metricsJson);
            return classify(root);
        } catch (Exception e) {
            log.warn("Failed to parse metrics JSON for risk classification: {}", e.getMessage());
            return RiskLabel.MODERATE;
        }
    }

    public static RiskLabel classify(JsonNode metricsNode) {
        if (metricsNode == null || metricsNode.isMissingNode() || metricsNode.isNull()) {
            return RiskLabel.MODERATE;
        }

        Double maxDrawdown = extractDoubleMetric(metricsNode, "max_drawdown", "maxDrawdown", "max_dd");
        Double volatility = extractDoubleMetric(metricsNode, "volatility", "annualized_volatility", "volatility_pct");

        return classify(maxDrawdown, volatility);
    }

    public static RiskLabel classify(Double maxDrawdown, Double volatility) {
        if (maxDrawdown == null) {
            return RiskLabel.MODERATE;
        }

        // Normalize ratio to percentage if specified in range (0, 1.0]
        double dd = maxDrawdown;
        if (dd > 0 && dd <= 1.0) {
            dd = dd * 100.0;
        }
        // Normalize negative drawdown to positive absolute value
        dd = Math.abs(dd);

        double vol = volatility != null ? Math.abs(volatility) : 0.0;
        if (vol > 0 && vol <= 1.0) {
            vol = vol * 100.0;
        }

        if (dd <= CONSERVATIVE_MAX_DRAWDOWN_THRESHOLD && (volatility == null || vol <= CONSERVATIVE_MAX_VOLATILITY_THRESHOLD)) {
            return RiskLabel.CONSERVATIVE;
        }

        if (dd <= MODERATE_MAX_DRAWDOWN_THRESHOLD && (volatility == null || vol <= MODERATE_MAX_VOLATILITY_THRESHOLD)) {
            return RiskLabel.MODERATE;
        }

        return RiskLabel.AGGRESSIVE;
    }

    private static Double extractDoubleMetric(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.hasNonNull(key)) {
                JsonNode valNode = node.get(key);
                if (valNode.isNumber()) {
                    return valNode.asDouble();
                } else if (valNode.isTextual()) {
                    try {
                        String txt = valNode.asText().replace("%", "").trim();
                        return Double.parseDouble(txt);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return null;
    }
}
