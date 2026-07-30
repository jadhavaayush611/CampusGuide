package com.campusguide.personal.ai.atlas.knowledge.graph;

import com.campusguide.personal.ai.atlas.context.AtlasContext;
import com.campusguide.personal.ai.atlas.context.contributor.GraphContextContributor;
import com.campusguide.personal.ai.atlas.context.evidence.EvidenceBundle;
import com.campusguide.personal.ai.atlas.context.intelligence.ContextIntelligenceEngine;
import com.campusguide.personal.ai.atlas.context.query.QueryContext;

import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import com.campusguide.personal.ai.atlas.knowledge.graph.projection.GraphProjectionEngine;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.engine.ReasoningEngine;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.engine.ReasoningEvidence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GraphReasoningIntegrationIT {

    @Test
    @DisplayName("GraphReasoning seamlessly integrates with AtlasContext and Context Intelligence Layer")
    void testEndToEndContextIntelligenceIntegration() {
        GraphProjectionEngine projectionEngine = new GraphProjectionEngine();
        ReasoningEngine reasoningEngine = new ReasoningEngine();
        GraphContextContributor contributor = new GraphContextContributor(projectionEngine, reasoningEngine);

        AtlasChatRequest request = AtlasChatRequest.builder().prompt("Explain prerequisite structure for CS102").build();
        AtlasContext context = new AtlasContext("conv_100", "user_100");

        // 1. Contribute Graph Context Evidence to AtlasContext
        contributor.contribute(request, context);

        // Verify contribution and bundle presence
        Object rawContribution = context.getContribution("graphReasoning");
        assertNotNull(rawContribution);
        assertTrue(rawContribution instanceof ReasoningEvidence);

        EvidenceBundle bundle = context.getEvidenceBundles().get("graphReasoning");
        assertNotNull(bundle);
        assertEquals("graphReasoning", bundle.getTargetDomain());
        assertFalse(bundle.getEvidences().isEmpty());

        // 2. Process AtlasContext via ContextIntelligenceEngine
        ContextIntelligenceEngine intelligenceEngine = new ContextIntelligenceEngine();
        QueryContext queryContext = QueryContext.builder().rawQuery("Explain prerequisite structure for CS102").build();

        intelligenceEngine.process(request, queryContext, null, context);

        assertNotNull(context.getIntelligenceMetrics());
        assertNotNull(context.getMetrics());
    }
}
