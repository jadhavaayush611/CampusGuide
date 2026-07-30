package com.campusguide.personal.ai.atlas.knowledge.graph;

import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.RelationshipStrength;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.RelationshipType;
import com.campusguide.personal.ai.atlas.knowledge.graph.inference.*;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.KnowledgeNode;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeType;
import com.campusguide.personal.ai.atlas.knowledge.graph.projection.GraphProjectionBuilder;
import com.campusguide.personal.ai.atlas.knowledge.graph.view.KnowledgeGraphView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InferenceEngineTest {

    private KnowledgeGraph baseGraph;
    private KnowledgeNode nodeA;
    private KnowledgeNode nodeB;
    private KnowledgeNode nodeC;

    @BeforeEach
    void setUp() {
        baseGraph = KnowledgeGraph.create("inference_graph");
        nodeA = KnowledgeNode.builder().id(NodeIdentifier.of(NodeType.STUDENT, "S1")).name("Alice").type(NodeType.STUDENT).build();
        nodeB = KnowledgeNode.builder().id(NodeIdentifier.of(NodeType.STUDENT, "S2")).name("Bob").type(NodeType.STUDENT).build();
        nodeC = KnowledgeNode.builder().id(NodeIdentifier.of(NodeType.STUDENT, "S3")).name("Charlie").type(NodeType.STUDENT).build();

        KnowledgeEdge edgeAB = KnowledgeEdge.builder()
                .id("eAB")
                .sourceNodeId(nodeA.getId())
                .targetNodeId(nodeB.getId())
                .relationshipType(RelationshipType.SAME_DEPARTMENT)
                .strength(RelationshipStrength.of(0.9))
                .build();

        KnowledgeEdge edgeBC = KnowledgeEdge.builder()
                .id("eBC")
                .sourceNodeId(nodeB.getId())
                .targetNodeId(nodeC.getId())
                .relationshipType(RelationshipType.SAME_DEPARTMENT)
                .strength(RelationshipStrength.of(0.8))
                .build();

        baseGraph.addNode(nodeA);
        baseGraph.addNode(nodeB);
        baseGraph.addNode(nodeC);
        baseGraph.addEdge(edgeAB);
        baseGraph.addEdge(edgeBC);
    }

    @Test
    @DisplayName("InferenceEngine infers virtual relationships without mutating source graph")
    void testInferenceWithoutGraphMutation() {
        KnowledgeGraphView view = new GraphProjectionBuilder().buildFrom(baseGraph);
        int originalEdgeCount = baseGraph.getEdges().size();

        InferenceRegistry registry = new InferenceRegistry();
        InferenceEngine engine = new InferenceEngine(registry);

        InferenceResult result = engine.infer(view);

        assertNotNull(result);
        assertFalse(result.getInferredEdges().isEmpty());
        assertEquals(originalEdgeCount, baseGraph.getEdges().size()); // Source graph un-mutated!

        KnowledgeEdge inferredEdge = result.getInferredEdges().get(0);
        assertEquals(nodeA.getId(), inferredEdge.getSourceNodeId());
        assertEquals(nodeC.getId(), inferredEdge.getTargetNodeId());
        assertEquals(RelationshipType.INFERRED_ACADEMIC_PEER, inferredEdge.getRelationshipType());
    }
}
