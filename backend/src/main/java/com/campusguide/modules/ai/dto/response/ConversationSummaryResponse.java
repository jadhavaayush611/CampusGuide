package com.campusguide.modules.ai.dto.response;

import com.campusguide.modules.ai.enums.ConversationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationSummaryResponse {
    private String id;
    private String title;
    private ConversationType type;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
