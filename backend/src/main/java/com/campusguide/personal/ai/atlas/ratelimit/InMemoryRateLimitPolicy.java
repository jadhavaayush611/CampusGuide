package com.campusguide.personal.ai.atlas.ratelimit;

import com.campusguide.personal.ai.atlas.config.AtlasProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class InMemoryRateLimitPolicy implements RateLimitPolicy {

    private final AtlasProperties atlasProperties;
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public InMemoryRateLimitPolicy(AtlasProperties atlasProperties) {
        this.atlasProperties = atlasProperties;
    }

    @Override
    public boolean tryAcquire(String key) {
        if (!atlasProperties.getRateLimit().isEnabled()) {
            return true;
        }

        String effectiveKey = (key != null && !key.isBlank()) ? key : "anonymous";
        TokenBucket bucket = buckets.computeIfAbsent(effectiveKey, k -> new TokenBucket(
                atlasProperties.getRateLimit().getCapacity(),
                atlasProperties.getRateLimit().getRequestsPerMinute()
        ));

        boolean acquired = bucket.tryAcquire();
        if (!acquired) {
            log.warn("Rate limit exceeded for key: {}", effectiveKey);
        }
        return acquired;
    }

    @Override
    public void reset(String key) {
        if (key != null) {
            buckets.remove(key);
        }
    }

    @Override
    public RateLimitStatus getStatus(String key) {
        String effectiveKey = (key != null && !key.isBlank()) ? key : "anonymous";
        TokenBucket bucket = buckets.get(effectiveKey);
        int capacity = atlasProperties.getRateLimit().getCapacity();
        if (bucket == null) {
            return RateLimitStatus.builder()
                    .key(effectiveKey)
                    .allowed(true)
                    .remainingTokens(capacity)
                    .capacity(capacity)
                    .resetInSeconds(0)
                    .build();
        }
        return bucket.getStatus(effectiveKey, capacity);
    }

    private static class TokenBucket {
        private final int capacity;
        private final double refillRatePerMs;
        private double tokens;
        private long lastRefillTimestamp;

        public TokenBucket(int capacity, int requestsPerMinute) {
            this.capacity = capacity;
            this.tokens = capacity;
            this.refillRatePerMs = (double) requestsPerMinute / (60.0 * 1000.0);
            this.lastRefillTimestamp = System.currentTimeMillis();
        }

        public synchronized boolean tryAcquire() {
            refill();
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }

        public synchronized RateLimitStatus getStatus(String key, int capacityConfig) {
            refill();
            int remaining = (int) tokens;
            long timeToFullMs = (long) ((capacity - tokens) / refillRatePerMs);
            return RateLimitStatus.builder()
                    .key(key)
                    .allowed(tokens >= 1.0)
                    .remainingTokens(remaining)
                    .capacity(capacityConfig)
                    .resetInSeconds(Math.max(0, timeToFullMs / 1000))
                    .build();
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTimestamp;
            if (elapsed > 0) {
                tokens = Math.min(capacity, tokens + (elapsed * refillRatePerMs));
                lastRefillTimestamp = now;
            }
        }
    }
}
