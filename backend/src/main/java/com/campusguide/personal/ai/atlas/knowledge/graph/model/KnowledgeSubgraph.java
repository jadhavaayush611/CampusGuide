package com.campusguide.personal.ai.atlas.knowledge.graph.model;

import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.KnowledgeNode;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;

/**
 * Sub-graph projection containing a localized subset of KnowledgeNodes and KnowledgeEdges.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeSubgraph implements Serializable {

    private static final long serialVersionUID = 1L;

    private String graphId;

    private NodeIdentifier rootNodeId;

    @Builder.Default
    private Map<NodeIdentifier, KnowledgeNode> nodes = new HashMap<>();

    @Builder.Default
    private Map<String, KnowledgeEdge> edges = new HashMap<>();

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    public void addNode(KnowledgeNode node) {
        if (node != null && node.getId() != null) {
            if (this.nodes == null) this.nodes = new HashMap<>();
            this.nodes.put(node.getId(), node);
        }
    }

    public void addEdge(KnowledgeEdge edge) {
        if (edge != null && edge.getId() != null) {
            if (this.edges == null) this.edges = new HashMap<>();
            this.edges.put(edge.getId(), edge);
        }
    }

    public KnowledgeNode getNode(NodeIdentifier id) {
        return nodes != null ? nodes.get(id) : null;
    }

    public boolean containsNode(NodeIdentifier id) {
        return nodes != null && nodes.containsKey(id);
    }

    public Collection<KnowledgeNode> getNodes() {
        return nodes != null ? nodes.values() : Collections.emptyList();
    }

    public Collection<KnowledgeEdge> getEdges() {
        return edges != null ? edges.values() : Collections.emptyList();
    }

    public int getNodeCount() {
        return nodes != null ? nodes.size() : 0;
    }

    public int getEdgeCount() {
        return edges != null ? edges.size() : 0;
    }
}
