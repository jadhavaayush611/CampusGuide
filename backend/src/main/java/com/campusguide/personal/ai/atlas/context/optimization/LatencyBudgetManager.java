package com.campusguide.personal.ai.atlas.context.optimization;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages allocation and enforcement of real-time latency budgets during retrieval,
 * supporting parallel retrieval tracking, degradation metrics, and size bounds.
 */
@Component
@Slf4j
public class LatencyBudgetManager {

    @Getter
    private final long defaultBudgetMs;

    private final Map<String, Long> strategyLatencies = new ConcurrentHashMap<>();

    public LatencyBudgetManager() {
        this.defaultBudgetMs = 300L; // 300ms default latency budget
    }

    public LatencyBudgetManager(long defaultBudgetMs) {
        this.defaultBudgetMs = defaultBudgetMs;
    }

    /**
     * Checks if remaining time budget allows starting another strategy.
     */
    public boolean hasRemainingBudget(long startTimeMs, long customBudgetMs) {
        long budget = customBudgetMs > 0 ? customBudgetMs : defaultBudgetMs;
        long elapsed = System.currentTimeMillis() - startTimeMs;
        return elapsed < budget;
    }

    public long getRemainingBudgetMs(long startTimeMs, long customBudgetMs) {
        long budget = customBudgetMs > 0 ? customBudgetMs : defaultBudgetMs;
        long elapsed = System.currentTimeMillis() - startTimeMs;
        return Math.max(0L, budget - elapsed);
    }

    public void recordStrategyLatency(String strategyName, long latencyMs) {
        if (strategyName != null) {
            strategyLatencies.put(strategyName, latencyMs);
        }
    }

    public LatencyBudgetSummary createSummary(long startTimeMs, long customBudgetMs, boolean degraded) {
        long budget = customBudgetMs > 0 ? customBudgetMs : defaultBudgetMs;
        long totalElapsed = System.currentTimeMillis() - startTimeMs;
        return new LatencyBudgetSummary(
                budget,
                totalElapsed,
                getRemainingBudgetMs(startTimeMs, customBudgetMs),
                degraded,
                new ConcurrentHashMap<>(strategyLatencies)
        );
    }

    public record LatencyBudgetSummary(
            long allocatedBudgetMs,
            long usedBudgetMs,
            long remainingBudgetMs,
            boolean degradedModeActive,
            Map<String, Long> strategyLatenciesMs
    ) {}
}
