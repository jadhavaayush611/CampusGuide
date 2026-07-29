package com.campusguide.personal.ai.atlas.ratelimit;

import com.campusguide.personal.ai.atlas.config.AtlasProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRateLimitPolicyTest {

    private AtlasProperties atlasProperties;
    private InMemoryRateLimitPolicy rateLimitPolicy;

    @BeforeEach
    void setUp() {
        atlasProperties = new AtlasProperties();
        atlasProperties.getRateLimit().setEnabled(true);
        atlasProperties.getRateLimit().setCapacity(2);
        atlasProperties.getRateLimit().setRequestsPerMinute(60);
        rateLimitPolicy = new InMemoryRateLimitPolicy(atlasProperties);
    }

    @Test
    void testRateLimitingEnforcement() {
        String key = "user-test-1";

        assertTrue(rateLimitPolicy.tryAcquire(key));
        assertTrue(rateLimitPolicy.tryAcquire(key));
        assertFalse(rateLimitPolicy.tryAcquire(key)); // 3rd request exceeds capacity 2

        RateLimitStatus status = rateLimitPolicy.getStatus(key);
        assertEquals(key, status.getKey());
        assertEquals(0, status.getRemainingTokens());
        assertFalse(status.isAllowed());

        rateLimitPolicy.reset(key);
        assertTrue(rateLimitPolicy.tryAcquire(key));
    }
}
