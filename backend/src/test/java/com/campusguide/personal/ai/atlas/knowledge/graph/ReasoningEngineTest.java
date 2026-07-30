package com.campusguide.personal.ai.atlas.knowledge.graph;

import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.RelationshipStrength;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.RelationshipType;
import com.campusguide.personal.ai.atlas.knowledge.graph.metrics.GraphReasoningMetrics;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.KnowledgeNode;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeType;
import com.campusguide.personal.ai.atlas.knowledge.graph.projection.GraphProjection;
import com.campusguide.personal.ai.atlas.knowledge.graph.projection.GraphProjectionBuilder;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.context.GraphContext;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.context.ReasoningObjective;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.engine.ReasoningEngine;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.engine.ReasoningEvidence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ReasoningEngineTest {

    private KnowledgeGraph graph;
    private KnowledgeNode nodeA;
    private KnowledgeNode nodeB;

    @BeforeEach
    void setUp() {
        graph = KnowledgeGraph.create("reasoning_test_graph");
        nodeA = KnowledgeNode.builder().id(NodeIdentifier.of(NodeType.COURSE, "CS101")).name("CS101").type(NodeType.COURSE).build();
        nodeB = KnowledgeNode.builder().id(NodeIdentifier.of(NodeType.COURSE, "CS102")).name("CS102").type(NodeType.COURSE).build();
        KnowledgeEdge edge = KnowledgeEdge.builder().id("e1").sourceNodeId(nodeA.getId()).targetNodeId(nodeB.getId()).relationshipType(RelationshipType.PREREQUISITE_FOR).strength(RelationshipStrength.of(0.95)).build();

        graph.addNode(nodeA);
        graph.addNode(nodeB);
        graph.addEdge(edge);
    }

    @Test
    @DisplayName("ReasoningEngine executes reasoning pipeline and records metrics")
    void testReasoningExecution() {
        GraphProjection projection = new GraphProjectionBuilder().buildFrom(graph);
        ReasoningObjective obj = ReasoningObjective.findPath(nodeA.getId(), nodeB.getId());

        GraphContext ctx = GraphContext.builder()
                .contextId("gctx_test")
                .rootNodes(Set.of(nodeA.getId()))
                .graphView(projection)
                .objective(obj)
                .build();

        GraphReasoningMetrics metrics = GraphReasoningMetrics.builder().build();
        ReasoningEngine engine = new ReasoningEngine();

        ReasoningEvidence evidence = engine.reason(ctx, metrics);

        assertNotNull(evidence);
        assertTrue(evidence.getConfidence() > 0.0);
        assertNotNull(evidence.getExplanation());
        assertTrue(metrics.getReasoningLatencyMs() >= 0);
    }
}
