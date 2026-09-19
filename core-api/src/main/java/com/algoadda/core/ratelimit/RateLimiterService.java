package com.algoadda.core.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterService.class);

    private final RateLimitProperties properties;
    private final Map<UUID, Bucket> sellerBuckets = new ConcurrentHashMap<>();

    public RateLimiterService(RateLimitProperties properties) {
        this.properties = properties;
    }

    /**
     * Attempts to consume 1 token for the specified seller.
     * Throws RateLimitExceededException if the limit is exceeded.
     *
     * @param sellerId The UUID of the seller attempting the upload.
     */
    public void tryConsume(UUID sellerId) {
        if (sellerId == null) {
            return;
        }

        Bucket bucket = sellerBuckets.computeIfAbsent(sellerId, this::createNewBucket);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (!probe.isConsumed()) {
            long nanosToWaitForRefill = probe.getNanosToWaitForRefill();
            long retryAfterSeconds = Math.max(1, (long) Math.ceil(nanosToWaitForRefill / 1_000_000_000.0));
            log.warn("Rate limit exceeded for seller {}. Retry after {} seconds.", sellerId, retryAfterSeconds);
            throw new RateLimitExceededException(
                String.format("Upload rate limit exceeded (%d uploads per %d minutes). Please try again later.",
                    properties.getCapacity(), properties.getWindowMinutes()),
                retryAfterSeconds
            );
        }
    }

    private Bucket createNewBucket(UUID sellerId) {
        Bandwidth limit = Bandwidth.builder()
            .capacity(properties.getCapacity())
            .refillIntervally(properties.getCapacity(), Duration.ofMinutes(properties.getWindowMinutes()))
            .build();

        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    /**
     * Resets the bucket for a specific seller (useful in tests).
     */
    public void reset(UUID sellerId) {
        sellerBuckets.remove(sellerId);
    }

    /**
     * Clears all buckets (useful in test teardown).
     */
    public void resetAll() {
        sellerBuckets.clear();
    }
}
