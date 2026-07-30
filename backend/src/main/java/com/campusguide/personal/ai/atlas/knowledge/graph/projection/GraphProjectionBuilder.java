package com.campusguide.personal.ai.atlas.knowledge.graph.projection;

import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.RelationshipType;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.KnowledgeNode;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeType;
import com.campusguide.personal.ai.atlas.knowledge.graph.view.KnowledgeGraphView;

import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;

/**
 * Builder for creating deterministic GraphProjection instances from KnowledgeGraph or KnowledgeGraphView.
 */
public class GraphProjectionBuilder {

    private String viewId;
    private String sourceGraphId;
    private GraphProjectionPolicy policy;
    private final Set<NodeIdentifier> rootNodes = new HashSet<>();
    private final Set<String> targetCollections = new HashSet<>();
    private final Set<String> userPermissions = new HashSet<>();
    private Predicate<KnowledgeNode> nodeFilter;
    private Predicate<KnowledgeEdge> edgeFilter;

    public GraphProjectionBuilder viewId(String viewId) {
        this.viewId = viewId;
        return this;
    }

    public GraphProjectionBuilder sourceGraphId(String sourceGraphId) {
        this.sourceGraphId = sourceGraphId;
        return this;
    }

    public GraphProjectionBuilder policy(GraphProjectionPolicy policy) {
        this.policy = policy;
        return this;
    }

    public GraphProjectionBuilder addRootNode(NodeIdentifier rootNode) {
        if (rootNode != null) this.rootNodes.add(rootNode);
        return this;
    }

    public GraphProjectionBuilder addRootNodes(Collection<NodeIdentifier> rootNodes) {
        if (rootNodes != null) this.rootNodes.addAll(rootNodes);
        return this;
    }

    public GraphProjectionBuilder addCollection(String collectionId) {
        if (collectionId != null) this.targetCollections.add(collectionId);
        return this;
    }

    public GraphProjectionBuilder addCollections(Collection<String> collections) {
        if (collections != null) this.targetCollections.addAll(collections);
        return this;
    }

    public GraphProjectionBuilder userPermissions(Set<String> permissions) {
        if (permissions != null) this.userPermissions.addAll(permissions);
        return this;
    }

    public GraphProjectionBuilder filterNodes(Predicate<KnowledgeNode> nodeFilter) {
        this.nodeFilter = nodeFilter;
        return this;
    }

    public GraphProjectionBuilder filterEdges(Predicate<KnowledgeEdge> edgeFilter) {
        this.edgeFilter = edgeFilter;
        return this;
    }

    public GraphProjection buildFrom(KnowledgeGraph graph) {
        if (graph == null) {
            return emptyProjection();
        }

        String graphId = sourceGraphId != null ? sourceGraphId : graph.getMetadata().getGraphId();
        GraphProjectionPolicy effectivePolicy = policy != null ? policy : GraphProjectionPolicy.permissive();

        if (!effectivePolicy.isPermissionSatisfied(userPermissions)) {
            return emptyProjection(graphId, effectivePolicy);
        }

        Map<NodeIdentifier, KnowledgeNode> projectedNodes = new HashMap<>();
        Map<String, KnowledgeEdge> projectedEdges = new HashMap<>();
        Map<NodeIdentifier, Set<String>> outgoing = new HashMap<>();
        Map<NodeIdentifier, Set<String>> incoming = new HashMap<>();

        // If root nodes provided, perform k-hop slice up to policy.maxDepth
        if (!rootNodes.isEmpty()) {
            Set<NodeIdentifier> currentLevel = new HashSet<>(rootNodes);
            Set<NodeIdentifier> visited = new HashSet<>();

            for (int depth = 0; depth <= effectivePolicy.getMaxDepth() && !currentLevel.isEmpty(); depth++) {
                Set<NodeIdentifier> nextLevel = new HashSet<>();

                for (NodeIdentifier nid : currentLevel) {
                    if (visited.contains(nid)) continue;
                    visited.add(nid);

                    KnowledgeNode node = graph.getNode(nid);
                    if (node == null || !isNodeAllowed(node, effectivePolicy)) continue;

                    projectedNodes.put(nid, node);
                    outgoing.putIfAbsent(nid, new HashSet<>());
                    incoming.putIfAbsent(nid, new HashSet<>());

                    // Process outgoing edges if under maxDepth limit
                    if (depth < effectivePolicy.getMaxDepth()) {
                        for (KnowledgeEdge edge : graph.getOutgoingEdges(nid)) {
                            if (isEdgeAllowed(edge, effectivePolicy)) {
                                NodeIdentifier target = edge.getTargetNodeId();
                                KnowledgeNode targetNode = graph.getNode(target);

                                if (targetNode != null && isNodeAllowed(targetNode, effectivePolicy)) {
                                    projectedEdges.put(edge.getId(), edge);
                                    outgoing.get(nid).add(edge.getId());

                                    incoming.putIfAbsent(target, new HashSet<>());
                                    incoming.get(target).add(edge.getId());

                                    projectedNodes.put(target, targetNode);
                                    nextLevel.add(target);
                                }
                            }
                        }

                        // Process incoming edges if policy allows
                        if (effectivePolicy.isIncludeBidirectional()) {
                            for (KnowledgeEdge edge : graph.getIncomingEdges(nid)) {
                                if (isEdgeAllowed(edge, effectivePolicy)) {
                                    NodeIdentifier source = edge.getSourceNodeId();
                                    KnowledgeNode sourceNode = graph.getNode(source);

                                    if (sourceNode != null && isNodeAllowed(sourceNode, effectivePolicy)) {
                                        projectedEdges.put(edge.getId(), edge);
                                        incoming.get(nid).add(edge.getId());

                                        outgoing.putIfAbsent(source, new HashSet<>());
                                        outgoing.get(source).add(edge.getId());

                                        projectedNodes.put(source, sourceNode);
                                        nextLevel.add(source);
                                    }
                                }
                            }
                        }
                    }
                }
                currentLevel = nextLevel;
            }
        } else {
            // Full graph slice filtering
            for (KnowledgeNode node : graph.getNodes()) {
                if (isNodeAllowed(node, effectivePolicy)) {
                    projectedNodes.put(node.getId(), node);
                    outgoing.putIfAbsent(node.getId(), new HashSet<>());
                    incoming.putIfAbsent(node.getId(), new HashSet<>());
                }
            }

            for (KnowledgeEdge edge : graph.getEdges()) {
                if (isEdgeAllowed(edge, effectivePolicy)
                        && projectedNodes.containsKey(edge.getSourceNodeId())
                        && projectedNodes.containsKey(edge.getTargetNodeId())) {

                    projectedEdges.put(edge.getId(), edge);
                    outgoing.get(edge.getSourceNodeId()).add(edge.getId());
                    incoming.get(edge.getTargetNodeId()).add(edge.getId());
                }
            }
        }

        return GraphProjection.builder()
                .viewId(viewId != null ? viewId : "proj_" + UUID.randomUUID().toString().substring(0, 8))
                .sourceGraphId(graphId)
                .policy(effectivePolicy)
                .nodes(projectedNodes)
                .edges(projectedEdges)
                .outgoingEdges(outgoing)
                .incomingEdges(incoming)
                .createdAt(Instant.now())
                .build();
    }

    public GraphProjection buildFrom(KnowledgeGraphView view) {
        if (view == null) return emptyProjection();
        Map<NodeIdentifier, KnowledgeNode> nodeMap = new HashMap<>();
        for (KnowledgeNode n : view.getNodes()) {
            nodeMap.put(n.getId(), n);
        }
        Map<String, KnowledgeEdge> edgeMap = new HashMap<>();
        for (KnowledgeEdge e : view.getEdges()) {
            edgeMap.put(e.getId(), e);
        }
        Map<NodeIdentifier, Set<String>> out = new HashMap<>();
        Map<NodeIdentifier, Set<String>> inc = new HashMap<>();

        for (KnowledgeNode n : view.getNodes()) {
            out.put(n.getId(), view.getOutgoingEdges(n.getId()).stream().map(KnowledgeEdge::getId).collect(HashSet::new, HashSet::add, HashSet::addAll));
            inc.put(n.getId(), view.getIncomingEdges(n.getId()).stream().map(KnowledgeEdge::getId).collect(HashSet::new, HashSet::add, HashSet::addAll));
        }

        return GraphProjection.builder()
                .viewId(viewId != null ? viewId : "proj_" + UUID.randomUUID().toString().substring(0, 8))
                .sourceGraphId(view.getSourceGraphId())
                .policy(policy != null ? policy : view.getPolicy())
                .nodes(nodeMap)
                .edges(edgeMap)
                .outgoingEdges(out)
                .incomingEdges(inc)
                .createdAt(Instant.now())
                .build();
    }

    private boolean isNodeAllowed(KnowledgeNode node, GraphProjectionPolicy policy) {
        if (node == null) return false;
        if (!targetCollections.isEmpty() && node.getSourceCollectionId() != null && !targetCollections.contains(node.getSourceCollectionId())) {
            return false;
        }
        if (!policy.isCollectionAllowed(node.getSourceCollectionId())) {
            return false;
        }
        if (!policy.isNodeTypeAllowed(node.getType())) {
            return false;
        }
        if (nodeFilter != null && !nodeFilter.test(node)) {
            return false;
        }
        return true;
    }

    private boolean isEdgeAllowed(KnowledgeEdge edge, GraphProjectionPolicy policy) {
        if (edge == null) return false;
        if (edge.getStrength() != null && edge.getStrength().getScore() < policy.getMinEdgeStrength()) {
            return false;
        }
        if (!policy.isRelationshipTypeAllowed(edge.getRelationshipType())) {
            return false;
        }
        if (edgeFilter != null && !edgeFilter.test(edge)) {
            return false;
        }
        return true;
    }

    private GraphProjection emptyProjection() {
        return emptyProjection("empty_graph", GraphProjectionPolicy.permissive());
    }

    private GraphProjection emptyProjection(String graphId, GraphProjectionPolicy policy) {
        return GraphProjection.builder()
                .viewId(viewId != null ? viewId : "proj_empty")
                .sourceGraphId(graphId)
                .policy(policy)
                .nodes(Collections.emptyMap())
                .edges(Collections.emptyMap())
                .outgoingEdges(Collections.emptyMap())
                .incomingEdges(Collections.emptyMap())
                .createdAt(Instant.now())
                .build();
    }
}
