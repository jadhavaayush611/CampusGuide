package com.campusguide.personal.ai.atlas.knowledge.graph.projection;

import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;

import java.util.Set;

/**
 * Neighborhood projection strategy: slices graph in a k-hop radius surrounding root nodes.
 */
public class NeighborhoodProjection implements ProjectionStrategy {

    @Override
    public GraphProjection project(KnowledgeGraph graph, Set<NodeIdentifier> rootNodes, GraphProjectionPolicy policy) {
        return new GraphProjectionBuilder()
                .policy(policy != null ? policy : GraphProjectionPolicy.permissive())
                .addRootNodes(rootNodes)
                .buildFrom(graph);
    }
}
