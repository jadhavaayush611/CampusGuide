package com.campusguide.personal.ai.atlas.knowledge.graph;

import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.RelationshipStrength;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.RelationshipType;
import com.campusguide.personal.ai.atlas.knowledge.graph.metrics.GraphReasoningMetrics;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.KnowledgeNode;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeType;
import com.campusguide.personal.ai.atlas.knowledge.graph.projection.*;
import com.campusguide.personal.ai.atlas.knowledge.graph.view.KnowledgeGraphView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GraphProjectionTest {

    private KnowledgeGraph graph;
    private KnowledgeNode nodeA;
    private KnowledgeNode nodeB;
    private KnowledgeNode nodeC;
    private KnowledgeEdge edgeAB;
    private KnowledgeEdge edgeBC;

    @BeforeEach
    void setUp() {
        graph = KnowledgeGraph.create("test_graph");
        nodeA = KnowledgeNode.builder()
                .id(NodeIdentifier.of(NodeType.COURSE, "CS101"))
                .name("Intro to CS")
                .type(NodeType.COURSE)
                .sourceCollectionId("academic")
                .build();

        nodeB = KnowledgeNode.builder()
                .id(NodeIdentifier.of(NodeType.COURSE, "CS102"))
                .name("Data Structures")
                .type(NodeType.COURSE)
                .sourceCollectionId("academic")
                .build();

        nodeC = KnowledgeNode.builder()
                .id(NodeIdentifier.of(NodeType.FACULTY, "P1"))
                .name("Dr. Smith")
                .type(NodeType.FACULTY)
                .sourceCollectionId("faculty")
                .build();

        edgeAB = KnowledgeEdge.builder()
                .id("e1")
                .sourceNodeId(nodeA.getId())
                .targetNodeId(nodeB.getId())
                .relationshipType(RelationshipType.PREREQUISITE_FOR)
                .strength(RelationshipStrength.of(0.9))
                .build();

        edgeBC = KnowledgeEdge.builder()
                .id("e2")
                .sourceNodeId(nodeB.getId())
                .targetNodeId(nodeC.getId())
                .relationshipType(RelationshipType.TAUGHT_BY)
                .strength(RelationshipStrength.of(0.8))
                .build();

        graph.addNode(nodeA);
        graph.addNode(nodeB);
        graph.addNode(nodeC);
        graph.addEdge(edgeAB);
        graph.addEdge(edgeBC);
    }

    @Test
    @DisplayName("GraphProjection operates exclusively as a read-only KnowledgeGraphView")
    void testGraphProjectionView() {
        GraphProjectionPolicy policy = GraphProjectionPolicy.permissive();
        GraphProjection projection = new GraphProjectionBuilder()
                .policy(policy)
                .buildFrom(graph);

        assertTrue(projection instanceof KnowledgeGraphView);
        assertEquals(3, projection.getNodeCount());
        assertEquals(2, projection.getEdgeCount());
        assertEquals(nodeA, projection.getNode(nodeA.getId()));
        assertTrue(projection.containsNode(nodeA.getId()));
        assertEquals(1, projection.getOutgoingEdges(nodeA.getId()).size());
    }

    @Test
    @DisplayName("GraphProjection Engine projects neighborhood deterministically up to depth limit")
    void testNeighborhoodProjection() {
        GraphProjectionEngine engine = new GraphProjectionEngine();
        GraphProjectionPolicy policy = GraphProjectionPolicy.builder().maxDepth(1).build();
        GraphReasoningMetrics metrics = GraphReasoningMetrics.builder().build();

        GraphProjection projection = engine.projectWithStrategy(
                graph,
                Set.of(nodeA.getId()),
                policy,
                new NeighborhoodProjection(),
                metrics
        );

        assertNotNull(projection);
        assertEquals(2, projection.getNodeCount()); // nodeA and nodeB
        assertEquals(1, projection.getEdgeCount()); // edgeAB only
        assertTrue(projection.containsNode(nodeA.getId()));
        assertTrue(projection.containsNode(nodeB.getId()));
        assertFalse(projection.containsNode(nodeC.getId()));
        assertTrue(metrics.getProjectionLatencyMs() >= 0);
    }

    @Test
    @DisplayName("CollectionProjection filters nodes and edges by active collection")
    void testCollectionProjection() {
        GraphProjectionEngine engine = new GraphProjectionEngine();
        GraphProjectionPolicy policy = GraphProjectionPolicy.permissive();

        GraphProjection projection = engine.projectWithStrategy(
                graph,
                Set.of(nodeA.getId(), nodeB.getId(), nodeC.getId()),
                policy,
                new CollectionProjection(Set.of("academic")),
                null
        );

        assertEquals(2, projection.getNodeCount()); // academic collection nodes
        assertEquals(1, projection.getEdgeCount()); // edgeAB between academic nodes
    }
}
