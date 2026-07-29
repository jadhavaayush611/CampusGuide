package com.campusguide.personal.ai.atlas.context;

import com.campusguide.personal.ai.atlas.context.intelligence.ContextIntelligenceEngine;
import com.campusguide.personal.ai.atlas.context.intelligence.IntelligenceMetrics;
import com.campusguide.personal.ai.atlas.context.model.AcademicContext;
import com.campusguide.personal.ai.atlas.context.model.CampusContext;
import com.campusguide.personal.ai.atlas.context.model.UserContext;
import com.campusguide.personal.ai.atlas.context.query.QueryContext;
import com.campusguide.personal.ai.atlas.context.retrieval.RetrievalContext;
import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContextIntelligenceEngineTest {

    private ContextIntelligenceEngine intelligenceEngine;

    @BeforeEach
    void setUp() {
        intelligenceEngine = new ContextIntelligenceEngine();
    }

    @Test
    @DisplayName("ContextIntelligenceEngine processes AtlasContext, generates evidence, fuses, prioritizes, and captures metrics")
    void testProcess_FullOrchestration() {
        AtlasChatRequest request = AtlasChatRequest.builder()
                .prompt("Where is CSH hall for my cs degree?")
                .conversationId("conv-intel-1")
                .build();

        AtlasContext context = new AtlasContext("conv-intel-1", "user-1");
        context.setUserContext(UserContext.builder().name("Alice").role("STUDENT").summary("Student profile").build());
        context.setAcademicContext(AcademicContext.builder().department("Computer Science").summary("CS Department").build());
        context.setCampusContext(CampusContext.builder().location("Turing Computer Science Hall").summary("CS Campus Hall").build());

        QueryContext queryContext = QueryContext.builder()
                .rawQuery(request.getPrompt())
                .normalizedQuery("where is csh hall for my cs degree")
                .confidenceScore(0.92)
                .build();

        RetrievalContext retrievalContext = new RetrievalContext(request, queryContext);

        intelligenceEngine.process(request, queryContext, retrievalContext, context);

        // Verify EvidenceBundles populated
        assertFalse(context.getEvidenceBundles().isEmpty());
        assertTrue(context.getEvidenceBundles().containsKey("userProfile"));
        assertTrue(context.getEvidenceBundles().containsKey("academic"));
        assertTrue(context.getEvidenceBundles().containsKey("campus"));

        // Verify IntelligenceMetrics populated
        IntelligenceMetrics intelMetrics = context.getIntelligenceMetrics();
        assertNotNull(intelMetrics);
        assertFalse(intelMetrics.getFusionDecisions().isEmpty());
        assertFalse(intelMetrics.getPrioritizationDecisions().isEmpty());
        assertFalse(intelMetrics.getEvidenceSummaries().isEmpty());

        // Verify ContextMetrics synced
        assertNotNull(context.getMetrics().getFusionDecisions());
        assertNotNull(context.getMetrics().getPrioritizationDecisions());
    }
}
