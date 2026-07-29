package com.campusguide.personal.ai.atlas.prompt;

import com.campusguide.personal.ai.atlas.config.AtlasProperties;
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

class PromptBuilderTest {

    private AtlasProperties properties;
    private PromptBuilder promptBuilder;
    private CampusGuideAssistantPersona persona;

    @BeforeEach
    void setUp() {
        properties = new AtlasProperties();
        List<InstructionLayer> layers = List.of(
                new CoreIdentityInstruction(),
                new SafetyInstruction(),
                new CampusInstruction(),
                new FormattingInstruction(),
                new ResponsePolicyInstruction()
        );
        persona = new CampusGuideAssistantPersona(layers);
        TokenBudgetManager tokenBudgetManager = new TokenBudgetManager();
        PromptTemplate promptTemplate = new PromptTemplate();

        promptBuilder = new PromptBuilder(properties, persona, tokenBudgetManager, promptTemplate);
    }

    @Test
    void testBuildPrompt_WithContextSectionsAndPlaceholders() {
        ContextSection userSection = ContextSection.of("--- USER PROFILE CONTEXT ---", "Name: Alex", "USER_PROFILE", 1, true);
        ContextSection academicSection = ContextSection.of("--- ACADEMIC CONTEXT ---", "Department: Computer Science", "ACADEMIC", 2, false);

        List<AtlasChatMessage> history = List.of(
                AtlasChatMessage.builder().role(AtlasRole.USER).content("Hello").build()
        );

        AtlasPrompt prompt = promptBuilder.buildPrompt(
                "Can you review my roadmap for {department}?",
                "Hello {student_name}, custom system prompt",
                history,
                List.of(userSection, academicSection),
                Map.of("student_name", "Alex", "department", "Computer Science"),
                "gpt-4o-mini",
                0.7,
                1000
        );

        assertNotNull(prompt);
        assertTrue(prompt.getSystemPrompt().contains("Hello Alex, custom system prompt"));
        assertTrue(prompt.getSystemPrompt().contains("Context Information:"));
        assertTrue(prompt.getSystemPrompt().contains("--- USER PROFILE CONTEXT ---"));
        assertTrue(prompt.getSystemPrompt().contains("--- ACADEMIC CONTEXT ---"));
        assertEquals("Can you review my roadmap for Computer Science?", prompt.getUserMessage());
        assertNotNull(prompt.getPromptVersion());
        assertEquals("1.0.0", prompt.getPromptVersion().getVersion());
        assertEquals(2, prompt.getPromptVersion().getSectionsIncluded().size());
    }

    @Test
    void testBuildPrompt_NullSystemPromptUsesPersonaBase() {
        AtlasPrompt prompt = promptBuilder.buildPrompt(
                "General question",
                null,
                null,
                List.of(),
                null,
                null,
                null,
                null
        );

        assertNotNull(prompt);
        assertTrue(prompt.getSystemPrompt().contains("=== CAMPUSGUIDE ASSISTANT PERSONA ==="));
        assertTrue(prompt.getSystemPrompt().contains("[CoreIdentity]"));
        assertEquals(2, prompt.getFormattedMessages().size()); // 1 System + 1 User
    }
}
