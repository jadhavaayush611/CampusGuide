package com.campusguide.personal.ai.atlas.knowledge.graph.traversal;

import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgePath;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.KnowledgeNode;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Deterministic Depth-First Traversal (DFS) implementation.
 */
@Component
public class DepthFirstTraversal {

    public List<KnowledgePath> traverse(KnowledgeGraph graph, NodeIdentifier startNodeId, TraversalPolicy policy) {
        if (graph == null || startNodeId == null || !graph.containsNode(startNodeId)) {
            return Collections.emptyList();
        }

        TraversalPolicy p = policy != null ? policy : TraversalPolicy.defaultPolicy();
        KnowledgeNode startNode = graph.getNode(startNodeId);
        if (!p.isNodeTypeAllowed(startNode.getType())) {
            return Collections.emptyList();
        }

        List<KnowledgePath> resultPaths = new ArrayList<>();
        KnowledgePath currentPath = KnowledgePath.singleNode(startNode);
        resultPaths.add(currentPath);

        dfsRecursive(graph, currentPath, p, resultPaths);
        return resultPaths;
    }

    private void dfsRecursive(KnowledgeGraph graph, KnowledgePath currentPath, TraversalPolicy policy, List<KnowledgePath> resultPaths) {
        if (resultPaths.size() >= policy.getMaxPaths() || currentPath.getHopCount() >= policy.getMaxDepth()) {
            return;
        }

        KnowledgeNode lastNode = currentPath.getEndNode();
        if (lastNode == null) return;

        List<KnowledgeEdge> candidateEdges = getCandidateEdges(graph, lastNode.getId(), policy);
        candidateEdges.sort(Comparator.comparing(KnowledgeEdge::getId));

        for (KnowledgeEdge edge : candidateEdges) {
            if (resultPaths.size() >= policy.getMaxPaths()) break;
            if (edge.getStrength().getScore() < policy.getMinStrength()) continue;
            if (!policy.isRelationshipAllowed(edge.getRelationshipType())) continue;

            NodeIdentifier nextNodeId = getNextNodeId(edge, lastNode.getId());
            KnowledgeNode nextNode = graph.getNode(nextNodeId);

            if (nextNode == null || !policy.isNodeTypeAllowed(nextNode.getType())) continue;

            if (policy.isDetectCycles() && currentPath.containsNode(nextNodeId)) {
                continue;
            }

            KnowledgePath newPath = new KnowledgePath(
                    new ArrayList<>(currentPath.getNodes()),
                    new ArrayList<>(currentPath.getEdges()),
                    currentPath.getTotalStrength()
            );
            newPath.addHop(nextNode, edge);

            resultPaths.add(newPath);
            dfsRecursive(graph, newPath, policy, resultPaths);
        }
    }

    private List<KnowledgeEdge> getCandidateEdges(KnowledgeGraph graph, NodeIdentifier nodeId, TraversalPolicy policy) {
        List<KnowledgeEdge> edges = new ArrayList<>();
        if (policy.getDirection() == TraversalPolicy.Direction.OUTGOING || policy.getDirection() == TraversalPolicy.Direction.BOTH) {
            edges.addAll(graph.getOutgoingEdges(nodeId));
        }
        if (policy.getDirection() == TraversalPolicy.Direction.INCOMING || policy.getDirection() == TraversalPolicy.Direction.BOTH) {
            edges.addAll(graph.getIncomingEdges(nodeId));
        }
        return edges;
    }

    private NodeIdentifier getNextNodeId(KnowledgeEdge edge, NodeIdentifier currentNodeId) {
        if (edge.getSourceNodeId().equals(currentNodeId)) {
            return edge.getTargetNodeId();
        } else {
            return edge.getSourceNodeId();
        }
    }
}
