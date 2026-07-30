package com.campusguide.personal.ai.atlas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ChatResponse extends AtlasChatResponse {

    @Builder(builderMethodName = "chatResponseBuilder")
    public ChatResponse(
            String id,
            String conversationId,
            String content,
            String role,
            String model,
            String finishReason,
            AtlasUsageDto usage,
            LocalDateTime timestamp,
            Map<String, Object> metadata
    ) {
        super(id, conversationId, content, role, model, finishReason, usage, timestamp, metadata);
    }
}
