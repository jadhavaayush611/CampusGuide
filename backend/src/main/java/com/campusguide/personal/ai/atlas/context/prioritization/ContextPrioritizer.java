package com.campusguide.personal.ai.atlas.context.prioritization;

import com.campusguide.personal.ai.atlas.context.AtlasContext;
import com.campusguide.personal.ai.atlas.context.evidence.EvidenceBundle;
import com.campusguide.personal.ai.atlas.context.evidence.EvidenceScore;
import com.campusguide.personal.ai.atlas.context.query.QueryContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * ContextPrioritizer ranks context entries and evidence bundles using a composite metric:
 * relevance, freshness, confidence, completeness, evidence strength, and source authority.
 */
@Component
@Slf4j
public class ContextPrioritizer {

    /**
     * Prioritizes evidence bundles in AtlasContext and produces ranking decisions.
     */
    public List<PrioritizationDecision> prioritize(AtlasContext context, QueryContext queryContext, int maxTopEntries) {
        List<PrioritizationDecision> decisions = new ArrayList<>();
        Map<String, EvidenceBundle> bundleMap = context.getEvidenceBundles();

        if (bundleMap == null || bundleMap.isEmpty()) {
            return decisions;
        }

        List<PrioritizationDecision> candidateDecisions = new ArrayList<>();

        bundleMap.forEach((domain, bundle) -> {
            EvidenceScore aggScore = bundle.getAggregateScore();

            double relevance = aggScore != null ? aggScore.getRelevanceScore() : 0.5;
            double freshness = aggScore != null ? aggScore.getFreshnessScore() : 0.5;
            double confidence = bundle.getConfidence() > 0 ? bundle.getConfidence() : (aggScore != null ? aggScore.getConfidenceScore() : 0.5);
            double completeness = calculateCompleteness(bundle);
            double evidenceStrength = aggScore != null ? aggScore.getOverallScore() : 0.5;
            double sourceAuthority = aggScore != null ? aggScore.getSourceAuthorityScore() : 0.5;

            // Composite formula:
            // RankScore = (relevance * 0.25) + (confidence * 0.20) + (evidenceStrength * 0.20)
            //             + (sourceAuthority * 0.15) + (freshness * 0.10) + (completeness * 0.10)
            double finalRankScore = (relevance * 0.25)
                    + (confidence * 0.20)
                    + (evidenceStrength * 0.20)
                    + (sourceAuthority * 0.15)
                    + (freshness * 0.10)
                    + (completeness * 0.10);

            candidateDecisions.add(PrioritizationDecision.builder()
                    .key("bundle:" + domain)
                    .targetDomain(domain)
                    .relevance(relevance)
                    .freshness(freshness)
                    .confidence(confidence)
                    .completeness(completeness)
                    .evidenceStrength(evidenceStrength)
                    .sourceAuthority(sourceAuthority)
                    .finalRankScore(finalRankScore)
                    .kept(true)
                    .reason("Evaluated bundle rank score")
                    .build());
        });

        // Deterministic sorting: by finalRankScore desc, then targetDomain asc
        candidateDecisions.sort(Comparator
                .comparing(PrioritizationDecision::getFinalRankScore, Comparator.reverseOrder())
                .thenComparing(PrioritizationDecision::getTargetDomain));

        // Assign ranks and prune if exceeding maxTopEntries
        for (int i = 0; i < candidateDecisions.size(); i++) {
            PrioritizationDecision dec = candidateDecisions.get(i);
            dec.setRank(i + 1);
            if (maxTopEntries > 0 && (i + 1) > maxTopEntries) {
                dec.setKept(false);
                dec.setReason(String.format("Pruned because rank %d exceeded max allowed budget of %d", i + 1, maxTopEntries));
            } else {
                dec.setKept(true);
            }
            decisions.add(dec);
        }

        return decisions;
    }

    private double calculateCompleteness(EvidenceBundle bundle) {
        if (bundle == null || bundle.getEvidences() == null || bundle.getEvidences().isEmpty()) {
            return 0.2;
        }
        int totalSnippets = bundle.getEvidences().size();
        long nonBlankCount = bundle.getEvidences().stream()
                .filter(e -> e.getContentSnippet() != null && !e.getContentSnippet().isBlank())
                .count();
        return Math.min(1.0, (double) nonBlankCount / Math.max(1, totalSnippets));
    }
}
