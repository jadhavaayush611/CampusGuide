package com.campusguide.personal.ai.atlas.context.retrieval;

import com.campusguide.personal.ai.atlas.context.AtlasContext;
import com.campusguide.personal.ai.atlas.context.contributor.UserProfileContributor;
import com.campusguide.personal.ai.atlas.context.query.QueryDomain;
import com.campusguide.personal.ai.atlas.context.query.QueryIntent;
import com.campusguide.personal.ai.atlas.context.query.QueryContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Intelligent context retrieval strategy for User Profile domain context.
 */
@Component
@RequiredArgsConstructor
public class UserRetrievalStrategy implements RetrievalStrategy {

    private final UserProfileContributor userProfileContributor;

    @Override
    public String getStrategyName() {
        return "user";
    }

    @Override
    public boolean supports(QueryContext queryContext, RetrievalPolicy policy) {
        if (policy != null && policy.isAlwaysRetrieveUserProfile()) {
            return true;
        }
        if (queryContext == null) {
            return true;
        }
        return queryContext.getDomainClassification() == QueryDomain.USER
                || queryContext.getIntent() == QueryIntent.USER_PROFILE
                || (policy != null && policy.isEnableFallbackToAllIfLowConfidence() && queryContext.getConfidenceScore() < policy.getMinConfidenceThreshold());
    }

    @Override
    public double calculateRelevance(QueryContext queryContext) {
        if (queryContext == null) return 0.85;
        if (queryContext.getDomainClassification() == QueryDomain.USER || queryContext.getIntent() == QueryIntent.USER_PROFILE) {
            return 1.0;
        }
        return 0.85; // Baseline high priority for User Profile
    }

    @Override
    public void retrieve(RetrievalContext retrievalContext, AtlasContext atlasContext) {
        if (retrievalContext != null && retrievalContext.getRequest() != null && atlasContext != null) {
            userProfileContributor.contribute(retrievalContext.getRequest(), atlasContext);
            if (atlasContext.getUserContext() != null) {
                retrievalContext.getRetrievedContributions().put(getStrategyName(), atlasContext.getUserContext());
            }
        }
    }
}
