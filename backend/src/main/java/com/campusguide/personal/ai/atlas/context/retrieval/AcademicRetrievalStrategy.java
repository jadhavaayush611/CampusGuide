package com.campusguide.personal.ai.atlas.context.retrieval;

import com.campusguide.personal.ai.atlas.context.AtlasContext;
import com.campusguide.personal.ai.atlas.context.contributor.AcademicContributor;
import com.campusguide.personal.ai.atlas.context.query.EntityType;
import com.campusguide.personal.ai.atlas.context.query.ExtractedEntity;
import com.campusguide.personal.ai.atlas.context.query.QueryContext;
import com.campusguide.personal.ai.atlas.context.query.QueryDomain;
import com.campusguide.personal.ai.atlas.context.query.QueryIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Intelligent context retrieval strategy for Academic domain context.
 */
@Component
@RequiredArgsConstructor
public class AcademicRetrievalStrategy implements RetrievalStrategy {

    private final AcademicContributor academicContributor;

    @Override
    public String getStrategyName() {
        return "academic";
    }

    @Override
    public boolean supports(QueryContext queryContext, RetrievalPolicy policy) {
        if (queryContext == null || queryContext.getDomainClassification() == QueryDomain.GENERAL || queryContext.getIntent() == QueryIntent.UNKNOWN) return true;
        if (queryContext.getDomainClassification() == QueryDomain.ACADEMIC || queryContext.getIntent() == QueryIntent.ACADEMIC_INQUIRY) {
            return true;
        }
        if (queryContext.getNormalizedQuery() != null) {
            String lower = queryContext.getNormalizedQuery().toLowerCase();
            if (lower.contains("course") || lower.contains("grade") || lower.contains("gpa") || lower.contains("department") || lower.contains("exam") || lower.contains("assignment") || lower.contains("homework")) {
                return true;
            }
        }
        if (queryContext.getEntities() != null) {
            for (ExtractedEntity entity : queryContext.getEntities()) {
                if (entity.getType() == EntityType.ACADEMIC_CONCEPT) {
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
        if (queryContext.getDomainClassification() == QueryDomain.ACADEMIC || queryContext.getIntent() == QueryIntent.ACADEMIC_INQUIRY) {
            return Math.min(1.0, queryContext.getConfidenceScore() + 0.20);
        }
        if (queryContext.getEntities() != null && queryContext.getEntities().stream().anyMatch(e -> e.getType() == EntityType.ACADEMIC_CONCEPT)) {
            return 0.75;
        }
        return 0.30;
    }

    @Override
    public void retrieve(RetrievalContext retrievalContext, AtlasContext atlasContext) {
        if (retrievalContext != null && retrievalContext.getRequest() != null && atlasContext != null) {
            academicContributor.contribute(retrievalContext.getRequest(), atlasContext);
            if (atlasContext.getAcademicContext() != null) {
                retrievalContext.getRetrievedContributions().put(getStrategyName(), atlasContext.getAcademicContext());
            }
        }
    }
}
