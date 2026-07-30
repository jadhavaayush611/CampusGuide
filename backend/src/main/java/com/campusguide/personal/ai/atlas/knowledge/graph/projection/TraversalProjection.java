package com.campusguide.personal.ai.atlas.knowledge.graph.projection;

import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.KnowledgeNode;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;

import java.util.Set;
import java.util.function.Predicate;

/**
 * Traversal projection strategy: slices graph using custom node/edge traversal predicates.
 */
public class TraversalProjection implements ProjectionStrategy {

    private final Predicate<KnowledgeNode> nodePredicate;
    private final Predicate<KnowledgeEdge> edgePredicate;

    public TraversalProjection(Predicate<KnowledgeNode> nodePredicate, Predicate<KnowledgeEdge> edgePredicate) {
        this.nodePredicate = nodePredicate;
        this.edgePredicate = edgePredicate;
    }

    @Override
    public GraphProjection project(KnowledgeGraph graph, Set<NodeIdentifier> rootNodes, GraphProjectionPolicy policy) {
        return new GraphProjectionBuilder()
                .policy(policy != null ? policy : GraphProjectionPolicy.permissive())
                .addRootNodes(rootNodes)
                .filterNodes(nodePredicate)
                .filterEdges(edgePredicate)
                .buildFrom(graph);
    }
}
