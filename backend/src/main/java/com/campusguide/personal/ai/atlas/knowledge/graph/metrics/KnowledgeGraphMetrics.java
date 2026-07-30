package com.campusguide.personal.ai.atlas.knowledge.graph.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Metrics instrumentation component for Knowledge Graph operations.
 * Emits Micrometer metrics for graph build latency, node/edge counts, traversal performance, and rebuild stats.
 * Guarantees zero sensitive or raw node content logging.
 */
@Component
public class KnowledgeGraphMetrics {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeGraphMetrics.class);

    private final MeterRegistry registry;

    public KnowledgeGraphMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordGraphBuild(long durationMs, int nodeCount, int edgeCount) {
        Timer.builder("atlas.graph.build.latency")
                .description("Time taken to construct a Knowledge Graph")
                .register(registry)
                .record(durationMs, TimeUnit.MILLISECONDS);

        Counter.builder("atlas.graph.nodes.total")
                .description("Total nodes processed in Knowledge Graphs")
                .register(registry)
                .increment(nodeCount);

        Counter.builder("atlas.graph.edges.total")
                .description("Total edges processed in Knowledge Graphs")
                .register(registry)
                .increment(edgeCount);

        log.debug("Recorded graph build metrics. durationMs: {}, nodes: {}, edges: {}", durationMs, nodeCount, edgeCount);
    }

    public void recordTraversalLatency(long durationMs, String strategy, int resultCount) {
        Timer.builder("atlas.graph.traversal.latency")
                .tag("strategy", strategy != null ? strategy : "unknown")
                .description("Time taken to traverse a Knowledge Graph")
                .register(registry)
                .record(durationMs, TimeUnit.MILLISECONDS);

        Counter.builder("atlas.graph.traversal.results")
                .tag("strategy", strategy != null ? strategy : "unknown")
                .register(registry)
                .increment(resultCount);

        log.debug("Recorded traversal metrics. strategy: {}, durationMs: {}, results: {}", strategy, durationMs, resultCount);
    }

    public void recordGraphRebuild(String graphId) {
        Counter.builder("atlas.graph.rebuild.count")
                .tag("graphId", graphId != null ? graphId : "unknown")
                .register(registry)
                .increment();
    }

    public void recordRelationshipExtracted(String relationshipType) {
        Counter.builder("atlas.graph.relationships.extracted")
                .tag("type", relationshipType != null ? relationshipType : "UNKNOWN")
                .register(registry)
                .increment();
    }
}
