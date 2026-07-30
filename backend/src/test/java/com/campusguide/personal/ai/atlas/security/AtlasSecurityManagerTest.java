package com.campusguide.personal.ai.atlas.security;

import com.campusguide.personal.ai.atlas.exception.AtlasForbiddenException;
import com.campusguide.personal.ai.atlas.exception.AtlasRateLimitException;
import com.campusguide.personal.ai.atlas.ratelimit.RateLimitPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AtlasSecurityManagerTest {

    private RateLimitPolicy rateLimitPolicy;
    private AtlasSecurityManager securityManager;

    @BeforeEach
    void setUp() {
        rateLimitPolicy = mock(RateLimitPolicy.class);
        securityManager = new AtlasSecurityManager(rateLimitPolicy);
    }

    @Test
    void testEnforceRateLimit_Allowed() {
        when(rateLimitPolicy.tryAcquire(anyString())).thenReturn(true);

        assertDoesNotThrow(() -> securityManager.enforceRateLimit("user123"));
    }

    @Test
    void testEnforceRateLimit_Exceeded_ThrowsException() {
        when(rateLimitPolicy.tryAcquire(anyString())).thenReturn(false);

        assertThrows(AtlasRateLimitException.class, () -> securityManager.enforceRateLimit("user123"));
    }

    @Test
    void testConcurrentExecutionSlot_QuotasEnforced() {
        String user = "quotaUser";
        for (int i = 0; i < 5; i++) {
            assertTrue(securityManager.tryAcquireExecutionSlot(user));
        }

        // 6th execution should fail quota check
        assertFalse(securityManager.tryAcquireExecutionSlot(user));

        // Release one slot
        securityManager.releaseExecutionSlot(user);

        // 6th execution should now succeed
        assertTrue(securityManager.tryAcquireExecutionSlot(user));
    }

    @Test
    void testValidateOwnership_Success() {
        assertDoesNotThrow(() -> securityManager.validateOwnership("user123", "user123", false));
        assertDoesNotThrow(() -> securityManager.validateOwnership("user123", "admin", true));
    }

    @Test
    void testValidateOwnership_Forbidden() {
        assertThrows(AtlasForbiddenException.class, () ->
                securityManager.validateOwnership("user123", "user456", false));
    }
}
