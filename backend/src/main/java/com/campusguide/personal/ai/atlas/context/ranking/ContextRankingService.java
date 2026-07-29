package com.campusguide.personal.ai.atlas.context.ranking;

import com.campusguide.personal.ai.atlas.context.AtlasContext;
import com.campusguide.personal.ai.atlas.context.query.QueryContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Ranks retrieved context domains deterministically based on composite relevance scores.
 */
@Component
@RequiredArgsConstructor
public class ContextRankingService {

    private final RelevanceScorer relevanceScorer;

    /**
     * Ranks all populated context models inside AtlasContext according to QueryContext relevance.
     *
     * @param atlasContext aggregate context model
     * @param queryContext structured query context
     * @return sorted list of ContextScores in deterministic descending order
     */
    public List<ContextScore> rankContexts(AtlasContext atlasContext, QueryContext queryContext) {
        if (atlasContext == null) {
            return Collections.emptyList();
        }

        List<ContextScore> scores = new ArrayList<>();

        if (atlasContext.getUserContext() != null) {
            scores.add(relevanceScorer.scoreContext("userProfile", atlasContext.getUserContext(), queryContext));
        }
        if (atlasContext.getAcademicContext() != null) {
            scores.add(relevanceScorer.scoreContext("academic", atlasContext.getAcademicContext(), queryContext));
        }
        if (atlasContext.getPlannerContext() != null) {
            scores.add(relevanceScorer.scoreContext("planner", atlasContext.getPlannerContext(), queryContext));
        }
        if (atlasContext.getCalendarContext() != null) {
            scores.add(relevanceScorer.scoreContext("calendar", atlasContext.getCalendarContext(), queryContext));
        }
        if (atlasContext.getCampusContext() != null) {
            scores.add(relevanceScorer.scoreContext("campus", atlasContext.getCampusContext(), queryContext));
        }

        // Sort deterministically using ContextScore natural ordering (totalScore desc, priority desc, name asc)
        Collections.sort(scores);

        return scores;
    }
}
