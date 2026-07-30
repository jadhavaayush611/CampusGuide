package com.campusguide.personal.ai.atlas.knowledge.graph.traversal;

import com.campusguide.personal.ai.atlas.knowledge.graph.metrics.KnowledgeGraphMetrics;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgePath;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeSubgraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default implementation of GraphTraversalEngine combining BFS, DFS, and Neighborhood strategies with metrics instrumentation.
 */
@Service
public class DefaultGraphTraversalEngine implements GraphTraversalEngine {

    private final BreadthFirstTraversal bfsTraversal;
    private final DepthFirstTraversal dfsTraversal;
    private final NeighborhoodTraversal neighborhoodTraversal;
    private final KnowledgeGraphMetrics metrics;

    public DefaultGraphTraversalEngine(BreadthFirstTraversal bfsTraversal,
                                        DepthFirstTraversal dfsTraversal,
                                        NeighborhoodTraversal neighborhoodTraversal,
                                        KnowledgeGraphMetrics metrics) {
        this.bfsTraversal = bfsTraversal;
        this.dfsTraversal = dfsTraversal;
        this.neighborhoodTraversal = neighborhoodTraversal;
        this.metrics = metrics;
    }

    @Override
    public List<KnowledgePath> traverse(KnowledgeGraph graph, NodeIdentifier startNodeId, TraversalPolicy policy) {
        long startTime = System.currentTimeMillis();
        List<KnowledgePath> result = bfsTraversal.traverse(graph, startNodeId, policy);
        long duration = System.currentTimeMillis() - startTime;

        if (metrics != null) {
            metrics.recordTraversalLatency(duration, "bfs", result != null ? result.size() : 0);
        }
        return result;
    }

    public List<KnowledgePath> traverseDfs(KnowledgeGraph graph, NodeIdentifier startNodeId, TraversalPolicy policy) {
        long startTime = System.currentTimeMillis();
        List<KnowledgePath> result = dfsTraversal.traverse(graph, startNodeId, policy);
        long duration = System.currentTimeMillis() - startTime;

        if (metrics != null) {
            metrics.recordTraversalLatency(duration, "dfs", result != null ? result.size() : 0);
        }
        return result;
    }

    @Override
    public KnowledgeSubgraph extractNeighborhood(KnowledgeGraph graph, NodeIdentifier rootNodeId, TraversalPolicy policy) {
        long startTime = System.currentTimeMillis();
        KnowledgeSubgraph subgraph = neighborhoodTraversal.extractNeighborhood(graph, rootNodeId, policy);
        long duration = System.currentTimeMillis() - startTime;

        if (metrics != null) {
            metrics.recordTraversalLatency(duration, "neighborhood", subgraph != null ? subgraph.getNodeCount() : 0);
        }
        return subgraph;
    }
}
