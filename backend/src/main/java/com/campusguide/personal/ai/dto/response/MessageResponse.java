package com.campusguide.personal.ai.dto.response;

import com.campusguide.personal.ai.enums.MessageRole;
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
public class MessageResponse {
    private String id;
    private String conversationId;
    private MessageRole role;
    private String content;
    private Map<String, Object> metadata;
    private Instant timestamp;
}
