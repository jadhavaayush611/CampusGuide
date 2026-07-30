package com.campusguide.personal.ai.atlas.knowledge.graph.traversal;

import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgePath;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeSubgraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;

import java.util.List;

/**
 * Engine interface for executing deterministic graph traversals across KnowledgeGraphs.
 */
public interface GraphTraversalEngine {

    /**
     * Traverses the KnowledgeGraph starting from startNodeId according to policy rules.
     */
    List<KnowledgePath> traverse(KnowledgeGraph graph, NodeIdentifier startNodeId, TraversalPolicy policy);

    /**
     * Extracts a k-hop neighborhood subgraph around rootNodeId according to policy depth.
     */
    KnowledgeSubgraph extractNeighborhood(KnowledgeGraph graph, NodeIdentifier rootNodeId, TraversalPolicy policy);
}
