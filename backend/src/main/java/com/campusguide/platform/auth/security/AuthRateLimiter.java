package com.campusguide.platform.auth.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class AuthRateLimiter {

    private final boolean enabled;
    private final int capacity;
    private final int requestsPerMinute;
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public AuthRateLimiter(
            @Value("${auth.ratelimit.enabled:true}") boolean enabled,
            @Value("${auth.ratelimit.capacity:15}") int capacity,
            @Value("${auth.ratelimit.requests-per-minute:10}") int requestsPerMinute) {
        this.enabled = enabled;
        this.capacity = capacity;
        this.requestsPerMinute = requestsPerMinute;
    }

    public boolean tryAcquire(String ipAddress) {
        if (!enabled) {
            return true;
        }

        String key = (ipAddress != null && !ipAddress.isBlank()) ? ipAddress : "unknown";
        TokenBucket bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(capacity, requestsPerMinute));

        boolean acquired = bucket.tryAcquire();
        if (!acquired) {
            log.warn("Auth rate limit exceeded for IP: {}", key);
        }
        return acquired;
    }

    public void reset(String ipAddress) {
        if (ipAddress != null) {
            buckets.remove(ipAddress);
        }
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
