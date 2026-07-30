package com.campusguide.personal.ai.atlas.knowledge.graph.path;

import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.KnowledgeNode;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import com.campusguide.personal.ai.atlas.knowledge.graph.view.KnowledgeGraphView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Path discovery engine supporting shortest, strongest, highest-confidence, and collection-aware reasoning paths.
 */
@Component
@Slf4j
public class ReasoningPathFinder {

    public enum PathStrategy {
        SHORTEST,
        STRONGEST,
        HIGHEST_CONFIDENCE,
        COLLECTION_AWARE
    }

    public List<EvidencePath> findPaths(KnowledgeGraphView view,
                                        List<KnowledgeEdge> inferredEdges,
                                        NodeIdentifier startNodeId,
                                        NodeIdentifier endNodeId,
                                        PathStrategy strategy,
                                        int maxDepth,
                                        Set<String> activeCollections) {
        if (view == null || startNodeId == null || endNodeId == null) {
            return Collections.emptyList();
        }

        if (!view.containsNode(startNodeId) || !view.containsNode(endNodeId)) {
            return Collections.emptyList();
        }

        PathStrategy effectiveStrategy = strategy != null ? strategy : PathStrategy.SHORTEST;
        int depthLimit = maxDepth > 0 ? maxDepth : 4;

        // Combine view edges and virtual inferred edges
        Map<NodeIdentifier, List<KnowledgeEdge>> adjacency = buildAdjacency(view, inferredEdges);

        return switch (effectiveStrategy) {
            case SHORTEST -> findShortestPaths(view, adjacency, startNodeId, endNodeId, depthLimit, activeCollections);
            case STRONGEST -> findStrongestPaths(view, adjacency, startNodeId, endNodeId, depthLimit, activeCollections);
            case HIGHEST_CONFIDENCE -> findHighestConfidencePaths(view, adjacency, startNodeId, endNodeId, depthLimit, activeCollections);
            case COLLECTION_AWARE -> findCollectionAwarePaths(view, adjacency, startNodeId, endNodeId, depthLimit, activeCollections);
        };
    }

    private Map<NodeIdentifier, List<KnowledgeEdge>> buildAdjacency(KnowledgeGraphView view, List<KnowledgeEdge> inferredEdges) {
        Map<NodeIdentifier, List<KnowledgeEdge>> adj = new HashMap<>();
        for (KnowledgeNode n : view.getNodes()) {
            adj.put(n.getId(), new ArrayList<>(view.getOutgoingEdges(n.getId())));
        }
        if (inferredEdges != null) {
            for (KnowledgeEdge ie : inferredEdges) {
                adj.computeIfAbsent(ie.getSourceNodeId(), k -> new ArrayList<>()).add(ie);
                if (ie.isBidirectional()) {
                    adj.computeIfAbsent(ie.getTargetNodeId(), k -> new ArrayList<>()).add(ie);
                }
            }
        }
        return adj;
    }

    private List<EvidencePath> findShortestPaths(KnowledgeGraphView view,
                                                Map<NodeIdentifier, List<KnowledgeEdge>> adjacency,
                                                NodeIdentifier start,
                                                NodeIdentifier target,
                                                int maxDepth,
                                                Set<String> collections) {
        List<EvidencePath> paths = new ArrayList<>();
        Queue<List<NodeIdentifier>> queue = new LinkedList<>();
        Queue<List<KnowledgeEdge>> edgeQueue = new LinkedList<>();

        queue.add(List.of(start));
        edgeQueue.add(Collections.emptyList());

        Set<NodeIdentifier> visited = new HashSet<>();

        while (!queue.isEmpty()) {
            List<NodeIdentifier> currentPath = queue.poll();
            List<KnowledgeEdge> currentEdges = edgeQueue.poll();
            NodeIdentifier last = currentPath.get(currentPath.size() - 1);

            if (currentPath.size() - 1 > maxDepth) continue;

            if (last.equals(target)) {
                paths.add(buildEvidencePath(view, currentPath, currentEdges, "SHORTEST"));
                if (paths.size() >= 5) break; // Limit candidate paths
                continue;
            }

            visited.add(last);
            List<KnowledgeEdge> out = adjacency.getOrDefault(last, Collections.emptyList());

            for (KnowledgeEdge edge : out) {
                NodeIdentifier next = edge.getSourceNodeId().equals(last) ? edge.getTargetNodeId() : edge.getSourceNodeId();
                if (!visited.contains(next) && isCollectionValid(view, next, collections)) {
                    List<NodeIdentifier> nextPath = new ArrayList<>(currentPath);
                    nextPath.add(next);
                    List<KnowledgeEdge> nextEdges = new ArrayList<>(currentEdges);
                    nextEdges.add(edge);

                    queue.add(nextPath);
                    edgeQueue.add(nextEdges);
                }
            }
        }

        return paths;
    }

    private List<EvidencePath> findStrongestPaths(KnowledgeGraphView view,
                                                 Map<NodeIdentifier, List<KnowledgeEdge>> adjacency,
                                                 NodeIdentifier start,
                                                 NodeIdentifier target,
                                                 int maxDepth,
                                                 Set<String> collections) {
        List<EvidencePath> paths = findShortestPaths(view, adjacency, start, target, maxDepth, collections);
        paths.sort((p1, p2) -> Double.compare(p2.getCumulativeScore(), p1.getCumulativeScore()));
        return paths;
    }

    private List<EvidencePath> findHighestConfidencePaths(KnowledgeGraphView view,
                                                        Map<NodeIdentifier, List<KnowledgeEdge>> adjacency,
                                                        NodeIdentifier start,
                                                        NodeIdentifier target,
                                                        int maxDepth,
                                                        Set<String> collections) {
        List<EvidencePath> paths = findShortestPaths(view, adjacency, start, target, maxDepth, collections);
        paths.sort((p1, p2) -> Double.compare(p2.getConfidence(), p1.getConfidence()));
        return paths;
    }

    private List<EvidencePath> findCollectionAwarePaths(KnowledgeGraphView view,
                                                        Map<NodeIdentifier, List<KnowledgeEdge>> adjacency,
                                                        NodeIdentifier start,
                                                        NodeIdentifier target,
                                                        int maxDepth,
                                                        Set<String> collections) {
        return findShortestPaths(view, adjacency, start, target, maxDepth, collections);
    }

    private boolean isCollectionValid(KnowledgeGraphView view, NodeIdentifier nodeId, Set<String> collections) {
        if (collections == null || collections.isEmpty()) return true;
        KnowledgeNode node = view.getNode(nodeId);
        if (node == null || node.getSourceCollectionId() == null) return true;
        return collections.contains(node.getSourceCollectionId());
    }

    private EvidencePath buildEvidencePath(KnowledgeGraphView view, List<NodeIdentifier> nodeIds, List<KnowledgeEdge> edges, String type) {
        List<KnowledgeNode> nodes = new ArrayList<>();
        double scoreSum = 0.0;
        double confProduct = 1.0;

        for (NodeIdentifier nid : nodeIds) {
            KnowledgeNode n = view.getNode(nid);
            if (n != null) nodes.add(n);
        }

        for (KnowledgeEdge e : edges) {
            double edgeScore = e.getStrength() != null ? e.getStrength().getScore() : 0.5;
            double edgeConf = 0.8;
            if (e.getMetadata() != null && e.getMetadata().getProperty("confidenceScore") instanceof Number n) {
                edgeConf = n.doubleValue();
            }

            scoreSum += edgeScore;
            confProduct *= edgeConf;
        }

        int hops = edges.size();
        double depthFactor = Math.pow(0.95, hops);
        double finalConf = confProduct * depthFactor;

        return EvidencePath.builder()
                .nodes(nodes)
                .edges(edges)
                .cumulativeScore(edges.isEmpty() ? 1.0 : scoreSum / edges.size())
                .confidence(finalConf)
                .hopCount(hops)
                .pathType(type)
                .build();
    }
}
