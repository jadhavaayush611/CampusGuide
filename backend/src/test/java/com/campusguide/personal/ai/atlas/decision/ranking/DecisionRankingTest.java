package com.campusguide.personal.ai.atlas.decision.ranking;

import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;
import com.campusguide.personal.ai.atlas.decision.utility.UtilityScore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DecisionRankingTest {

    private DecisionRanker ranker;

    @BeforeEach
    void setUp() {
        DeterministicRankingStrategy strategy = new DeterministicRankingStrategy();
        ranker = new DecisionRanker(strategy);
    }

    @Test
    @DisplayName("DecisionRanker ranks candidates deterministically based on normalized utility and confidence tie-breaker")
    void testDeterministicRanking() {
        DecisionCandidate c1 = DecisionCandidate.simple("cand_b", "ACTION_1", "Low utility", 0.50);
        DecisionCandidate c2 = DecisionCandidate.simple("cand_a", "ACTION_2", "High utility", 0.90);

        UtilityScore u1 = UtilityScore.builder().candidateId("cand_b").normalizedUtility(0.50).build();
        UtilityScore u2 = UtilityScore.builder().candidateId("cand_a").normalizedUtility(0.90).build();

        Map<String, UtilityScore> utilities = Map.of("cand_b", u1, "cand_a", u2);

        DecisionRanking ranking = ranker.rankCandidates(List.of(c1, c2), Map.of(), utilities);

        assertNotNull(ranking);
        assertEquals(c2, ranking.getTopCandidate());
        assertEquals(1, ranking.getCandidateRanks().get("cand_a"));
        assertEquals(2, ranking.getCandidateRanks().get("cand_b"));
    }

    @Test
    @DisplayName("DecisionRanker uses candidateId lexicographical tie-breaker when scores match")
    void testLexicographicalTieBreaking() {
        DecisionCandidate c1 = DecisionCandidate.simple("cand_z", "ACTION_1", "Equal", 0.80);
        DecisionCandidate c2 = DecisionCandidate.simple("cand_a", "ACTION_2", "Equal", 0.80);

        UtilityScore u1 = UtilityScore.builder().candidateId("cand_z").normalizedUtility(0.80).build();
        UtilityScore u2 = UtilityScore.builder().candidateId("cand_a").normalizedUtility(0.80).build();

        Map<String, UtilityScore> utilities = Map.of("cand_z", u1, "cand_a", u2);

        DecisionRanking ranking = ranker.rankCandidates(List.of(c1, c2), Map.of(), utilities);

        assertEquals("cand_a", ranking.getTopCandidate().getCandidateId());
        assertFalse(ranking.getTieBreakingLog().isEmpty());
    }
}
