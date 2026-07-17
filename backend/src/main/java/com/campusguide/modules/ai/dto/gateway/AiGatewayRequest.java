package com.campusguide.modules.ai.dto.gateway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiGatewayRequest {
    private String correlationId;
    private String conversationId;
    private String conversationType;
    private String userMessage;
    private List<GatewayMessage> conversationHistory;
    private Map<String, Object> metadata;
}
