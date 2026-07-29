package com.campusguide.personal.ai.atlas.prompt;

import com.campusguide.personal.ai.atlas.prompt.model.ContextSection;
import com.campusguide.personal.ai.atlas.prompt.model.PromptVersion;
import com.campusguide.personal.ai.atlas.util.AtlasUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages system prompt template rendering, instruction composition, context section formatting,
 * placeholder substitution, and PromptVersion diagnostic metadata construction.
 */
@Component
public class PromptTemplate {

    /**
     * Renders effective system prompt by formatting base system prompt text, appending context sections,
     * and substituting placeholders.
     */
    public String renderSystemPrompt(
            String systemPromptBase,
            List<ContextSection> contextSections,
            Map<String, Object> placeholders) {

        String effectivePrompt = systemPromptBase != null ? systemPromptBase : "";

        if (placeholders != null && !placeholders.isEmpty()) {
            effectivePrompt = AtlasUtils.formatTemplate(effectivePrompt, placeholders);
        }

        if (contextSections != null && !contextSections.isEmpty()) {
            StringBuilder sb = new StringBuilder(effectivePrompt);
            sb.append("\n\nContext Information:\n");
            for (ContextSection section : contextSections) {
                sb.append(section.getTitle()).append("\n")
                        .append(section.getContent()).append("\n\n");
            }
            effectivePrompt = sb.toString().trim();
        }

        return effectivePrompt;
    }

    /**
     * Renders user message text by evaluating context placeholders.
     */
    public String renderUserMessage(String userMessage, Map<String, Object> placeholders) {
        if (userMessage == null || placeholders == null || placeholders.isEmpty()) {
            return userMessage;
        }
        return AtlasUtils.formatTemplate(userMessage, placeholders);
    }

    /**
     * Constructs diagnostic PromptVersion metadata.
     */
    public PromptVersion buildPromptVersion(
            List<ContextSection> includedSections,
            List<ContextSection> skippedSections,
            int systemTokens,
            int contextTokens,
            int historyTokens,
            int userTokens) {

        List<String> includedTitles = (includedSections != null) ?
                includedSections.stream().map(ContextSection::getTitle).toList() : List.of();
        List<String> skippedTitles = (skippedSections != null) ?
                skippedSections.stream().map(ContextSection::getTitle).toList() : List.of();

        Map<String, Integer> tokenEstimates = new HashMap<>();
        tokenEstimates.put("systemPromptTokens", systemTokens);
        tokenEstimates.put("contextTokens", contextTokens);
        tokenEstimates.put("historyTokens", historyTokens);
        tokenEstimates.put("userMessageTokens", userTokens);
        tokenEstimates.put("totalPromptTokens", systemTokens + contextTokens + historyTokens + userTokens);

        return PromptVersion.builder()
                .version(PromptVersion.CURRENT_VERSION)
                .sectionsIncluded(includedTitles)
                .sectionsSkipped(skippedTitles)
                .tokenEstimates(tokenEstimates)
                .createdAt(Instant.now())
                .build();
    }
}
