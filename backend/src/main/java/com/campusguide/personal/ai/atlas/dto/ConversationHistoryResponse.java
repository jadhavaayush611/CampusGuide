package com.campusguide.personal.ai.atlas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationHistoryResponse {
    private String conversationId;
    private String userId;
    private List<AtlasChatMessageDto> messages;
    private Integer totalMessages;
}
