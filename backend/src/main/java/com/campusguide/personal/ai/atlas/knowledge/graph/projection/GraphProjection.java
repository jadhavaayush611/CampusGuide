package com.campusguide.personal.ai.atlas.knowledge.graph.projection;

import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.RelationshipType;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.KnowledgeNode;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeType;
import com.campusguide.personal.ai.atlas.knowledge.graph.view.KnowledgeGraphView;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Immutable/Read-only KnowledgeGraphView implementation representing a deterministic slice/projection of a Knowledge Graph.
 */
@Getter
@Builder
public class GraphProjection implements KnowledgeGraphView, Serializable {

    private static final long serialVersionUID = 1L;

    private final String viewId;
    private final String sourceGraphId;
    private final GraphProjectionPolicy policy;
    private final Map<NodeIdentifier, KnowledgeNode> nodes;
    private final Map<String, KnowledgeEdge> edges;
    private final Map<NodeIdentifier, Set<String>> outgoingEdges;
    private final Map<NodeIdentifier, Set<String>> incomingEdges;
    private final Instant createdAt;

    public GraphProjection(String viewId,
                           String sourceGraphId,
                           GraphProjectionPolicy policy,
                           Map<NodeIdentifier, KnowledgeNode> nodes,
                           Map<String, KnowledgeEdge> edges,
                           Map<NodeIdentifier, Set<String>> outgoingEdges,
                           Map<NodeIdentifier, Set<String>> incomingEdges,
                           Instant createdAt) {
        this.viewId = viewId != null ? viewId : "proj_" + UUID.randomUUID().toString().substring(0, 8);
        this.sourceGraphId = sourceGraphId != null ? sourceGraphId : "default_graph";
        this.policy = policy != null ? policy : GraphProjectionPolicy.permissive();
        this.nodes = nodes != null ? Collections.unmodifiableMap(new HashMap<>(nodes)) : Collections.emptyMap();
        this.edges = edges != null ? Collections.unmodifiableMap(new HashMap<>(edges)) : Collections.emptyMap();
        this.outgoingEdges = outgoingEdges != null ? unmodifiableAdjacency(outgoingEdges) : Collections.emptyMap();
        this.incomingEdges = incomingEdges != null ? unmodifiableAdjacency(incomingEdges) : Collections.emptyMap();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    private static Map<NodeIdentifier, Set<String>> unmodifiableAdjacency(Map<NodeIdentifier, Set<String>> source) {
        Map<NodeIdentifier, Set<String>> copy = new HashMap<>();
        source.forEach((k, v) -> copy.put(k, Collections.unmodifiableSet(new HashSet<>(v))));
        return Collections.unmodifiableMap(copy);
    }

    @Override
    public List<KnowledgeNode> getNodes() {
        return new ArrayList<>(nodes.values());
    }

    @Override
    public KnowledgeNode getNode(NodeIdentifier id) {
        return id != null ? nodes.get(id) : null;
    }

    @Override
    public boolean containsNode(NodeIdentifier id) {
        return id != null && nodes.containsKey(id);
    }

    @Override
    public List<KnowledgeEdge> getEdges() {
        return new ArrayList<>(edges.values());
    }

    @Override
    public KnowledgeEdge getEdge(String edgeId) {
        return edgeId != null ? edges.get(edgeId) : null;
    }

    @Override
    public List<KnowledgeEdge> getOutgoingEdges(NodeIdentifier nodeId) {
        Set<String> edgeIds = outgoingEdges.get(nodeId);
        if (edgeIds == null || edgeIds.isEmpty()) return Collections.emptyList();
        List<KnowledgeEdge> result = new ArrayList<>();
        for (String id : edgeIds) {
            KnowledgeEdge e = edges.get(id);
            if (e != null) result.add(e);
        }
        return result;
    }

    @Override
    public List<KnowledgeEdge> getIncomingEdges(NodeIdentifier nodeId) {
        Set<String> edgeIds = incomingEdges.get(nodeId);
        if (edgeIds == null || edgeIds.isEmpty()) return Collections.emptyList();
        List<KnowledgeEdge> result = new ArrayList<>();
        for (String id : edgeIds) {
            KnowledgeEdge e = edges.get(id);
            if (e != null) result.add(e);
        }
        return result;
    }

    @Override
    public List<KnowledgeNode> getAdjacentNodes(NodeIdentifier nodeId) {
        Set<NodeIdentifier> adjacentIds = new HashSet<>();
        for (KnowledgeEdge out : getOutgoingEdges(nodeId)) {
            if (out.getSourceNodeId().equals(nodeId)) {
                adjacentIds.add(out.getTargetNodeId());
            } else {
                adjacentIds.add(out.getSourceNodeId());
            }
        }
        for (KnowledgeEdge inc : getIncomingEdges(nodeId)) {
            if (inc.getTargetNodeId().equals(nodeId)) {
                adjacentIds.add(inc.getSourceNodeId());
            } else {
                adjacentIds.add(inc.getTargetNodeId());
            }
        }
        List<KnowledgeNode> result = new ArrayList<>();
        for (NodeIdentifier adjId : adjacentIds) {
            KnowledgeNode n = getNode(adjId);
            if (n != null) result.add(n);
        }
        return result;
    }

    @Override
    public List<KnowledgeNode> getNodesByType(NodeType type) {
        if (type == null) return Collections.emptyList();
        return nodes.values().stream()
                .filter(n -> n.getType() == type)
                .collect(Collectors.toList());
    }

    @Override
    public List<KnowledgeEdge> getEdgesByType(RelationshipType type) {
        if (type == null) return Collections.emptyList();
        return edges.values().stream()
                .filter(e -> e.getRelationshipType() == type)
                .collect(Collectors.toList());
    }

    @Override
    public int getNodeCount() {
        return nodes.size();
    }

    @Override
    public int getEdgeCount() {
        return edges.size();
    }
}
