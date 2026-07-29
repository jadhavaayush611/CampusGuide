package com.campusguide.personal.ai.atlas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtlasChatRequest {

    @NotBlank(message = "Prompt message cannot be empty or blank")
    @Size(max = 4096, message = "Prompt exceeds maximum allowed length of 4096 characters")
    private String prompt;

    private String conversationId;
    private String systemPrompt;
    private List<AtlasChatMessageDto> conversationHistory;
    private Map<String, Object> contextPlaceholders;
    private String model;
    private Double temperature;
    private Integer maxTokens;
}
