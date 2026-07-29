package com.campusguide.personal.ai.atlas.prompt;

import com.campusguide.personal.ai.atlas.prompt.instruction.*;
import com.campusguide.personal.ai.atlas.prompt.persona.CampusGuideAssistantPersona;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CampusGuideAssistantPersonaTest {

    private CampusGuideAssistantPersona persona;

    @BeforeEach
    void setUp() {
        List<InstructionLayer> layers = List.of(
                new ResponsePolicyInstruction(),
                new CoreIdentityInstruction(),
                new SafetyInstruction(),
                new FormattingInstruction(),
                new CampusInstruction()
        );
        persona = new CampusGuideAssistantPersona(layers);
    }

    @Test
    void testPersonaAttributes_AreDefined() {
        assertNotNull(persona.getTone());
        assertNotNull(persona.getResponsePhilosophy());
        assertNotNull(persona.getAcademicAssistanceGuidelines());
        assertNotNull(persona.getCampusGuidanceGuidelines());
        assertNotNull(persona.getFormattingExpectations());
        assertNotNull(persona.getRefusalPolicy());
    }

    @Test
    void testInstructionLayers_AreSortedByOrder() {
        List<InstructionLayer> layers = persona.getInstructionLayers();
        assertEquals(5, layers.size());

        assertEquals(10, layers.get(0).getOrder());
        assertEquals("CoreIdentity", layers.get(0).getLayerName());

        assertEquals(20, layers.get(1).getOrder());
        assertEquals("Safety", layers.get(1).getLayerName());

        assertEquals(30, layers.get(2).getOrder());
        assertEquals("Campus", layers.get(2).getLayerName());

        assertEquals(40, layers.get(3).getOrder());
        assertEquals("Formatting", layers.get(3).getLayerName());

        assertEquals(50, layers.get(4).getOrder());
        assertEquals("ResponsePolicy", layers.get(4).getLayerName());
    }

    @Test
    void testRenderPersonaBase_IncludesPersonaAndOrderedInstructions() {
        String basePrompt = persona.renderPersonaBase();

        assertNotNull(basePrompt);
        assertTrue(basePrompt.contains("=== CAMPUSGUIDE ASSISTANT PERSONA ==="));
        assertTrue(basePrompt.contains("Tone:"));
        assertTrue(basePrompt.contains("=== SYSTEM INSTRUCTIONS ==="));
        assertTrue(basePrompt.contains("[CoreIdentity]:"));
        assertTrue(basePrompt.contains("[Safety]:"));
        assertTrue(basePrompt.contains("[Campus]:"));
        assertTrue(basePrompt.contains("[Formatting]:"));
        assertTrue(basePrompt.contains("[ResponsePolicy]:"));

        // Verify CoreIdentity appears before ResponsePolicy
        int coreIndex = basePrompt.indexOf("[CoreIdentity]");
        int responseIndex = basePrompt.indexOf("[ResponsePolicy]");
        assertTrue(coreIndex < responseIndex);
    }
}
