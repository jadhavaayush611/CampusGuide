package com.campusguide.personal.ai.atlas.model;

import com.campusguide.personal.ai.atlas.prompt.model.PromptVersion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtlasPrompt {
    private String systemPrompt;
    @Builder.Default
    private List<AtlasChatMessage> conversationHistory = new ArrayList<>();
    private String userMessage;
    @Builder.Default
    private Map<String, Object> contextPlaceholders = new HashMap<>();
    private String model;
    private Double temperature;
    private Integer maxTokens;

    @Builder.Default
    private List<AtlasChatMessage> formattedMessages = new ArrayList<>();

    private PromptVersion promptVersion;
}
