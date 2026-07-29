package com.campusguide.personal.ai.atlas.prompt;

import com.campusguide.personal.ai.atlas.config.AtlasProperties;
import com.campusguide.personal.ai.atlas.context.AtlasContext;
import com.campusguide.personal.ai.atlas.context.model.*;
import com.campusguide.personal.ai.atlas.model.AtlasChatMessage;
import com.campusguide.personal.ai.atlas.model.AtlasPrompt;
import com.campusguide.personal.ai.atlas.model.AtlasRole;
import com.campusguide.personal.ai.atlas.prompt.budget.TokenBudgetManager;
import com.campusguide.personal.ai.atlas.prompt.instruction.*;
import com.campusguide.personal.ai.atlas.prompt.model.ContextSection;
import com.campusguide.personal.ai.atlas.prompt.persona.CampusGuideAssistantPersona;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PromptPipelineIntegrationTest {

    private ContextSectionAssembler assembler;
    private PromptBuilder promptBuilder;

    @BeforeEach
    void setUp() {
        assembler = new ContextSectionAssembler();

        CampusGuideAssistantPersona persona = new CampusGuideAssistantPersona(List.of(
                new CoreIdentityInstruction(),
                new SafetyInstruction(),
                new CampusInstruction(),
                new FormattingInstruction(),
                new ResponsePolicyInstruction()
        ));

        AtlasProperties properties = new AtlasProperties();
        TokenBudgetManager tokenBudgetManager = new TokenBudgetManager();
        PromptTemplate promptTemplate = new PromptTemplate();

        promptBuilder = new PromptBuilder(properties, persona, tokenBudgetManager, promptTemplate);
    }

    @Test
    void testFullPromptPipelineAssembly_WithContextSectionsPersonaAndBudget() {
        // 1. Construct AtlasContext Aggregate
        AtlasContext atlasContext = AtlasContext.builder()
                .conversationId("conv-pipeline-1")
                .userId("student-123")
                .userContext(UserContext.builder().name("John Doe").role("STUDENT").status("ACTIVE").summary("Active undergrad").build())
                .academicContext(AcademicContext.builder().department("Computer Science").degreeProgram("B.S. CS").gpa(3.9).summary("Dean's list").currentCourses(List.of("CS301", "CS302")).build())
                .plannerContext(PlannerContext.builder().activeTasksCount(2).summary("2 pending assignments").build())
                .calendarContext(CalendarContext.builder().todayEventsCount(1).summary("Algorithm lecture today").build())
                .campusContext(CampusContext.builder().location("North Campus").summary("Library open late").build())
                .build();

        // 2. Execute ContextSectionAssembler (AtlasContext -> List<ContextSection>)
        List<ContextSection> sections = assembler.assembleSections(atlasContext);
        assertEquals(5, sections.size());

        // 3. Conversation History
        List<AtlasChatMessage> history = List.of(
                AtlasChatMessage.builder().role(AtlasRole.USER).content("Hi Atlas!").build(),
                AtlasChatMessage.builder().role(AtlasRole.ASSISTANT).content("Hello John! How can I assist you with your CS studies today?").build()
        );

        // 4. Invoke PromptBuilder (which uses PromptTemplate & TokenBudgetManager)
        AtlasPrompt prompt = promptBuilder.buildPrompt(
                "What is my schedule and next deadline for {department}?",
                null, // Use default persona system prompt
                history,
                sections,
                Map.of("department", "Computer Science"),
                "gpt-4o-mini",
                0.7,
                1000
        );

        // 5. Verify Prompt Pipeline Output
        assertNotNull(prompt);
        assertNotNull(prompt.getSystemPrompt());
        assertTrue(prompt.getSystemPrompt().contains("=== CAMPUSGUIDE ASSISTANT PERSONA ==="));
        assertTrue(prompt.getSystemPrompt().contains("[CoreIdentity]:"));
        assertTrue(prompt.getSystemPrompt().contains("[Safety]:"));
        assertTrue(prompt.getSystemPrompt().contains("[Campus]:"));
        assertTrue(prompt.getSystemPrompt().contains("[Formatting]:"));
        assertTrue(prompt.getSystemPrompt().contains("[ResponsePolicy]:"));

        assertTrue(prompt.getSystemPrompt().contains("--- USER PROFILE CONTEXT ---"));
        assertTrue(prompt.getSystemPrompt().contains("Name: John Doe"));
        assertTrue(prompt.getSystemPrompt().contains("--- ACADEMIC CONTEXT ---"));
        assertTrue(prompt.getSystemPrompt().contains("Department: Computer Science"));

        assertEquals("What is my schedule and next deadline for Computer Science?", prompt.getUserMessage());

        // 6. Verify Diagnostic PromptVersion Metadata
        assertNotNull(prompt.getPromptVersion());
        assertEquals("1.0.0", prompt.getPromptVersion().getVersion());
        assertEquals(5, prompt.getPromptVersion().getSectionsIncluded().size());
        assertTrue(prompt.getPromptVersion().getTokenEstimates().get("totalPromptTokens") > 0);
    }
}
