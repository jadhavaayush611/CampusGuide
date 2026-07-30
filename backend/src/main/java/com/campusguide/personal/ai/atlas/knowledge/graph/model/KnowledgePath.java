package com.campusguide.personal.ai.atlas.knowledge.graph.model;

import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.KnowledgeNode;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Ordered path of KnowledgeNodes and KnowledgeEdges resulting from a graph traversal.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgePath implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private List<KnowledgeNode> nodes = new ArrayList<>();

    @Builder.Default
    private List<KnowledgeEdge> edges = new ArrayList<>();

    @Builder.Default
    private double totalStrength = 1.0;

    public static KnowledgePath empty() {
        return new KnowledgePath(new ArrayList<>(), new ArrayList<>(), 0.0);
    }

    public static KnowledgePath singleNode(KnowledgeNode node) {
        List<KnowledgeNode> nodeList = new ArrayList<>();
        if (node != null) {
            nodeList.add(node);
        }
        return new KnowledgePath(nodeList, new ArrayList<>(), 1.0);
    }

    public void addHop(KnowledgeNode node, KnowledgeEdge edge) {
        if (this.nodes == null) this.nodes = new ArrayList<>();
        if (this.edges == null) this.edges = new ArrayList<>();

        if (node != null) {
            this.nodes.add(node);
        }
        if (edge != null) {
            this.edges.add(edge);
            this.totalStrength *= edge.getStrength().getScore();
        }
    }

    public KnowledgeNode getStartNode() {
        return (nodes != null && !nodes.isEmpty()) ? nodes.get(0) : null;
    }

    public KnowledgeNode getEndNode() {
        return (nodes != null && !nodes.isEmpty()) ? nodes.get(nodes.size() - 1) : null;
    }

    public int getHopCount() {
        return edges != null ? edges.size() : 0;
    }

    public int getNodeCount() {
        return nodes != null ? nodes.size() : 0;
    }

    public boolean isEmpty() {
        return nodes == null || nodes.isEmpty();
    }

    public boolean containsNode(NodeIdentifier id) {
        if (nodes == null || id == null) return false;
        return nodes.stream().anyMatch(n -> n.getId().equals(id));
    }

    public List<KnowledgeNode> getNodes() {
        return nodes != null ? Collections.unmodifiableList(nodes) : Collections.emptyList();
    }

    public List<KnowledgeEdge> getEdges() {
        return edges != null ? Collections.unmodifiableList(edges) : Collections.emptyList();
    }
}
