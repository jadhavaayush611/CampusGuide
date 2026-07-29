package com.campusguide.personal.ai.atlas.context;

import com.campusguide.personal.ai.atlas.config.AtlasProperties;
import com.campusguide.personal.ai.atlas.context.contributor.*;
import com.campusguide.personal.ai.atlas.context.metrics.ContextMetrics;
import com.campusguide.personal.ai.atlas.context.service.*;
import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import com.campusguide.personal.ai.atlas.prompt.ContextSectionAssembler;
import com.campusguide.personal.ai.atlas.prompt.PromptBuilder;
import com.campusguide.personal.ai.atlas.prompt.PromptTemplate;
import com.campusguide.personal.ai.atlas.prompt.budget.TokenBudgetManager;
import com.campusguide.personal.ai.atlas.prompt.instruction.*;
import com.campusguide.personal.ai.atlas.prompt.model.ContextSection;
import com.campusguide.personal.ai.atlas.prompt.persona.CampusGuideAssistantPersona;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AtlasContextIntegrationTest {

    private ContextEngine contextEngine;
    private ContextSectionAssembler contextSectionAssembler;
    private PromptBuilder promptBuilder;

    @BeforeEach
    void setUp() {
        UserContextService userContextService = new UserContextService(null);
        PlannerContextService plannerContextService = new PlannerContextService(null);
        CalendarContextService calendarContextService = new CalendarContextService(null);
        AcademicContextService academicContextService = new AcademicContextService(null);
        CampusContextService campusContextService = new CampusContextService(null);

        List<ContextContributor> contributors = List.of(
                new UserProfileContributor(userContextService),
                new PlannerContributor(plannerContextService),
                new CalendarContributor(calendarContextService),
                new AcademicContributor(academicContextService),
                new CampusContributor(campusContextService)
        );

        contextEngine = new ContextEngine(contributors);
        contextSectionAssembler = new ContextSectionAssembler();

        CampusGuideAssistantPersona persona = new CampusGuideAssistantPersona(List.of(
                new CoreIdentityInstruction(),
                new SafetyInstruction(),
                new CampusInstruction(),
                new FormattingInstruction(),
                new ResponsePolicyInstruction()
        ));

        promptBuilder = new PromptBuilder(
                new AtlasProperties(),
                persona,
                new TokenBudgetManager(),
                new PromptTemplate()
        );
    }

    @Test
    void testEndToEndContextAggregationAndPromptRendering() {
        AtlasChatRequest request = AtlasChatRequest.builder()
                .conversationId("conv-integration-1")
                .prompt("What should I focus on today for {department} at the campus library?")
                .systemPrompt("You are Atlas AI assisting {student_name}.")
                .contextPlaceholders(Map.of("student_name", "Sarah", "department", "Computer Science"))
                .build();

        AtlasContext context = contextEngine.buildContext(request);

        // 1. Validate AtlasContext Aggregation
        assertNotNull(context);
        assertEquals("conv-integration-1", context.getConversationId());
        assertNotNull(context.getUserContext());
        assertEquals("Sarah", context.getUserContext().getName());

        assertNotNull(context.getAcademicContext());
        assertEquals("Computer Science", context.getAcademicContext().getDepartment());

        assertNotNull(context.getPlannerContext());
        assertNotNull(context.getCalendarContext());
        assertNotNull(context.getCampusContext());

        // 2. Validate Diagnostics Metrics
        ContextMetrics metrics = context.getMetrics();
        assertNotNull(metrics);
        assertEquals(5, metrics.getExecutionTimeMs().size());
        assertTrue(metrics.getEstimatedContextSizeBytes() > 0);
        assertTrue(metrics.getEstimatedTokenCount() > 0);

        // 3. Assemble ContextSections & Render Prompt
        List<ContextSection> sections = contextSectionAssembler.assembleSections(context);
        assertEquals(5, sections.size());

        var prompt = promptBuilder.buildPrompt(
                request.getPrompt(),
                request.getSystemPrompt(),
                null,
                sections,
                context.getMergedPlaceholders(),
                "gpt-4o-mini",
                0.7,
                1000
        );

        assertNotNull(prompt);
        assertTrue(prompt.getSystemPrompt().contains("You are Atlas AI assisting Sarah."));
        assertTrue(prompt.getSystemPrompt().contains("--- USER PROFILE CONTEXT ---"));
        assertTrue(prompt.getSystemPrompt().contains("--- ACADEMIC CONTEXT ---"));
        assertTrue(prompt.getSystemPrompt().contains("--- PLANNER CONTEXT ---"));
        assertTrue(prompt.getSystemPrompt().contains("--- CALENDAR CONTEXT ---"));
        assertTrue(prompt.getSystemPrompt().contains("--- CAMPUS CONTEXT ---"));
        assertEquals("What should I focus on today for Computer Science at the campus library?", prompt.getUserMessage());
    }
}
