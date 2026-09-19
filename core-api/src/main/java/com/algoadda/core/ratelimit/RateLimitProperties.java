package com.algoadda.core.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "algoadda.rate-limit.bot-upload")
public class RateLimitProperties {

    /**
     * Maximum number of upload tokens (burst capacity).
     */
    private int capacity = 10;

    /**
     * Time window in minutes for refilling the full capacity tokens.
     */
    private int windowMinutes = 60;

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getWindowMinutes() {
        return windowMinutes;
    }

    public void setWindowMinutes(int windowMinutes) {
        this.windowMinutes = windowMinutes;
    }
}
