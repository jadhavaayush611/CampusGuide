package com.campusguide.personal.ai.atlas.knowledge.graph.projection;

import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;

import java.util.Set;

/**
 * Collection projection strategy: slices graph filtered strictly by active collections.
 */
public class CollectionProjection implements ProjectionStrategy {

    private final Set<String> targetCollections;

    public CollectionProjection(Set<String> targetCollections) {
        this.targetCollections = targetCollections;
    }

    @Override
    public GraphProjection project(KnowledgeGraph graph, Set<NodeIdentifier> rootNodes, GraphProjectionPolicy policy) {
        GraphProjectionPolicy effectivePolicy = policy != null ? policy : GraphProjectionPolicy.permissive();
        if (targetCollections != null && !targetCollections.isEmpty()) {
            effectivePolicy.setAllowedCollections(targetCollections);
        }
        return new GraphProjectionBuilder()
                .policy(effectivePolicy)
                .addCollections(targetCollections)
                .buildFrom(graph);
    }
}
