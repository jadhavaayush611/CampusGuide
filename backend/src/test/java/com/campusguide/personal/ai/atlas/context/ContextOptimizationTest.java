package com.campusguide.personal.ai.atlas.context;

import com.campusguide.personal.ai.atlas.context.evidence.EvidenceBundle;
import com.campusguide.personal.ai.atlas.context.evidence.RetrievalEvidence;
import com.campusguide.personal.ai.atlas.context.optimization.ContextCache;
import com.campusguide.personal.ai.atlas.context.optimization.LatencyBudgetManager;
import com.campusguide.personal.ai.atlas.context.optimization.RetrievalCachePolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ContextOptimizationTest {

    @Test
    @DisplayName("ContextCache stores, hits, invalidates, and tracks metrics correctly")
    void testContextCache_OperationsAndMetrics() {
        RetrievalCachePolicy policy = RetrievalCachePolicy.builder()
                .enabled(true)
                .defaultTtlMs(5000L)
                .maxEntries(100)
                .build();
        ContextCache cache = new ContextCache(policy);

        EvidenceBundle bundle = EvidenceBundle.builder()
                .targetDomain("campus")
                .evidences(List.of(RetrievalEvidence.builder().contentSnippet("CSH hall").build()))
                .build();

        cache.put("key-1", bundle);

        Optional<EvidenceBundle> cached = cache.get("key-1");
        assertTrue(cached.isPresent());
        assertEquals("campus", cached.get().getTargetDomain());

        // Cache miss
        Optional<EvidenceBundle> miss = cache.get("non-existent");
        assertFalse(miss.isPresent());

        ContextCache.CacheDiagnostics diag = cache.getDiagnostics();
        assertEquals(1, diag.hits());
        assertEquals(1, diag.misses());
        assertEquals(0.5, diag.hitRatio(), 0.01);

        // Invalidation
        cache.invalidateDomain("campus");
        assertFalse(cache.get("key-1").isPresent());
    }

    @Test
    @DisplayName("LatencyBudgetManager tracks latency budget allocation and remaining time")
    void testLatencyBudgetManager() {
        LatencyBudgetManager budgetManager = new LatencyBudgetManager(200L);
        long start = System.currentTimeMillis();

        assertTrue(budgetManager.hasRemainingBudget(start, 200L));
        assertTrue(budgetManager.getRemainingBudgetMs(start, 200L) <= 200L);

        budgetManager.recordStrategyLatency("user", 25L);
        LatencyBudgetManager.LatencyBudgetSummary summary = budgetManager.createSummary(start, 200L, false);

        assertNotNull(summary);
        assertEquals(200L, summary.allocatedBudgetMs());
        assertFalse(summary.degradedModeActive());
        assertTrue(summary.strategyLatenciesMs().containsKey("user"));
    }
}
