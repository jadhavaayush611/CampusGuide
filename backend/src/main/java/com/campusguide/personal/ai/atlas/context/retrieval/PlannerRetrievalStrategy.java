package com.campusguide.personal.ai.atlas.context.retrieval;

import com.campusguide.personal.ai.atlas.context.AtlasContext;
import com.campusguide.personal.ai.atlas.context.contributor.PlannerContributor;
import com.campusguide.personal.ai.atlas.context.query.EntityType;
import com.campusguide.personal.ai.atlas.context.query.ExtractedEntity;
import com.campusguide.personal.ai.atlas.context.query.QueryContext;
import com.campusguide.personal.ai.atlas.context.query.QueryDomain;
import com.campusguide.personal.ai.atlas.context.query.QueryIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Intelligent context retrieval strategy for Planner domain context.
 */
@Component
@RequiredArgsConstructor
public class PlannerRetrievalStrategy implements RetrievalStrategy {

    private final PlannerContributor plannerContributor;

    @Override
    public String getStrategyName() {
        return "planner";
    }

    @Override
    public boolean supports(QueryContext queryContext, RetrievalPolicy policy) {
        if (queryContext == null || queryContext.getDomainClassification() == QueryDomain.GENERAL || queryContext.getIntent() == QueryIntent.UNKNOWN) return true;
        if (queryContext.getDomainClassification() == QueryDomain.PLANNER || queryContext.getIntent() == QueryIntent.PLANNER_LOOKUP) {
            return true;
        }
        if (queryContext.getNormalizedQuery() != null) {
            String lower = queryContext.getNormalizedQuery().toLowerCase();
            if (lower.contains("focus") || lower.contains("task") || lower.contains("todo") || lower.contains("deadline") || lower.contains("priority") || lower.contains("pending") || lower.contains("due")) {
                return true;
            }
        }
        if (queryContext.getEntities() != null) {
            for (ExtractedEntity entity : queryContext.getEntities()) {
                if (entity.getType() == EntityType.PLANNER_ITEM) {
                    return true;
                }
            }
        }
        if (policy != null && policy.isEnableFallbackToAllIfLowConfidence() && queryContext.getConfidenceScore() <= policy.getMinConfidenceThreshold()) {
            return true;
        }
        return false;
    }

    @Override
    public double calculateRelevance(QueryContext queryContext) {
        if (queryContext == null) return 0.50;
        if (queryContext.getDomainClassification() == QueryDomain.PLANNER || queryContext.getIntent() == QueryIntent.PLANNER_LOOKUP) {
            return Math.min(1.0, queryContext.getConfidenceScore() + 0.20);
        }
        if (queryContext.getEntities() != null && queryContext.getEntities().stream().anyMatch(e -> e.getType() == EntityType.PLANNER_ITEM)) {
            return 0.75;
        }
        return 0.30;
    }

    @Override
    public void retrieve(RetrievalContext retrievalContext, AtlasContext atlasContext) {
        if (retrievalContext != null && retrievalContext.getRequest() != null && atlasContext != null) {
            plannerContributor.contribute(retrievalContext.getRequest(), atlasContext);
            if (atlasContext.getPlannerContext() != null) {
                retrievalContext.getRetrievedContributions().put(getStrategyName(), atlasContext.getPlannerContext());
            }
        }
    }
}
