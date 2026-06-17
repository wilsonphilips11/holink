package com.holink.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory rate limiter for click tracking: 100 requests/minute per IP by default.
 * Production would use Redis-backed Bucket4j for distributed rate limiting.
 */
@Component
public class ClickRateLimiter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Bandwidth bandwidth;

    public ClickRateLimiter(@Value("${holink.click-rate-limit.requests-per-minute}") int requestsPerMinute) {
        this.bandwidth = Bandwidth.builder()
                .capacity(requestsPerMinute)
                .refillGreedy(requestsPerMinute, Duration.ofMinutes(1))
                .build();
    }

    public void checkRateLimit(String ip) {
        String key = ip == null || ip.isBlank() ? "unknown" : ip;
        Bucket bucket = buckets.computeIfAbsent(key, k -> Bucket.builder().addLimit(bandwidth).build());
        if (!bucket.tryConsume(1)) {
            throw new com.holink.exception.RateLimitExceededException(
                    "Rate limit exceeded. Maximum 100 click requests per minute.");
        }
    }
}
