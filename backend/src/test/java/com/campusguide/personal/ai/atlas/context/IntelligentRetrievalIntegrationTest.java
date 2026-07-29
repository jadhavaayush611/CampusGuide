package com.campusguide.personal.ai.atlas.context;

import com.campusguide.personal.ai.atlas.config.AtlasProperties;
import com.campusguide.personal.ai.atlas.context.contributor.*;
import com.campusguide.personal.ai.atlas.context.metrics.ContextMetrics;
import com.campusguide.personal.ai.atlas.context.model.*;
import com.campusguide.personal.ai.atlas.context.query.*;
import com.campusguide.personal.ai.atlas.context.ranking.ContextRankingService;
import com.campusguide.personal.ai.atlas.context.ranking.RelevanceScorer;
import com.campusguide.personal.ai.atlas.context.retrieval.*;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;

/**
 * End-to-End Integration Test for Phase 3.2 — Batch 3.2.1:
 * User Query → QueryContext → Retrieval → AtlasContext → Prompt Pipeline.
 */
class IntelligentRetrievalIntegrationTest {

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

        // Wire mock contributors to populate domain context objects when invoked
        doAnswer(inv -> {
            AtlasContext ctx = inv.getArgument(1);
            ctx.setUserContext(UserContext.builder().name("Alice Smith").role("STUDENT").status("ACTIVE").summary("Undergrad Student").build());
            return null;
        }).when(userProfileContributor).contribute(any(), any());

        doAnswer(inv -> {
            AtlasContext ctx = inv.getArgument(1);
            ctx.setAcademicContext(AcademicContext.builder().department("Computer Science").degreeProgram("B.S. CS").summary("CS Department").build());
            return null;
        }).when(academicContributor).contribute(any(), any());

        doAnswer(inv -> {
            AtlasContext ctx = inv.getArgument(1);
            ctx.setPlannerContext(PlannerContext.builder().activeTasksCount(3).summary("3 active assignments").build());
            return null;
        }).when(plannerContributor).contribute(any(), any());

        doAnswer(inv -> {
            AtlasContext ctx = inv.getArgument(1);
            ctx.setCalendarContext(CalendarContext.builder().todayEventsCount(2).summary("CS Lab at 2 PM").build());
            return null;
        }).when(calendarContributor).contribute(any(), any());

        doAnswer(inv -> {
            AtlasContext ctx = inv.getArgument(1);
            ctx.setCampusContext(CampusContext.builder().location("Science Building").summary("Main Science Hall").build());
            return null;
        }).when(campusContributor).contribute(any(), any());

        // Build Query Analyzer
        QueryNormalizer normalizer = new QueryNormalizer();
        TemporalExpressionResolver temporalResolver = new TemporalExpressionResolver();
        EntityExtractor entityExtractor = new EntityExtractor();
        IntentDetector intentDetector = new IntentDetector();
        QueryAnalyzer queryAnalyzer = new QueryAnalyzer(normalizer, temporalResolver, entityExtractor, intentDetector);

        // Build Context Retriever & Strategies
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

        contextEngine = new ContextEngine(
                List.of(userProfileContributor, academicContributor, plannerContributor, calendarContributor, campusContributor),
                queryAnalyzer,
                contextRetriever,
                rankingService
        );

        // Build Prompt Pipeline
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
    @DisplayName("Full End-to-End Pipeline: User Query -> QueryContext -> Selective Retrieval -> AtlasContext -> Prompt Pipeline")
    void testEndToEndPipeline_AcademicQuery() {
        // Step 1: Incoming User Query Request
        String rawPrompt = "Where is prof Smith for my cs assign due tomorrow?";
        AtlasChatRequest request = AtlasChatRequest.builder()
                .prompt(rawPrompt)
                .conversationId("conv-integration-321")
                .build();

        // Step 2: Context Engine Execution (Query Analysis + Selective Strategy Retrieval + Context Ranking)
        AtlasContext atlasContext = contextEngine.buildContext(request);

        assertNotNull(atlasContext);
        assertEquals("conv-integration-321", atlasContext.getConversationId());

        // Verify Diagnostic Metrics
        ContextMetrics metrics = atlasContext.getMetrics();
        assertNotNull(metrics);
        assertEquals("ACADEMIC_INQUIRY", metrics.getDetectedIntent());
        assertTrue(metrics.getNormalizedQuery().contains("professor"));
        assertTrue(metrics.getNormalizedQuery().contains("assignment"));
        assertTrue(metrics.getExecutedStrategies().contains("user"));
        assertTrue(metrics.getExecutedStrategies().contains("academic"));

        // Verify AtlasContext contents populated selectively
        assertNotNull(atlasContext.getUserContext());
        assertNotNull(atlasContext.getAcademicContext());

        // Step 3: Transform AtlasContext -> ContextSections
        List<ContextSection> sections = contextSectionAssembler.assembleSections(atlasContext);
        assertFalse(sections.isEmpty());

        // Step 4: Assemble Final Prompt via PromptBuilder
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

        // Step 5: Assertions on Final Assembled Prompt
        assertNotNull(finalPrompt);
        assertEquals(rawPrompt, finalPrompt.getUserMessage());
        assertTrue(finalPrompt.getSystemPrompt().contains("=== CAMPUSGUIDE ASSISTANT PERSONA ==="));
        assertTrue(finalPrompt.getSystemPrompt().contains("--- USER PROFILE CONTEXT ---"));
        assertTrue(finalPrompt.getSystemPrompt().contains("Alice Smith"));
        assertTrue(finalPrompt.getSystemPrompt().contains("--- ACADEMIC CONTEXT ---"));
        assertTrue(finalPrompt.getSystemPrompt().contains("Computer Science"));
    }
}
