package com.campusguide.modules.ai.dto.response;

import com.campusguide.modules.ai.enums.AiProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String assistantMessage;
    private String conversationId;
    private String model;
    private AiProvider provider;
    private Double processingTime;
}
