package com.campusguide.personal.ai.atlas.knowledge.graph.path;

import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.KnowledgeNode;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an ordered evidence path of nodes and edges connecting reasoning sources to targets.
 */
@Data
@Builder
public class EvidencePath implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private final List<KnowledgeNode> nodes = Collections.emptyList();

    @Builder.Default
    private final List<KnowledgeEdge> edges = Collections.emptyList();

    private final double cumulativeScore;
    private final double confidence;
    private final int hopCount;
    private final String pathType;

    public String getSourceNodeId() {
        return (nodes != null && !nodes.isEmpty()) ? nodes.get(0).getId().getValue() : null;
    }

    public String getTargetNodeId() {
        return (nodes != null && !nodes.isEmpty()) ? nodes.get(nodes.size() - 1).getId().getValue() : null;
    }

    public List<String> getCitedEdgeIds() {
        if (edges == null) return Collections.emptyList();
        List<String> ids = new ArrayList<>();
        for (KnowledgeEdge e : edges) {
            if (e != null && e.getId() != null) ids.add(e.getId());
        }
        return ids;
    }
}
