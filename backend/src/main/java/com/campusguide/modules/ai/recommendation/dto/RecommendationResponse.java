package com.campusguide.modules.ai.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {
    private String id;
    private String title;
    private String description;
    private RecommendationType recommendationType;
    private RecommendationSource recommendationSource;
    private RecommendationReason reasonCode;
    private Double score;
    private String explanation;
    private Map<String, Object> metadata;
}
