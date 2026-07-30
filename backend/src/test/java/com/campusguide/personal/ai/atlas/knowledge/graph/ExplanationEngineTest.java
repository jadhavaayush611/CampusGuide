package com.campusguide.personal.ai.atlas.knowledge.graph;

import com.campusguide.personal.ai.atlas.knowledge.graph.confidence.ReasoningConfidence;

import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.RelationshipStrength;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.RelationshipType;
import com.campusguide.personal.ai.atlas.knowledge.graph.explainability.ExplanationEngine;
import com.campusguide.personal.ai.atlas.knowledge.graph.explainability.ReasoningExplanation;
import com.campusguide.personal.ai.atlas.knowledge.graph.inference.InferenceResult;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.KnowledgeNode;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeType;
import com.campusguide.personal.ai.atlas.knowledge.graph.path.EvidencePath;
import com.campusguide.personal.ai.atlas.knowledge.graph.path.ReasoningChain;
import com.campusguide.personal.ai.atlas.knowledge.graph.projection.GraphProjectionBuilder;
import com.campusguide.personal.ai.atlas.knowledge.graph.view.KnowledgeGraphView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExplanationEngineTest {

    @Test
    @DisplayName("ExplanationEngine generates structured reasoning explanations with steps and cited edges")
    void testExplanationGeneration() {
        ExplanationEngine engine = new ExplanationEngine();

        KnowledgeNode nodeA = KnowledgeNode.builder().id(NodeIdentifier.of(NodeType.COURSE, "CS101")).name("CS101").type(NodeType.COURSE).sourceArtifactId("art_1").build();
        KnowledgeNode nodeB = KnowledgeNode.builder().id(NodeIdentifier.of(NodeType.COURSE, "CS102")).name("CS102").type(NodeType.COURSE).sourceArtifactId("art_2").build();
        KnowledgeEdge edgeAB = KnowledgeEdge.builder().id("e1").sourceNodeId(nodeA.getId()).targetNodeId(nodeB.getId()).relationshipType(RelationshipType.PREREQUISITE_FOR).strength(RelationshipStrength.of(0.9)).build();

        EvidencePath path = EvidencePath.builder()
                .nodes(List.of(nodeA, nodeB))
                .edges(List.of(edgeAB))
                .hopCount(1)
                .cumulativeScore(0.9)
                .confidence(0.9)
                .pathType("SHORTEST")
                .build();

        ReasoningChain chain = ReasoningChain.builder()
                .chainId("chain_1")
                .evidencePaths(List.of(path))
                .logicalSteps(List.of("Step 1: CS101 -> CS102"))
                .overallChainConfidence(0.9)
                .build();

        ReasoningConfidence confidence = ReasoningConfidence.create(0.9, null, "High confidence");

        KnowledgeGraphView view = new GraphProjectionBuilder().buildFrom((com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraph) null);

        ReasoningExplanation explanation = engine.explain(
                view,
                chain,
                path,
                InferenceResult.empty(),
                confidence,
                null
        );

        assertNotNull(explanation);
        assertEquals(1, explanation.getSteps().size());
        assertTrue(explanation.getCitedGraphEdges().contains("e1"));
        assertTrue(explanation.getCitedArtifacts().contains("art_1"));
        assertNotNull(explanation.getSummary());
    }
}
