package com.campusguide.personal.ai.atlas.context.ranking;

import com.campusguide.personal.ai.atlas.context.AtlasContext;
import com.campusguide.personal.ai.atlas.context.model.AcademicContext;
import com.campusguide.personal.ai.atlas.context.model.CampusContext;
import com.campusguide.personal.ai.atlas.context.model.UserContext;
import com.campusguide.personal.ai.atlas.context.query.QueryContext;
import com.campusguide.personal.ai.atlas.context.query.QueryDomain;
import com.campusguide.personal.ai.atlas.context.query.QueryIntent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContextRankingServiceTest {

    private ContextRankingService rankingService;

    @BeforeEach
    void setUp() {
        rankingService = new ContextRankingService(new RelevanceScorer());
    }

    @Test
    @DisplayName("Should rank campus domain context highest when query intent is CAMPUS_NAVIGATION")
    void testRankContexts_CampusIntent() {
        AtlasContext atlasContext = AtlasContext.builder()
                .userContext(UserContext.builder().name("John Doe").summary("Student").build())
                .campusContext(CampusContext.builder().location("Library").summary("Main campus library").build())
                .academicContext(AcademicContext.builder().department("CS").summary("Computer Science").build())
                .build();

        QueryContext queryContext = QueryContext.builder()
                .domainClassification(QueryDomain.CAMPUS)
                .intent(QueryIntent.CAMPUS_NAVIGATION)
                .confidenceScore(0.90)
                .build();

        List<ContextScore> scores = rankingService.rankContexts(atlasContext, queryContext);

        assertFalse(scores.isEmpty());
        // Campus should rank top or high due to intent match
        assertEquals("campus", scores.get(0).getContributorName());
        assertTrue(scores.get(0).getTotalScore() > scores.get(1).getTotalScore() || scores.get(0).getSourcePriority() >= scores.get(1).getSourcePriority());
    }

    @Test
    @DisplayName("Should produce deterministic ordering when sorting context scores")
    void testRankContexts_DeterministicOrdering() {
        AtlasContext atlasContext = AtlasContext.builder()
                .userContext(UserContext.builder().name("John Doe").build())
                .academicContext(AcademicContext.builder().department("Physics").build())
                .build();

        QueryContext qc = QueryContext.builder().domainClassification(QueryDomain.GENERAL).intent(QueryIntent.GENERAL_CONVERSATION).confidenceScore(0.50).build();

        List<ContextScore> run1 = rankingService.rankContexts(atlasContext, qc);
        List<ContextScore> run2 = rankingService.rankContexts(atlasContext, qc);

        assertEquals(run1.size(), run2.size());
        for (int i = 0; i < run1.size(); i++) {
            assertEquals(run1.get(i).getContributorName(), run2.get(i).getContributorName());
            assertEquals(run1.get(i).getTotalScore(), run2.get(i).getTotalScore());
        }
    }
}
