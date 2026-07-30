package com.campusguide.personal.ai.atlas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationSummaryResponse {
    private String conversationId;
    private String title;
    private String summary;
    private List<String> keyTopics;
    private int messageCount;
    private Instant lastMessageTimestamp;
    private String status;
}
