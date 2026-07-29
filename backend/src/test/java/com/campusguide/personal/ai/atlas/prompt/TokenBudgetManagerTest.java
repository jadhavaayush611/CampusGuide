package com.campusguide.personal.ai.atlas.prompt;

import com.campusguide.personal.ai.atlas.model.AtlasChatMessage;
import com.campusguide.personal.ai.atlas.model.AtlasRole;
import com.campusguide.personal.ai.atlas.prompt.budget.TokenBudgetManager;
import com.campusguide.personal.ai.atlas.prompt.budget.TokenBudgetResult;
import com.campusguide.personal.ai.atlas.prompt.model.ContextSection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TokenBudgetManagerTest {

    private TokenBudgetManager budgetManager;

    @BeforeEach
    void setUp() {
        budgetManager = new TokenBudgetManager();
    }

    @Test
    void testEvaluateBudget_UnderCap_IncludesAllSections() {
        ContextSection sec1 = ContextSection.of("Section 1", "Content 1", "CAT1", 1, true);
        ContextSection sec2 = ContextSection.of("Section 2", "Content 2", "CAT2", 2, false);

        TokenBudgetResult result = budgetManager.evaluateBudget(
                "System Prompt",
                List.of(sec1, sec2),
                List.of(),
                "User Message",
                1024,
                4096
        );

        assertNotNull(result);
        assertEquals(2, result.getIncludedSections().size());
        assertTrue(result.getSkippedSections().isEmpty());
        assertEquals(1024, result.getReservedCompletionTokens());
        assertTrue(result.getEstimatedPromptTokens() > 0);
    }

    @Test
    void testEvaluateBudget_ExceedsCap_PrunesOptionalLowerPrioritySections() {
        String longText = "Very long text description repeating to increase token count... ".repeat(10);
        // High priority required
        ContextSection userSec = ContextSection.of("User Section", "Profile: " + longText, "USER", 1, true);
        // Priority 2 optional
        ContextSection acadSec = ContextSection.of("Academic Section", "Academic: " + longText, "ACADEMIC", 2, false);
        // Priority 5 optional (lowest priority, pruned first)
        ContextSection campusSec = ContextSection.of("Campus Section", "Campus: " + longText, "CAMPUS", 5, false);

        // Force budget cap: overall = 300, reserved = 100 -> netPromptCap = 200 tokens
        TokenBudgetResult result = budgetManager.evaluateBudget(
                "System Prompt Base Instructions",
                List.of(userSec, acadSec, campusSec),
                List.of(),
                "User Message Question",
                100, // reserved completion
                300  // total budget -> prompt cap = 200
        );

        assertNotNull(result);
        assertTrue(result.getIncludedSections().contains(userSec));
        // Section 5 should be pruned first
        assertTrue(result.getSkippedSections().contains(campusSec));
    }

    @Test
    void testEvaluateBudget_DeterministicInclusion() {
        ContextSection sec1 = ContextSection.of("S1", "Content 1", "C1", 1, false);
        ContextSection sec2 = ContextSection.of("S2", "Content 2", "C2", 2, false);

        TokenBudgetResult result1 = budgetManager.evaluateBudget("Sys", List.of(sec1, sec2), null, "User", 100, 500);
        TokenBudgetResult result2 = budgetManager.evaluateBudget("Sys", List.of(sec1, sec2), null, "User", 100, 500);

        assertEquals(result1.getIncludedSections(), result2.getIncludedSections());
        assertEquals(result1.getSkippedSections(), result2.getSkippedSections());
        assertEquals(result1.getEstimatedPromptTokens(), result2.getEstimatedPromptTokens());
    }
}
