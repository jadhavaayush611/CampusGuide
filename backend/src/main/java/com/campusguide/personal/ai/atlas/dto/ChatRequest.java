package com.campusguide.personal.ai.atlas.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest extends AtlasChatRequest {

    @Size(max = 10, message = "Maximum 10 attachments allowed")
    private List<String> attachments;

    @Builder(builderMethodName = "chatRequestBuilder")
    public ChatRequest(
            String prompt,
            String conversationId,
            String systemPrompt,
            List<AtlasChatMessageDto> conversationHistory,
            Map<String, Object> contextPlaceholders,
            String model,
            Double temperature,
            Integer maxTokens,
            List<String> attachments
    ) {
        super(prompt, conversationId, systemPrompt, conversationHistory, contextPlaceholders, model, temperature, maxTokens);
        this.attachments = attachments;
    }
}
