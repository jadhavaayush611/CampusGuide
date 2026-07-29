package com.campusguide.personal.ai.atlas.context.retrieval;

import com.campusguide.personal.ai.atlas.context.AtlasContext;
import com.campusguide.personal.ai.atlas.context.contributor.CalendarContributor;
import com.campusguide.personal.ai.atlas.context.query.EntityType;
import com.campusguide.personal.ai.atlas.context.query.ExtractedEntity;
import com.campusguide.personal.ai.atlas.context.query.QueryContext;
import com.campusguide.personal.ai.atlas.context.query.QueryDomain;
import com.campusguide.personal.ai.atlas.context.query.QueryIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Intelligent context retrieval strategy for Calendar domain context.
 */
@Component
@RequiredArgsConstructor
public class CalendarRetrievalStrategy implements RetrievalStrategy {

    private final CalendarContributor calendarContributor;

    @Override
    public String getStrategyName() {
        return "calendar";
    }

    @Override
    public boolean supports(QueryContext queryContext, RetrievalPolicy policy) {
        if (queryContext == null || queryContext.getDomainClassification() == QueryDomain.GENERAL || queryContext.getIntent() == QueryIntent.UNKNOWN) return true;
        if (queryContext.getDomainClassification() == QueryDomain.CALENDAR || queryContext.getIntent() == QueryIntent.CALENDAR_EVENT) {
            return true;
        }
        if (queryContext.getTemporalInformation() != null && queryContext.getTemporalInformation().isResolved()) {
            return true;
        }
        if (queryContext.getNormalizedQuery() != null) {
            String lower = queryContext.getNormalizedQuery().toLowerCase();
            if (lower.contains("today") || lower.contains("tomorrow") || lower.contains("schedule") || lower.contains("calendar") || lower.contains("event") || lower.contains("meeting")) {
                return true;
            }
        }
        if (queryContext.getEntities() != null) {
            for (ExtractedEntity entity : queryContext.getEntities()) {
                if (entity.getType() == EntityType.CALENDAR_EVENT || entity.getType() == EntityType.TEMPORAL_EXPRESSION) {
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
        if (queryContext.getDomainClassification() == QueryDomain.CALENDAR || queryContext.getIntent() == QueryIntent.CALENDAR_EVENT) {
            return Math.min(1.0, queryContext.getConfidenceScore() + 0.20);
        }
        if (queryContext.getTemporalInformation() != null && queryContext.getTemporalInformation().isResolved()) {
            return 0.80;
        }
        return 0.30;
    }

    @Override
    public void retrieve(RetrievalContext retrievalContext, AtlasContext atlasContext) {
        if (retrievalContext != null && retrievalContext.getRequest() != null && atlasContext != null) {
            calendarContributor.contribute(retrievalContext.getRequest(), atlasContext);
            if (atlasContext.getCalendarContext() != null) {
                retrievalContext.getRetrievedContributions().put(getStrategyName(), atlasContext.getCalendarContext());
            }
        }
    }
}
