package com.campusguide.personal.ai.atlas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationResponse {
    private String id;
    private String userId;
    private String title;
    private String type;
    private String status;
    private Integer messageCount;
    private Instant createdAt;
    private Instant updatedAt;
    private Map<String, Object> metadata;
}
