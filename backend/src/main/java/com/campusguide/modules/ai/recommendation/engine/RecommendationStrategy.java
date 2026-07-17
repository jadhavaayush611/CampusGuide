package com.campusguide.modules.ai.recommendation.engine;

import com.campusguide.modules.ai.recommendation.dto.RecommendationResponse;
import com.campusguide.modules.ai.recommendation.dto.RecommendationType;
import com.campusguide.modules.ai.recommendation.dto.RecommendationUserContext;

import java.util.List;

public interface RecommendationStrategy {
    
    /**
     * Generates recommendations based on the user's context.
     *
     * @param context the user profile and data context
     * @return list of generated recommendations
     */
    List<RecommendationResponse> recommend(RecommendationUserContext context);

    /**
     * Returns the type of recommendations handled by this strategy.
     *
     * @return the recommendation type
     */
    RecommendationType getType();
}
