package com.campusguide.personal.ai.atlas.orchestration.coordination;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Engine coordinating parallel agent synchronization, dependency enforcement, result merging, and conflict resolution.
 */
@Slf4j
@Service
public class CoordinationEngine {

    private final Map<String, SynchronizationBarrier> barriers = new ConcurrentHashMap<>();

    public SynchronizationBarrier createBarrier(int expectedCount) {
        SynchronizationBarrier barrier = new SynchronizationBarrier(expectedCount);
        barriers.put(barrier.getBarrierId(), barrier);
        log.info("CoordinationEngine created barrier {} for {} agents", barrier.getBarrierId(), expectedCount);
        return barrier;
    }

    public Optional<SynchronizationBarrier> getBarrier(String barrierId) {
        return Optional.ofNullable(barriers.get(barrierId));
    }

    public boolean arriveAndAwait(String barrierId, String agentId, Object resultData, long timeoutMs) {
        SynchronizationBarrier barrier = barriers.get(barrierId);
        if (barrier == null) {
            log.error("Barrier {} not found", barrierId);
            return false;
        }

        barrier.arrive(agentId, resultData);
        try {
            return barrier.await(timeoutMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while awaiting barrier {}: {}", barrierId, e.getMessage());
            return false;
        }
    }

    public Map<String, Object> mergeResults(Map<String, Object> newResults, CoordinationPolicy policy) {
        return mergeResults(null, newResults, policy);
    }

    public Map<String, Object> mergeResults(Map<String, Object> existingResults, Map<String, Object> newResults, CoordinationPolicy policy) {
        Map<String, Object> combined = new HashMap<>();
        if (existingResults != null) combined.putAll(existingResults);
        if (newResults == null) return combined;

        CoordinationPolicy effectivePolicy = policy != null ? policy : CoordinationPolicy.defaultPolicy();

        newResults.forEach((key, val) -> {
            if (!combined.containsKey(key)) {
                combined.put(key, val);
            } else {
                // Conflict resolution
                if (effectivePolicy.getConflictPolicy() == CoordinationPolicy.ConflictResolutionPolicy.FAIL_ON_CONFLICT) {
                    throw new IllegalStateException("Conflict detected during result merge for key: " + key);
                } else if (effectivePolicy.getConflictPolicy() == CoordinationPolicy.ConflictResolutionPolicy.LATEST_WINS) {
                    combined.put(key, val); // overwrite
                }
            }
        });

        return combined;
    }

    public void removeBarrier(String barrierId) {
        if (barrierId != null) {
            barriers.remove(barrierId);
        }
    }
}
