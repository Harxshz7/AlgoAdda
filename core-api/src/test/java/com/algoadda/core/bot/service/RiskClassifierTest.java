package com.algoadda.core.bot.service;

import com.algoadda.core.bot.RiskLabel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RiskClassifierTest {

    @Test
    @DisplayName("Should classify exact conservative drawdown threshold boundaries")
    void testConservativeThresholdBoundaries() {
        assertEquals(RiskLabel.CONSERVATIVE, RiskClassifier.classify(0.0, null));
        assertEquals(RiskLabel.CONSERVATIVE, RiskClassifier.classify(14.9, null));
        assertEquals(RiskLabel.CONSERVATIVE, RiskClassifier.classify(15.0, null)); // Boundary edge
        assertEquals(RiskLabel.MODERATE, RiskClassifier.classify(15.1, null));   // Immediately past boundary
    }

    @Test
    @DisplayName("Should classify exact moderate drawdown threshold boundaries")
    void testModerateThresholdBoundaries() {
        assertEquals(RiskLabel.MODERATE, RiskClassifier.classify(29.9, null));
        assertEquals(RiskLabel.MODERATE, RiskClassifier.classify(30.0, null));   // Boundary edge
        assertEquals(RiskLabel.AGGRESSIVE, RiskClassifier.classify(30.1, null)); // Immediately past boundary
        assertEquals(RiskLabel.AGGRESSIVE, RiskClassifier.classify(55.0, null));
    }

    @Test
    @DisplayName("Should correctly handle fractional decimal inputs (0 to 1.0)")
    void testFractionalDecimalInputs() {
        assertEquals(RiskLabel.CONSERVATIVE, RiskClassifier.classify(0.12, null)); // 12%
        assertEquals(RiskLabel.CONSERVATIVE, RiskClassifier.classify(0.15, null)); // 15%
        assertEquals(RiskLabel.MODERATE, RiskClassifier.classify(0.151, null));    // 15.1%
        assertEquals(RiskLabel.MODERATE, RiskClassifier.classify(0.30, null));     // 30%
        assertEquals(RiskLabel.AGGRESSIVE, RiskClassifier.classify(0.35, null));   // 35%
    }

    @Test
    @DisplayName("Should handle negative drawdowns by taking absolute value")
    void testNegativeDrawdowns() {
        assertEquals(RiskLabel.CONSERVATIVE, RiskClassifier.classify(-10.0, null));
        assertEquals(RiskLabel.MODERATE, RiskClassifier.classify(-25.0, null));
        assertEquals(RiskLabel.AGGRESSIVE, RiskClassifier.classify(-40.0, null));
    }

    @Test
    @DisplayName("Should account for volatility thresholds when present")
    void testVolatilityThresholds() {
        // Drawdown is conservative (10%), but high volatility (35%) elevates label to AGGRESSIVE
        assertEquals(RiskLabel.AGGRESSIVE, RiskClassifier.classify(10.0, 35.0));
        // Drawdown conservative (10%), moderate volatility (20%) elevates to MODERATE
        assertEquals(RiskLabel.MODERATE, RiskClassifier.classify(10.0, 20.0));
        // Drawdown conservative (10%), low volatility (12%) stays CONSERVATIVE
        assertEquals(RiskLabel.CONSERVATIVE, RiskClassifier.classify(10.0, 12.0));
    }

    @ParameterizedTest
    @CsvSource({
        "'{\"max_drawdown\": 12.5}', CONSERVATIVE",
        "'{\"max_drawdown\": 15.0}', CONSERVATIVE",
        "'{\"max_drawdown\": 18.0}', MODERATE",
        "'{\"max_drawdown\": 30.0}', MODERATE",
        "'{\"max_drawdown\": 45.2}', AGGRESSIVE",
        "'{\"max_dd\": \"12.0%\"}', CONSERVATIVE",
        "'invalid json', MODERATE",
        "'{}', MODERATE"
    })
    @DisplayName("Should parse JSON metrics strings correctly")
    void testJsonParsing(String json, RiskLabel expected) {
        assertEquals(expected, RiskClassifier.classify(json));
    }
}
