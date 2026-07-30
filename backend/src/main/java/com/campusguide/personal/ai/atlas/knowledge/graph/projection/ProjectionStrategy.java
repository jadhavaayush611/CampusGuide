package com.campusguide.personal.ai.atlas.knowledge.graph.projection;

import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;

import java.util.Set;

/**
 * Interface for graph projection slicing strategies.
 */
public interface ProjectionStrategy {

    /**
     * Projects a KnowledgeGraph into a deterministic GraphProjection (KnowledgeGraphView).
     *
     * @param graph source knowledge graph
     * @param rootNodes seed/root nodes for slicing
     * @param policy projection policy constraints
     * @return projected view
     */
    GraphProjection project(KnowledgeGraph graph, Set<NodeIdentifier> rootNodes, GraphProjectionPolicy policy);
}
