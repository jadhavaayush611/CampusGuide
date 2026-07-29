package com.campusguide.personal.ai.atlas.benchmark;

import com.campusguide.personal.ai.atlas.config.AtlasProperties;
import com.campusguide.personal.ai.atlas.model.AtlasPrompt;
import com.campusguide.personal.ai.atlas.prompt.PromptBuilder;
import com.campusguide.personal.ai.atlas.prompt.PromptTemplate;
import com.campusguide.personal.ai.atlas.prompt.budget.TokenBudgetManager;
import com.campusguide.personal.ai.atlas.prompt.persona.CampusGuideAssistantPersona;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PromptAssemblyBenchmarkTest {

    private PromptBuilder promptBuilder;

    @BeforeEach
    void setUp() {
        AtlasProperties properties = new AtlasProperties();
        CampusGuideAssistantPersona persona = new CampusGuideAssistantPersona(List.of());
        TokenBudgetManager budgetManager = new TokenBudgetManager();
        PromptTemplate template = new PromptTemplate();
        promptBuilder = new PromptBuilder(properties, persona, budgetManager, template);
    }

    @Test
    void benchmarkPromptAssemblyLatency() {
        int iterations = 100;
        long startTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            AtlasPrompt prompt = promptBuilder.buildPrompt(
                    "What courses should I take next semester?",
                    null,
                    List.of(),
                    List.of(),
                    Map.of("userName", "Student" + i),
                    "gpt-4o-mini",
                    0.7,
                    500
            );
            assertNotNull(prompt);
        }

        long totalTimeNs = System.nanoTime() - startTime;
        double avgTimeMs = (totalTimeNs / 1_000_000.0) / iterations;

        System.out.println("Average Prompt Assembly Latency: " + avgTimeMs + " ms over " + iterations + " iterations.");
        assertTrue(avgTimeMs < 50.0, "Prompt assembly average latency should be under 50ms");
    }
}
