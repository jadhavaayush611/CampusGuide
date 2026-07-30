package com.campusguide.personal.ai.atlas.knowledge.graph.projection;

import com.campusguide.personal.ai.atlas.knowledge.graph.metrics.GraphReasoningMetrics;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Projection Engine responsible for executing deterministic graph projections and recording latency metrics.
 */
@Component
@Slf4j
public class GraphProjectionEngine {

    public GraphProjection project(KnowledgeGraph graph, Set<NodeIdentifier> rootNodes, GraphProjectionPolicy policy) {
        return projectWithStrategy(graph, rootNodes, policy, new NeighborhoodProjection(), null);
    }

    public GraphProjection projectWithStrategy(KnowledgeGraph graph,
                                                Set<NodeIdentifier> rootNodes,
                                                GraphProjectionPolicy policy,
                                                ProjectionStrategy strategy,
                                                GraphReasoningMetrics metrics) {
        long startTime = System.nanoTime();

        if (graph == null) {
            log.warn("Cannot project null KnowledgeGraph");
            return new GraphProjectionBuilder().policy(policy).buildFrom((KnowledgeGraph) null);
        }

        ProjectionStrategy effectiveStrategy = strategy != null ? strategy : new NeighborhoodProjection();
        GraphProjectionPolicy effectivePolicy = policy != null ? policy : GraphProjectionPolicy.permissive();

        GraphProjection projection = effectiveStrategy.project(graph, rootNodes, effectivePolicy);

        long latencyNs = System.nanoTime() - startTime;
        long latencyMs = latencyNs / 1_000_000;

        if (metrics != null) {
            metrics.recordProjectionLatency(latencyMs);
            metrics.recordNodesProjected(projection.getNodeCount());
            metrics.recordEdgesProjected(projection.getEdgeCount());
        }

        log.debug("Graph projection computed viewId={} sourceGraphId={} projectedNodes={} projectedEdges={} latencyMs={}",
                projection.getViewId(), projection.getSourceGraphId(), projection.getNodeCount(), projection.getEdgeCount(), latencyMs);

        return projection;
    }
}
