package com.campusguide.personal.ai.atlas.prompt.budget;

import com.campusguide.personal.ai.atlas.model.AtlasChatMessage;
import com.campusguide.personal.ai.atlas.prompt.model.ContextSection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenBudgetResult {
    @Builder.Default
    private List<ContextSection> includedSections = new ArrayList<>();

    @Builder.Default
    private List<ContextSection> skippedSections = new ArrayList<>();

    @Builder.Default
    private List<AtlasChatMessage> includedHistory = new ArrayList<>();

    private int estimatedPromptTokens;
    private int budgetUsed;
    private int maxPromptBudget;
    private int reservedCompletionTokens;
}
