package com.campusguide.personal.ai.atlas.context.retrieval;

import com.campusguide.personal.ai.atlas.context.AtlasContext;
import com.campusguide.personal.ai.atlas.context.query.QueryContext;

/**
 * Interface for selective domain context retrieval strategies driven by semantic query understanding.
 */
public interface RetrievalStrategy {

    /**
     * Unique identifier for this retrieval strategy (e.g. "user", "academic", "planner", "calendar", "campus").
     */
    String getStrategyName();

    /**
     * Determines whether this strategy should execute for the given query context and retrieval policy.
     */
    boolean supports(QueryContext queryContext, RetrievalPolicy policy);

    /**
     * Calculates relevance score of this strategy for ranking.
     */
    double calculateRelevance(QueryContext queryContext);

    /**
     * Executes context retrieval and enriches AtlasContext.
     */
    void retrieve(RetrievalContext retrievalContext, AtlasContext atlasContext);
}
