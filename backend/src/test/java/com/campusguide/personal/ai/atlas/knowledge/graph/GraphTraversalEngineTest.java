package com.campusguide.personal.ai.atlas.knowledge.graph;

import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.RelationshipType;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgePath;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeSubgraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.KnowledgeNode;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeType;
import com.campusguide.personal.ai.atlas.knowledge.graph.traversal.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class GraphTraversalEngineTest {

    private KnowledgeGraph graph;
    private BreadthFirstTraversal bfsTraversal;
    private DepthFirstTraversal dfsTraversal;
    private NeighborhoodTraversal neighborhoodTraversal;
    private DefaultGraphTraversalEngine engine;

    private NodeIdentifier nA, nB, nC, nD;

    @BeforeEach
    void setUp() {
        graph = KnowledgeGraph.create("traversal_graph");

        nA = NodeIdentifier.of("A");
        nB = NodeIdentifier.of("B");
        nC = NodeIdentifier.of("C");
        nD = NodeIdentifier.of("D");

        graph.addNode(KnowledgeNode.builder().id(nA).name("Node A").type(NodeType.COURSE).build());
        graph.addNode(KnowledgeNode.builder().id(nB).name("Node B").type(NodeType.DOCUMENT).build());
        graph.addNode(KnowledgeNode.builder().id(nC).name("Node C").type(NodeType.PERSON).build());
        graph.addNode(KnowledgeNode.builder().id(nD).name("Node D").type(NodeType.BUILDING).build());

        // A -> B (PREREQUISITE)
        graph.addEdge(KnowledgeEdge.create(nA, nB, RelationshipType.PREREQUISITE, 0.9));
        // B -> C (TEACHES)
        graph.addEdge(KnowledgeEdge.create(nB, nC, RelationshipType.TEACHES, 0.8));
        // C -> D (LOCATED_IN)
        graph.addEdge(KnowledgeEdge.create(nC, nD, RelationshipType.LOCATED_IN, 0.7));
        // D -> A (Cycle)
        graph.addEdge(KnowledgeEdge.create(nD, nA, RelationshipType.RELATED_TO, 0.5));

        bfsTraversal = new BreadthFirstTraversal();
        dfsTraversal = new DepthFirstTraversal();
        neighborhoodTraversal = new NeighborhoodTraversal(bfsTraversal);
        engine = new DefaultGraphTraversalEngine(bfsTraversal, dfsTraversal, neighborhoodTraversal, null);
    }

    @Test
    @DisplayName("Should traverse graph using BFS and respect max depth and cycle detection")
    void testBfsTraversal() {
        TraversalPolicy policy = TraversalPolicy.builder()
                .maxDepth(3)
                .detectCycles(true)
                .direction(TraversalPolicy.Direction.OUTGOING)
                .build();

        List<KnowledgePath> paths = engine.traverse(graph, nA, policy);
        assertThat(paths).isNotEmpty();

        boolean reachedD = paths.stream().anyMatch(p -> p.containsNode(nD));
        assertThat(reachedD).isTrue();

        // Ensure no paths exceed maxDepth 3 hops
        assertThat(paths.stream().allMatch(p -> p.getHopCount() <= 3)).isTrue();
    }

    @Test
    @DisplayName("Should traverse graph using DFS with relationship type filtering")
    void testDfsTraversal() {
        TraversalPolicy policy = TraversalPolicy.builder()
                .maxDepth(2)
                .allowedRelationshipTypes(Set.of(RelationshipType.PREREQUISITE, RelationshipType.TEACHES))
                .build();

        List<KnowledgePath> paths = engine.traverseDfs(graph, nA, policy);
        assertThat(paths).isNotEmpty();

        // Should reach C (A -> B -> C)
        boolean reachedC = paths.stream().anyMatch(p -> p.containsNode(nC));
        assertThat(reachedC).isTrue();

        // Should NOT reach D because C->D uses LOCATED_IN which is not allowed
        boolean reachedD = paths.stream().anyMatch(p -> p.containsNode(nD));
        assertThat(reachedD).isFalse();
    }

    @Test
    @DisplayName("Should extract k-hop neighborhood subgraph correctly")
    void testNeighborhoodExtraction() {
        TraversalPolicy policy = TraversalPolicy.builder()
                .maxDepth(1)
                .direction(TraversalPolicy.Direction.OUTGOING)
                .build();

        KnowledgeSubgraph subgraph = engine.extractNeighborhood(graph, nA, policy);
        assertThat(subgraph).isNotNull();
        assertThat(subgraph.getRootNodeId()).isEqualTo(nA);
        assertThat(subgraph.containsNode(nA)).isTrue();
        assertThat(subgraph.containsNode(nB)).isTrue();
        assertThat(subgraph.getNodeCount()).isEqualTo(2);
    }
}
