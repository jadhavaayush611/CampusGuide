package com.campusguide.personal.ai.atlas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtlasChatResponse {
    private String id;
    private String conversationId;
    private String content;
    private String role;
    private String model;
    private String finishReason;
    private AtlasUsageDto usage;
    private LocalDateTime timestamp;
    private Map<String, Object> metadata;
}
