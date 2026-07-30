package com.campusguide.personal.ai.atlas.knowledge.graph.model;

import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.RelationshipType;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.KnowledgeNode;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeType;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Provider-independent core Knowledge Graph representation in Atlas.
 * Encapsulates graph nodes, edges, adjacency indexes, and graph operations.
 */
@Data
public class KnowledgeGraph implements Serializable {

    private static final long serialVersionUID = 1L;

    private final KnowledgeGraphMetadata metadata;
    private final Map<NodeIdentifier, KnowledgeNode> nodes;
    private final Map<String, KnowledgeEdge> edges;
    private final Map<NodeIdentifier, Set<String>> outgoingEdges;
    private final Map<NodeIdentifier, Set<String>> incomingEdges;

    public KnowledgeGraph(KnowledgeGraphMetadata metadata) {
        this.metadata = metadata != null ? metadata : KnowledgeGraphMetadata.builder().graphId("default_graph").build();
        this.nodes = new ConcurrentHashMap<>();
        this.edges = new ConcurrentHashMap<>();
        this.outgoingEdges = new ConcurrentHashMap<>();
        this.incomingEdges = new ConcurrentHashMap<>();
    }

    public static KnowledgeGraph create(String graphId) {
        KnowledgeGraphMetadata meta = KnowledgeGraphMetadata.builder()
                .graphId(graphId)
                .name(graphId)
                .build();
        return new KnowledgeGraph(meta);
    }

    public static KnowledgeGraph create(String graphId, String name) {
        KnowledgeGraphMetadata meta = KnowledgeGraphMetadata.builder()
                .graphId(graphId)
                .name(name)
                .build();
        return new KnowledgeGraph(meta);
    }

    public void addNode(KnowledgeNode node) {
        if (node == null || node.getId() == null) {
            return;
        }
        nodes.put(node.getId(), node);
        outgoingEdges.putIfAbsent(node.getId(), ConcurrentHashMap.newKeySet());
        incomingEdges.putIfAbsent(node.getId(), ConcurrentHashMap.newKeySet());
        updateCounts();
    }

    public KnowledgeNode getNode(NodeIdentifier id) {
        return id != null ? nodes.get(id) : null;
    }

    public boolean containsNode(NodeIdentifier id) {
        return id != null && nodes.containsKey(id);
    }

    public KnowledgeNode removeNode(NodeIdentifier id) {
        if (id == null || !nodes.containsKey(id)) {
            return null;
        }
        KnowledgeNode removed = nodes.remove(id);

        // Remove connected outgoing edges
        Set<String> outgoing = outgoingEdges.remove(id);
        if (outgoing != null) {
            for (String edgeId : outgoing) {
                KnowledgeEdge edge = edges.remove(edgeId);
                if (edge != null) {
                    Set<String> inc = incomingEdges.get(edge.getTargetNodeId());
                    if (inc != null) inc.remove(edgeId);
                }
            }
        }

        // Remove connected incoming edges
        Set<String> incoming = incomingEdges.remove(id);
        if (incoming != null) {
            for (String edgeId : incoming) {
                KnowledgeEdge edge = edges.remove(edgeId);
                if (edge != null) {
                    Set<String> out = outgoingEdges.get(edge.getSourceNodeId());
                    if (out != null) out.remove(edgeId);
                }
            }
        }

        updateCounts();
        return removed;
    }

    public List<KnowledgeNode> getNodes() {
        return new ArrayList<>(nodes.values());
    }

    public List<KnowledgeNode> getNodesByType(NodeType type) {
        if (type == null) return Collections.emptyList();
        return nodes.values().stream()
                .filter(n -> n.getType() == type)
                .collect(Collectors.toList());
    }

    public void addEdge(KnowledgeEdge edge) {
        if (edge == null || edge.getId() == null) return;
        if (edge.getSourceNodeId() == null || edge.getTargetNodeId() == null) return;

        // Ensure nodes exist in index maps
        outgoingEdges.putIfAbsent(edge.getSourceNodeId(), ConcurrentHashMap.newKeySet());
        incomingEdges.putIfAbsent(edge.getSourceNodeId(), ConcurrentHashMap.newKeySet());
        outgoingEdges.putIfAbsent(edge.getTargetNodeId(), ConcurrentHashMap.newKeySet());
        incomingEdges.putIfAbsent(edge.getTargetNodeId(), ConcurrentHashMap.newKeySet());

        edges.put(edge.getId(), edge);
        outgoingEdges.get(edge.getSourceNodeId()).add(edge.getId());
        incomingEdges.get(edge.getTargetNodeId()).add(edge.getId());

        if (edge.isBidirectional()) {
            outgoingEdges.get(edge.getTargetNodeId()).add(edge.getId());
            incomingEdges.get(edge.getSourceNodeId()).add(edge.getId());
        }

        updateCounts();
    }

    public KnowledgeEdge getEdge(String edgeId) {
        return edgeId != null ? edges.get(edgeId) : null;
    }

    public KnowledgeEdge removeEdge(String edgeId) {
        if (edgeId == null || !edges.containsKey(edgeId)) {
            return null;
        }
        KnowledgeEdge removed = edges.remove(edgeId);
        if (removed != null) {
            Set<String> out = outgoingEdges.get(removed.getSourceNodeId());
            if (out != null) out.remove(edgeId);
            Set<String> inc = incomingEdges.get(removed.getTargetNodeId());
            if (inc != null) inc.remove(edgeId);

            if (removed.isBidirectional()) {
                Set<String> outRev = outgoingEdges.get(removed.getTargetNodeId());
                if (outRev != null) outRev.remove(edgeId);
                Set<String> incRev = incomingEdges.get(removed.getSourceNodeId());
                if (incRev != null) incRev.remove(edgeId);
            }
        }
        updateCounts();
        return removed;
    }

    public List<KnowledgeEdge> getEdges() {
        return new ArrayList<>(edges.values());
    }

    public List<KnowledgeEdge> getEdgesByType(RelationshipType type) {
        if (type == null) return Collections.emptyList();
        return edges.values().stream()
                .filter(e -> e.getRelationshipType() == type)
                .collect(Collectors.toList());
    }

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

    /**
     * Merges node attributes and metadata if node exists, or inserts new node.
     */
    public boolean mergeNode(KnowledgeNode newNode) {
        if (newNode == null || newNode.getId() == null) return false;
        KnowledgeNode existing = nodes.get(newNode.getId());
        if (existing == null) {
            addNode(newNode);
            return false; // Inserted new
        } else {
            if (newNode.getName() != null && !newNode.getName().isBlank()) {
                existing.setName(newNode.getName());
            }
            if (newNode.getAttributes() != null) {
                existing.updateAttributes(newNode.getAttributes());
            }
            if (newNode.getMetadata() != null) {
                newNode.getMetadata().forEach(existing::addMetadata);
            }
            if (newNode.getSourceCollectionId() != null) {
                existing.setSourceCollectionId(newNode.getSourceCollectionId());
            }
            if (newNode.getSourceArtifactId() != null) {
                existing.setSourceArtifactId(newNode.getSourceArtifactId());
            }
            existing.setUpdatedAt(java.time.Instant.now());
            return true; // Merged
        }
    }

    /**
     * Deduplicates edge: if identical edge exists (same source, target, type), updates strength & metadata.
     */
    public boolean deduplicateEdge(KnowledgeEdge edge) {
        if (edge == null || edge.getId() == null) return false;
        KnowledgeEdge existing = edges.get(edge.getId());
        if (existing == null) {
            addEdge(edge);
            return false; // Inserted new
        } else {
            // Combine strength
            existing.setStrength(existing.getStrength().combine(edge.getStrength()));
            if (edge.getMetadata() != null) {
                if (edge.getMetadata().getProperties() != null) {
                    edge.getMetadata().getProperties().forEach((k, v) -> existing.getMetadata().addProperty(k, v));
                }
            }
            existing.setUpdatedAt(java.time.Instant.now());
            return true; // Deduplicated / updated
        }
    }

    /**
     * Cleans dangling edges pointing to missing nodes.
     * Returns count of removed dangling edges.
     */
    public int validateConsistency() {
        List<String> dangling = new ArrayList<>();
        for (KnowledgeEdge edge : edges.values()) {
            boolean sourceExists = nodes.containsKey(edge.getSourceNodeId());
            boolean targetExists = nodes.containsKey(edge.getTargetNodeId());
            if (!sourceExists || !targetExists) {
                dangling.add(edge.getId());
            }
        }
        for (String edgeId : dangling) {
            removeEdge(edgeId);
        }
        return dangling.size();
    }

    public KnowledgeSubgraph toSubgraph() {
        return KnowledgeSubgraph.builder()
                .graphId(metadata.getGraphId())
                .nodes(new HashMap<>(nodes))
                .edges(new HashMap<>(edges))
                .build();
    }

    private void updateCounts() {
        if (metadata != null) {
            metadata.setNodeCount(nodes.size());
            metadata.setEdgeCount(edges.size());
            metadata.setUpdatedAt(java.time.Instant.now());
        }
    }
}
