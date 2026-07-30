package com.campusguide.personal.ai.atlas.knowledge.graph.traversal;

import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgePath;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeSubgraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.KnowledgeNode;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Neighborhood / k-hop traversal extracting localized subgraphs centered around a root node.
 */
@Component
public class NeighborhoodTraversal {

    private final BreadthFirstTraversal bfsTraversal;

    public NeighborhoodTraversal(BreadthFirstTraversal bfsTraversal) {
        this.bfsTraversal = bfsTraversal;
    }

    public KnowledgeSubgraph extractNeighborhood(KnowledgeGraph graph, NodeIdentifier rootNodeId, TraversalPolicy policy) {
        if (graph == null || rootNodeId == null || !graph.containsNode(rootNodeId)) {
            return KnowledgeSubgraph.builder().build();
        }

        KnowledgeSubgraph subgraph = KnowledgeSubgraph.builder()
                .graphId(graph.getMetadata().getGraphId())
                .rootNodeId(rootNodeId)
                .build();

        KnowledgeNode rootNode = graph.getNode(rootNodeId);
        subgraph.addNode(rootNode);

        List<KnowledgePath> paths = bfsTraversal.traverse(graph, rootNodeId, policy);
        for (KnowledgePath path : paths) {
            path.getNodes().forEach(subgraph::addNode);
            path.getEdges().forEach(subgraph::addEdge);
        }

        return subgraph;
    }
}
