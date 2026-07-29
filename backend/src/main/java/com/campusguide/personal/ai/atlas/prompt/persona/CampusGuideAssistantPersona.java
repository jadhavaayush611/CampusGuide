package com.campusguide.personal.ai.atlas.prompt.persona;

import com.campusguide.personal.ai.atlas.prompt.instruction.InstructionLayer;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Encapsulates Atlas's primary assistant persona defining tone, response philosophy,
 * academic assistance guidelines, campus guidance guidelines, formatting expectations,
 * refusal policy, and ordered instruction layers.
 */
@Component
@Getter
public class CampusGuideAssistantPersona {

    public static final String NAME = "CampusGuideAssistantPersona";

    private final String tone = "Supportive, professional, academic, clear, and encouraging.";
    private final String responsePhilosophy = "Deliver student-centric, highly relevant, accurate, and actionable guidance.";
    private final String academicAssistanceGuidelines = "Support academic progress, study organization, and course guidance without completing assignments or enabling academic dishonesty.";
    private final String campusGuidanceGuidelines = "Provide accurate information about campus locations, department events, notices, and schedule events.";
    private final String formattingExpectations = "Format responses using clean GitHub-flavored Markdown, clear headings, bullet points, and concise text.";
    private final String refusalPolicy = "Politely and firmly decline requests that involve cheating, policy violations, unsafe actions, or unverified private user data.";

    private final List<InstructionLayer> instructionLayers;

    @Autowired
    public CampusGuideAssistantPersona(List<InstructionLayer> instructionLayers) {
        if (instructionLayers != null) {
            this.instructionLayers = instructionLayers.stream()
                    .sorted(Comparator.comparingInt(InstructionLayer::getOrder))
                    .collect(Collectors.toList());
        } else {
            this.instructionLayers = List.of();
        }
    }

    /**
     * Renders the persona identity, guidelines, and ordered instruction layers into a cohesive system prompt baseline.
     */
    public String renderPersonaBase() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== CAMPUSGUIDE ASSISTANT PERSONA ===\n");
        sb.append("Tone: ").append(tone).append("\n");
        sb.append("Philosophy: ").append(responsePhilosophy).append("\n");
        sb.append("Academic Guidelines: ").append(academicAssistanceGuidelines).append("\n");
        sb.append("Campus Guidelines: ").append(campusGuidanceGuidelines).append("\n");
        sb.append("Formatting Expectations: ").append(formattingExpectations).append("\n");
        sb.append("Refusal Policy: ").append(refusalPolicy).append("\n\n");

        sb.append("=== SYSTEM INSTRUCTIONS ===\n");
        if (instructionLayers != null && !instructionLayers.isEmpty()) {
            for (InstructionLayer layer : instructionLayers) {
                sb.append("[").append(layer.getLayerName()).append("]: ")
                        .append(layer.renderInstruction()).append("\n");
            }
        }
        return sb.toString().trim();
    }
}
