package com.campusguide.personal.ai.atlas.security;

import com.campusguide.personal.ai.atlas.exception.AtlasForbiddenException;
import com.campusguide.personal.ai.atlas.exception.AtlasRateLimitException;
import com.campusguide.personal.ai.atlas.ratelimit.RateLimitPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Security, quota, throttling, and metadata audit logging manager for Atlas API Platform.
 * Enforces rate limits, concurrent execution quotas, ownership checks, and metadata-only audit logging.
 *
 * PRIVACY GUARANTEE: Never logs prompts or execution payload data.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AtlasSecurityManager {

    private final RateLimitPolicy rateLimitPolicy;

    private static final int MAX_CONCURRENT_PER_USER = 5;
    private static final int MAX_CONCURRENT_SYSTEM = 20;

    private final Map<String, AtomicInteger> userConcurrentExecutions = new ConcurrentHashMap<>();
    private final AtomicInteger systemConcurrentExecutions = new AtomicInteger(0);

    /**
     * Verifies rate limit for user or client key.
     */
    public void enforceRateLimit(String userOrClientKey) {
        if (rateLimitPolicy != null && !rateLimitPolicy.tryAcquire(userOrClientKey)) {
            logAudit("RATE_LIMIT_EXCEEDED", userOrClientKey, null, 0, "THROTTLED");
            throw new AtlasRateLimitException("Rate limit exceeded for client: " + userOrClientKey);
        }
    }

    /**
     * Acquires a concurrent execution slot for workflow execution.
     */
    public boolean tryAcquireExecutionSlot(String userId) {
        if (systemConcurrentExecutions.get() >= MAX_CONCURRENT_SYSTEM) {
            logAudit("QUOTA_EXCEEDED", userId, null, 0, "SYSTEM_CONCURRENT_LIMIT_REACHED");
            return false;
        }

        String effectiveUser = (userId != null && !userId.isBlank()) ? userId : "anonymous";
        AtomicInteger userCounter = userConcurrentExecutions.computeIfAbsent(effectiveUser, k -> new AtomicInteger(0));
        
        if (userCounter.get() >= MAX_CONCURRENT_PER_USER) {
            logAudit("QUOTA_EXCEEDED", userId, null, 0, "USER_CONCURRENT_LIMIT_REACHED");
            return false;
        }

        userCounter.incrementAndGet();
        systemConcurrentExecutions.incrementAndGet();
        return true;
    }

    /**
     * Releases a concurrent execution slot.
     */
    public void releaseExecutionSlot(String userId) {
        String effectiveUser = (userId != null && !userId.isBlank()) ? userId : "anonymous";
        AtomicInteger userCounter = userConcurrentExecutions.get(effectiveUser);
        if (userCounter != null && userCounter.get() > 0) {
            userCounter.decrementAndGet();
        }
        if (systemConcurrentExecutions.get() > 0) {
            systemConcurrentExecutions.decrementAndGet();
        }
    }

    /**
     * Validates resource ownership for a target user ID.
     */
    public void validateOwnership(String resourceOwnerId, String requestUserId, boolean isAdmin) {
        if (isAdmin) {
            return;
        }
        if (resourceOwnerId == null || requestUserId == null || !resourceOwnerId.equals(requestUserId)) {
            logAudit("OWNERSHIP_VIOLATION", requestUserId, resourceOwnerId, 0, "ACCESS_DENIED");
            throw new AtlasForbiddenException("Access denied: You do not own this resource");
        }
    }

    /**
     * Logs metadata-only audit information.
     * NEVER logs prompt text, user inputs, or execution payload content.
     */
    public void logAudit(String operation, String userId, String resourceId, long latencyMs, String status) {
        log.info("[ATLAS-AUDIT] operation={} userId={} resourceId={} latencyMs={} status={}",
                operation,
                userId != null ? userId : "anonymous",
                resourceId != null ? resourceId : "N/A",
                latencyMs,
                status);
    }
}
