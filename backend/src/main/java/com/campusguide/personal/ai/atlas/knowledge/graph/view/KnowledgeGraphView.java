package com.campusguide.personal.ai.atlas.knowledge.graph.view;

import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.RelationshipType;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.KnowledgeNode;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeType;
import com.campusguide.personal.ai.atlas.knowledge.graph.projection.GraphProjectionPolicy;

import java.util.List;

/**
 * Read-only view interface for Knowledge Graph instances.
 * The reasoning engine operates exclusively on KnowledgeGraphView rather than mutating full KnowledgeGraph instances directly.
 */
public interface KnowledgeGraphView {

    String getViewId();

    String getSourceGraphId();

    List<KnowledgeNode> getNodes();

    KnowledgeNode getNode(NodeIdentifier id);

    boolean containsNode(NodeIdentifier id);

    List<KnowledgeEdge> getEdges();

    KnowledgeEdge getEdge(String edgeId);

    List<KnowledgeEdge> getOutgoingEdges(NodeIdentifier nodeId);

    List<KnowledgeEdge> getIncomingEdges(NodeIdentifier nodeId);

    List<KnowledgeNode> getAdjacentNodes(NodeIdentifier nodeId);

    List<KnowledgeNode> getNodesByType(NodeType type);

    List<KnowledgeEdge> getEdgesByType(RelationshipType type);

    GraphProjectionPolicy getPolicy();

    int getNodeCount();

    int getEdgeCount();
}
