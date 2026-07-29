package com.campusguide.personal.ai.atlas.prompt.budget;

import com.campusguide.personal.ai.atlas.model.AtlasChatMessage;
import com.campusguide.personal.ai.atlas.prompt.model.ContextSection;
import com.campusguide.personal.ai.atlas.util.AtlasUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Manages token budget for prompts by estimating prompt size, prioritizing required sections,
 * pruning optional sections, reserving completion tokens, and ensuring deterministic inclusion.
 */
@Component
@Slf4j
public class TokenBudgetManager {

    private static final int DEFAULT_MAX_PROMPT_BUDGET = 4096;
    private static final int DEFAULT_RESERVED_COMPLETION_TOKENS = 1024;

    /**
     * Evaluates prompt components against the token budget cap and returns a deterministic TokenBudgetResult.
     *
     * @param systemPromptBase base system prompt text (identity + instructions)
     * @param candidateSections list of candidate ContextSections
     * @param conversationHistory list of conversation history messages
     * @param userMessage current user message
     * @param maxTokens requested completion tokens limit (for reservation)
     * @param maxPromptBudget optional prompt token budget cap
     * @return TokenBudgetResult detailing included/skipped sections and token metrics
     */
    public TokenBudgetResult evaluateBudget(
            String systemPromptBase,
            List<ContextSection> candidateSections,
            List<AtlasChatMessage> conversationHistory,
            String userMessage,
            Integer maxTokens,
            Integer maxPromptBudget) {

        int reservedCompletion = (maxTokens != null && maxTokens > 0) ? maxTokens : DEFAULT_RESERVED_COMPLETION_TOKENS;
        int overallBudget = (maxPromptBudget != null && maxPromptBudget > 0) ? maxPromptBudget : DEFAULT_MAX_PROMPT_BUDGET;

        int netPromptCap = Math.max(1, overallBudget - reservedCompletion);

        int systemTokens = AtlasUtils.estimateTokens(systemPromptBase);
        int userTokens = AtlasUtils.estimateTokens(userMessage);
        int fixedBaseCost = systemTokens + userTokens;

        List<ContextSection> includedSections = new ArrayList<>();
        List<ContextSection> skippedSections = new ArrayList<>();
        List<AtlasChatMessage> includedHistory = new ArrayList<>();

        if (candidateSections == null) {
            candidateSections = List.of();
        }

        // Separate required vs optional sections
        List<ContextSection> requiredSections = candidateSections.stream()
                .filter(ContextSection::isRequired)
                .sorted(Comparator.comparingInt(ContextSection::getPriority))
                .toList();

        List<ContextSection> optionalSections = candidateSections.stream()
                .filter(s -> !s.isRequired())
                .sorted(Comparator.comparingInt(ContextSection::getPriority))
                .toList();

        int currentRunningCost = fixedBaseCost;

        // 1. Process Required Sections
        for (ContextSection section : requiredSections) {
            currentRunningCost += section.getEstimatedTokens();
            includedSections.add(section);
        }

        // 2. Deterministically Include Optional Sections (by priority order: 1 < 2 < 3 < 4 < 5)
        for (ContextSection section : optionalSections) {
            if (currentRunningCost + section.getEstimatedTokens() <= netPromptCap) {
                currentRunningCost += section.getEstimatedTokens();
                includedSections.add(section);
            } else {
                skippedSections.add(section);
                log.debug("Token budget pruned optional context section: {}", section.getTitle());
            }
        }

        // 3. Process History Messages (Newest to Oldest) within remaining capacity
        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            List<AtlasChatMessage> tempHistory = new ArrayList<>();
            for (int i = conversationHistory.size() - 1; i >= 0; i--) {
                AtlasChatMessage msg = conversationHistory.get(i);
                int msgTokens = AtlasUtils.estimateTokens(msg.getContent());
                if (currentRunningCost + msgTokens <= netPromptCap) {
                    currentRunningCost += msgTokens;
                    tempHistory.add(0, msg);
                } else {
                    log.debug("Token budget omitted older history message to fit cap.");
                    break;
                }
            }
            includedHistory = tempHistory;
        }

        return TokenBudgetResult.builder()
                .includedSections(includedSections)
                .skippedSections(skippedSections)
                .includedHistory(includedHistory)
                .estimatedPromptTokens(currentRunningCost)
                .budgetUsed(currentRunningCost)
                .maxPromptBudget(netPromptCap)
                .reservedCompletionTokens(reservedCompletion)
                .build();
    }
}
