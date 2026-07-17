package com.campusguide.modules.ai.dto.gateway;

import com.campusguide.modules.ai.enums.AiProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiGatewayResponse {
    private String response;
    private String model;
    private AiProvider provider;
    private Integer tokensUsed;
    private Double processingTime;
    private Map<String, Object> metadata;
}
