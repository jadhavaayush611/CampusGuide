package com.campusguide.personal.ai.atlas.prompt;

import com.campusguide.personal.ai.atlas.prompt.model.PromptVersion;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PromptVersionTest {

    @Test
    void testPromptVersion_DefaultAndCustomFields() {
        PromptVersion pv = PromptVersion.builder()
                .version("1.0.0")
                .sectionsIncluded(List.of("--- USER PROFILE CONTEXT ---", "--- ACADEMIC CONTEXT ---"))
                .sectionsSkipped(List.of("--- CAMPUS CONTEXT ---"))
                .tokenEstimates(Map.of("systemPromptTokens", 50, "contextTokens", 120, "totalPromptTokens", 170))
                .build();

        assertNotNull(pv);
        assertEquals("1.0.0", pv.getVersion());
        assertEquals(2, pv.getSectionsIncluded().size());
        assertEquals(1, pv.getSectionsSkipped().size());
        assertEquals(170, pv.getTokenEstimates().get("totalPromptTokens"));
        assertNotNull(pv.getCreatedAt());
    }
}
