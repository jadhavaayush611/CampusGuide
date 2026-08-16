package com.campusguide.personal.ai.atlas.context.retrieval;

import com.campusguide.personal.ai.atlas.context.AtlasContext;
import com.campusguide.personal.ai.atlas.context.contributor.CampusContributor;
import com.campusguide.personal.ai.atlas.context.query.EntityType;
import com.campusguide.personal.ai.atlas.context.query.ExtractedEntity;
import com.campusguide.personal.ai.atlas.context.query.QueryContext;
import com.campusguide.personal.ai.atlas.context.query.QueryDomain;
import com.campusguide.personal.ai.atlas.context.query.QueryIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Intelligent context retrieval strategy for Campus domain context.
 */
@Component
@RequiredArgsConstructor
public class CampusRetrievalStrategy implements RetrievalStrategy {

    private final CampusContributor campusContributor;

    @Override
    public String getStrategyName() {
        return "campus";
    }

    @Override
    public boolean supports(QueryContext queryContext, RetrievalPolicy policy) {
        if (queryContext == null || queryContext.getDomainClassification() == QueryDomain.GENERAL || queryContext.getIntent() == QueryIntent.UNKNOWN) return true;
        if (queryContext.getDomainClassification() == QueryDomain.CAMPUS || queryContext.getIntent() == QueryIntent.CAMPUS_NAVIGATION) {
            return true;
        }
        if (queryContext.getNormalizedQuery() != null) {
            String lower = queryContext.getNormalizedQuery().toLowerCase();
            if (lower.contains("campus") || lower.contains("library") || lower.contains("building") || 
                lower.contains("location") || lower.contains("cafeteria") || lower.contains("hall") || 
                lower.contains("dorm") || lower.contains("map") || lower.contains("floor") || 
                lower.contains("department") || lower.contains("dept") || lower.contains("office") || 
                lower.contains("room") || lower.contains("lift") || lower.contains("washroom") || 
                lower.contains("toilet") || lower.contains("stairway") || lower.contains("workshop") || 
                lower.contains("canteen") || lower.contains("common room") || lower.contains("auditorium") || 
                lower.contains("music room") || lower.contains("amphitheatre") || lower.contains("principal") || 
                lower.contains("cmpn") || lower.contains("aids") || lower.contains("it") || 
                lower.contains("extc") || lower.contains("auro") || lower.contains("ecs")) {
                return true;
            }
        }
        if (queryContext.getEntities() != null) {
            for (ExtractedEntity entity : queryContext.getEntities()) {
                if (entity.getType() == EntityType.CAMPUS_LOCATION) {
                    return true;
                }
            }
        }
        if (policy != null && policy.isEnableFallbackToAllIfLowConfidence() && queryContext.getConfidenceScore() <= 0.75) {
            return true;
        }
        return false;
    }

    @Override
    public double calculateRelevance(QueryContext queryContext) {
        if (queryContext == null) return 0.50;
        if (queryContext.getDomainClassification() == QueryDomain.CAMPUS || queryContext.getIntent() == QueryIntent.CAMPUS_NAVIGATION) {
            return Math.min(1.0, queryContext.getConfidenceScore() + 0.20);
        }
        if (queryContext.getEntities() != null && queryContext.getEntities().stream().anyMatch(e -> e.getType() == EntityType.CAMPUS_LOCATION)) {
            return 0.80;
        }
        return 0.30;
    }

    @Override
    public void retrieve(RetrievalContext retrievalContext, AtlasContext atlasContext) {
        if (retrievalContext != null && retrievalContext.getRequest() != null && atlasContext != null) {
            campusContributor.contribute(retrievalContext.getRequest(), atlasContext);
            if (atlasContext.getCampusContext() != null) {
                retrievalContext.getRetrievedContributions().put(getStrategyName(), atlasContext.getCampusContext());
            }
        }
    }
}
