package com.campusguide.personal.ai.atlas.context;

import com.campusguide.personal.ai.atlas.config.AtlasProperties;
import com.campusguide.personal.ai.atlas.context.contributor.*;
import com.campusguide.personal.ai.atlas.context.evidence.EvidenceBundle;
import com.campusguide.personal.ai.atlas.context.intelligence.ContextIntelligenceEngine;
import com.campusguide.personal.ai.atlas.context.intelligence.IntelligenceMetrics;
import com.campusguide.personal.ai.atlas.context.metrics.ContextMetrics;
import com.campusguide.personal.ai.atlas.context.model.*;
import com.campusguide.personal.ai.atlas.context.query.*;
import com.campusguide.personal.ai.atlas.context.ranking.ContextRankingService;
import com.campusguide.personal.ai.atlas.context.ranking.RelevanceScorer;
import com.campusguide.personal.ai.atlas.context.retrieval.*;
import com.campusguide.personal.ai.atlas.context.service.knowledge.CampusKnowledgeService;
import com.campusguide.personal.ai.atlas.context.service.knowledge.InMemoryCampusKnowledgeProvider;
import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import com.campusguide.personal.ai.atlas.model.AtlasPrompt;
import com.campusguide.personal.ai.atlas.prompt.ContextSectionAssembler;
import com.campusguide.personal.ai.atlas.prompt.PromptBuilder;
import com.campusguide.personal.ai.atlas.prompt.PromptTemplate;
import com.campusguide.personal.ai.atlas.prompt.budget.TokenBudgetManager;
import com.campusguide.personal.ai.atlas.prompt.instruction.*;
import com.campusguide.personal.ai.atlas.prompt.model.ContextSection;
import com.campusguide.personal.ai.atlas.prompt.persona.CampusGuideAssistantPersona;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * End-to-End Integration Test for Phase 3.2 — Batch 3.2.2 Context Intelligence Layer:
 * User Query -> Query Context -> Selective Retrieval -> Context Intelligence (Evidence, Fusion, Conflicts, Prioritization, Cache) -> AtlasContext -> Prompt Pipeline.
 */
class ContextIntelligenceIntegrationTest {

    @Mock private UserProfileContributor userProfileContributor;
    @Mock private AcademicContributor academicContributor;
    @Mock private PlannerContributor plannerContributor;
    @Mock private CalendarContributor calendarContributor;
    @Mock private CampusContributor campusContributor;

    private ContextEngine contextEngine;
    private ContextSectionAssembler contextSectionAssembler;
    private PromptBuilder promptBuilder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        doAnswer(inv -> {
            AtlasContext ctx = inv.getArgument(1);
            ctx.setUserContext(UserContext.builder().name("Alice Smith").role("STUDENT").summary("Undergrad Student Profile").build());
            return null;
        }).when(userProfileContributor).contribute(any(), any());

        doAnswer(inv -> {
            AtlasContext ctx = inv.getArgument(1);
            ctx.setAcademicContext(AcademicContext.builder().department("Computer Science").degreeProgram("B.S. CS").summary("CS Academic Dept").build());
            return null;
        }).when(academicContributor).contribute(any(), any());

        doAnswer(inv -> {
            AtlasContext ctx = inv.getArgument(1);
            ctx.setPlannerContext(PlannerContext.builder().activeTasksCount(2).summary("2 active tasks").build());
            return null;
        }).when(plannerContributor).contribute(any(), any());

        doAnswer(inv -> {
            AtlasContext ctx = inv.getArgument(1);
            ctx.setCalendarContext(CalendarContext.builder().todayEventsCount(1).summary("CS Seminar at 3 PM").build());
            return null;
        }).when(calendarContributor).contribute(any(), any());

        doAnswer(inv -> {
            AtlasContext ctx = inv.getArgument(1);
            ctx.setCampusContext(CampusContext.builder().location("Turing Computer Science Hall").summary("Main CS Hall").build());
            return null;
        }).when(campusContributor).contribute(any(), any());

        // Build Query Analyzer
        QueryNormalizer normalizer = new QueryNormalizer();
        TemporalExpressionResolver temporalResolver = new TemporalExpressionResolver();
        EntityExtractor entityExtractor = new EntityExtractor();
        IntentDetector intentDetector = new IntentDetector();
        QueryAnalyzer queryAnalyzer = new QueryAnalyzer(normalizer, temporalResolver, entityExtractor, intentDetector);

        // Build Strategies & Retrieval
        List<RetrievalStrategy> strategies = List.of(
                new UserRetrievalStrategy(userProfileContributor),
                new AcademicRetrievalStrategy(academicContributor),
                new PlannerRetrievalStrategy(plannerContributor),
                new CalendarRetrievalStrategy(calendarContributor),
                new CampusRetrievalStrategy(campusContributor)
        );
        RetrievalPolicy policy = new RetrievalPolicy();
        ContextRetriever contextRetriever = new ContextRetriever(strategies, policy);
        ContextRankingService rankingService = new ContextRankingService(new RelevanceScorer());

        // Context Intelligence Engine
        ContextIntelligenceEngine intelligenceEngine = new ContextIntelligenceEngine(
                null, null, null, null, new CampusKnowledgeService(new InMemoryCampusKnowledgeProvider())
        );

        contextEngine = new ContextEngine(
                List.of(userProfileContributor, academicContributor, plannerContributor, calendarContributor, campusContributor),
                queryAnalyzer,
                contextRetriever,
                rankingService,
                intelligenceEngine
        );

        // Prompt Pipeline
        contextSectionAssembler = new ContextSectionAssembler();
        CampusGuideAssistantPersona persona = new CampusGuideAssistantPersona(List.of(
                new CoreIdentityInstruction(),
                new SafetyInstruction(),
                new CampusInstruction(),
                new FormattingInstruction(),
                new ResponsePolicyInstruction()
        ));
        AtlasProperties properties = new AtlasProperties();
        TokenBudgetManager budgetManager = new TokenBudgetManager();
        PromptTemplate promptTemplate = new PromptTemplate();
        promptBuilder = new PromptBuilder(properties, persona, budgetManager, promptTemplate);
    }

    @Test
    @DisplayName("Complete Context Intelligence Pipeline: Query -> Retrieval -> Evidence Fusion & Prioritization -> AtlasContext -> Prompt")
    void testCompleteContextIntelligencePipeline() {
        String rawPrompt = "Where is CSH hall for prof Smith office hours?";
        AtlasChatRequest request = AtlasChatRequest.builder()
                .prompt(rawPrompt)
                .conversationId("conv-intel-e2e")
                .build();

        AtlasContext atlasContext = contextEngine.buildContext(request);

        assertNotNull(atlasContext);
        assertEquals("conv-intel-e2e", atlasContext.getConversationId());

        // Verify Evidence Bundles
        assertFalse(atlasContext.getEvidenceBundles().isEmpty());

        // Verify Intelligence Metrics
        IntelligenceMetrics intelMetrics = atlasContext.getIntelligenceMetrics();
        assertNotNull(intelMetrics);
        assertFalse(intelMetrics.getFusionDecisions().isEmpty());
        assertFalse(intelMetrics.getPrioritizationDecisions().isEmpty());

        // Verify ContextMetrics
        ContextMetrics metrics = atlasContext.getMetrics();
        assertNotNull(metrics);
        assertFalse(metrics.getFusionDecisions().isEmpty());

        // Prompt Assembly
        List<ContextSection> sections = contextSectionAssembler.assembleSections(atlasContext);
        assertFalse(sections.isEmpty());

        AtlasPrompt finalPrompt = promptBuilder.buildPrompt(
                request.getPrompt(),
                request.getSystemPrompt(),
                null,
                sections,
                atlasContext.getMergedPlaceholders(),
                "gpt-4o-mini",
                0.7,
                1000
        );

        assertNotNull(finalPrompt);
        assertEquals(rawPrompt, finalPrompt.getUserMessage());
        assertTrue(finalPrompt.getSystemPrompt().contains("=== CAMPUSGUIDE ASSISTANT PERSONA ==="));
        assertTrue(finalPrompt.getSystemPrompt().contains("Alice Smith"));
    }
}
