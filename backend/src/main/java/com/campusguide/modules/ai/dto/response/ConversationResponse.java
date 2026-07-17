package com.campusguide.modules.ai.dto.response;

import com.campusguide.modules.ai.enums.ConversationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
    private String id;
    private String userId;
    private String title;
    private ConversationType type;
    private Map<String, Object> metadata;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
