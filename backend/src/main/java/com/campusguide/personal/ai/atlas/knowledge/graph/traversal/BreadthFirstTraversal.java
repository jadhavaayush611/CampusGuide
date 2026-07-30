package com.campusguide.personal.ai.atlas.knowledge.graph.traversal;

import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgePath;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.KnowledgeNode;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Deterministic Breadth-First Traversal (BFS) implementation.
 */
@Component
public class BreadthFirstTraversal {

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
        Queue<KnowledgePath> queue = new LinkedList<>();

        KnowledgePath rootPath = KnowledgePath.singleNode(startNode);
        queue.add(rootPath);
        resultPaths.add(rootPath);

        while (!queue.isEmpty() && resultPaths.size() < p.getMaxPaths()) {
            KnowledgePath currentPath = queue.poll();
            if (currentPath.getHopCount() >= p.getMaxDepth()) {
                continue;
            }

            KnowledgeNode lastNode = currentPath.getEndNode();
            if (lastNode == null) continue;

            List<KnowledgeEdge> candidateEdges = getCandidateEdges(graph, lastNode.getId(), p);

            // Deterministic sorting of candidate edges by ID
            candidateEdges.sort(Comparator.comparing(KnowledgeEdge::getId));

            for (KnowledgeEdge edge : candidateEdges) {
                if (edge.getStrength().getScore() < p.getMinStrength()) continue;
                if (!p.isRelationshipAllowed(edge.getRelationshipType())) continue;

                NodeIdentifier nextNodeId = getNextNodeId(edge, lastNode.getId());
                KnowledgeNode nextNode = graph.getNode(nextNodeId);

                if (nextNode == null || !p.isNodeTypeAllowed(nextNode.getType())) continue;

                // Cycle detection
                if (p.isDetectCycles() && currentPath.containsNode(nextNodeId)) {
                    continue;
                }

                KnowledgePath newPath = new KnowledgePath(
                        new ArrayList<>(currentPath.getNodes()),
                        new ArrayList<>(currentPath.getEdges()),
                        currentPath.getTotalStrength()
                );
                newPath.addHop(nextNode, edge);

                resultPaths.add(newPath);
                queue.add(newPath);

                if (resultPaths.size() >= p.getMaxPaths()) {
                    break;
                }
            }
        }

        return resultPaths;
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
