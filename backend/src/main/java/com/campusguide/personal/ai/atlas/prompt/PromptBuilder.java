package com.campusguide.personal.ai.atlas.prompt;

import com.campusguide.personal.ai.atlas.config.AtlasProperties;
import com.campusguide.personal.ai.atlas.model.AtlasChatMessage;
import com.campusguide.personal.ai.atlas.model.AtlasPrompt;
import com.campusguide.personal.ai.atlas.model.AtlasRole;
import com.campusguide.personal.ai.atlas.prompt.budget.TokenBudgetManager;
import com.campusguide.personal.ai.atlas.prompt.budget.TokenBudgetResult;
import com.campusguide.personal.ai.atlas.prompt.model.ContextSection;
import com.campusguide.personal.ai.atlas.prompt.model.PromptVersion;
import com.campusguide.personal.ai.atlas.prompt.persona.CampusGuideAssistantPersona;
import com.campusguide.personal.ai.atlas.util.AtlasUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Assembles system prompts, instruction layers, context sections, conversation history,
 * and user messages into an AtlasPrompt object.
 *
 * <p>PromptBuilder does not consume AtlasContext directly, interacting instead through
 * modular List&lt;ContextSection&gt; inputs processed by ContextSectionAssembler.</p>
 */
@Component("atlasPromptBuilder")
@RequiredArgsConstructor
@Slf4j
public class PromptBuilder {

    private final AtlasProperties atlasProperties;
    private final CampusGuideAssistantPersona persona;
    private final TokenBudgetManager tokenBudgetManager;
    private final PromptTemplate promptTemplate;

    /**
     * Assembles an AtlasPrompt from modular ContextSections, instruction layers, conversation history,
     * and user message while enforcing token budget limits and versioning metadata.
     */
    public AtlasPrompt buildPrompt(
            String userMessage,
            String systemPromptOverride,
            List<AtlasChatMessage> conversationHistory,
            List<ContextSection> contextSections,
            Map<String, Object> contextPlaceholders,
            String model,
            Double temperature,
            Integer maxTokens) {

        String baseSystemPrompt = resolveBaseSystemPrompt(systemPromptOverride);

        // 1. Token Budgeting & Section Selection
        TokenBudgetResult budgetResult = tokenBudgetManager.evaluateBudget(
                baseSystemPrompt,
                contextSections,
                conversationHistory,
                userMessage,
                maxTokens,
                atlasProperties.getPromptTokenBudgetCap()
        );

        // 2. System Prompt & User Message Rendering
        String effectiveSystemPrompt = promptTemplate.renderSystemPrompt(
                baseSystemPrompt,
                budgetResult.getIncludedSections(),
                contextPlaceholders
        );

        String effectiveUserMessage = promptTemplate.renderUserMessage(
                userMessage,
                contextPlaceholders
        );

        // 3. Messages Formatting
        List<AtlasChatMessage> historyList = budgetResult.getIncludedHistory();
        List<AtlasChatMessage> formattedMessages = new ArrayList<>();

        // 3.1 System Prompt Message
        formattedMessages.add(AtlasChatMessage.builder()
                .role(AtlasRole.SYSTEM)
                .content(effectiveSystemPrompt)
                .timestamp(LocalDateTime.now())
                .build());

        // 3.2 Conversation History
        for (AtlasChatMessage msg : historyList) {
            if (msg.getContent() != null && !msg.getContent().isBlank()) {
                formattedMessages.add(msg);
            }
        }

        // 3.3 Current User Message
        if (effectiveUserMessage != null && !effectiveUserMessage.isBlank()) {
            formattedMessages.add(AtlasChatMessage.builder()
                    .role(AtlasRole.USER)
                    .content(effectiveUserMessage)
                    .timestamp(LocalDateTime.now())
                    .build());
        }

        // 4. Diagnostic Prompt Version Metadata Construction
        int systemTokens = AtlasUtils.estimateTokens(effectiveSystemPrompt);
        int contextTokens = budgetResult.getIncludedSections().stream()
                .mapToInt(ContextSection::getEstimatedTokens).sum();
        int historyTokens = historyList.stream()
                .mapToInt(m -> AtlasUtils.estimateTokens(m.getContent())).sum();
        int userTokens = AtlasUtils.estimateTokens(effectiveUserMessage);

        PromptVersion promptVersion = promptTemplate.buildPromptVersion(
                budgetResult.getIncludedSections(),
                budgetResult.getSkippedSections(),
                systemTokens,
                contextTokens,
                historyTokens,
                userTokens
        );

        // 5. Structured Logging (NEVER LOG PROMPT CONTENT)
        logStructuredBudget(promptVersion, budgetResult);

        return AtlasPrompt.builder()
                .systemPrompt(effectiveSystemPrompt)
                .conversationHistory(historyList)
                .userMessage(effectiveUserMessage)
                .contextPlaceholders(contextPlaceholders)
                .model(model)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .formattedMessages(formattedMessages)
                .promptVersion(promptVersion)
                .build();
    }

    /**
     * Convenience overload assembling an AtlasPrompt without context sections.
     */
    public AtlasPrompt buildPrompt(
            String userMessage,
            String systemPrompt,
            List<AtlasChatMessage> conversationHistory,
            Map<String, Object> contextPlaceholders,
            String model,
            Double temperature,
            Integer maxTokens) {
        return buildPrompt(
                userMessage,
                systemPrompt,
                conversationHistory,
                List.of(),
                contextPlaceholders,
                model,
                temperature,
                maxTokens
        );
    }

    private String resolveBaseSystemPrompt(String systemPromptOverride) {
        if (systemPromptOverride != null && !systemPromptOverride.isBlank()) {
            return systemPromptOverride;
        }
        if (persona != null) {
            String personaPrompt = persona.renderPersonaBase();
            if (personaPrompt != null && !personaPrompt.isBlank()) {
                return personaPrompt;
            }
        }
        return atlasProperties != null ? atlasProperties.getDefaultSystemPrompt() : "You are Atlas AI assistant.";
    }

    private void logStructuredBudget(PromptVersion promptVersion, TokenBudgetResult budgetResult) {
        log.info("Atlas Prompt Assembled - version: {}, budgetUsage: [used: {}/{} tokens, reservedCompletion: {}], sectionsIncluded: {}, sectionsSkipped: {}, estimatedPromptTokens: {}",
                promptVersion.getVersion(),
                budgetResult.getBudgetUsed(),
                budgetResult.getMaxPromptBudget(),
                budgetResult.getReservedCompletionTokens(),
                promptVersion.getSectionsIncluded(),
                promptVersion.getSectionsSkipped(),
                budgetResult.getEstimatedPromptTokens());
    }
}
