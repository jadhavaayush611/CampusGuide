package com.campusguide.personal.ai.atlas.context.evidence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Aggregated bundle of RetrievalEvidence associated with a target domain or context module.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvidenceBundle {

    @Builder.Default
    private String bundleId = UUID.randomUUID().toString();

    private String targetDomain;

    @Builder.Default
    private List<RetrievalEvidence> evidences = new ArrayList<>();

    @Builder.Default
    private EvidenceScore aggregateScore = new EvidenceScore();

    private double confidence;
    private String sourceSummary;

    @Builder.Default
    private long timestamp = System.currentTimeMillis();

    @Builder.Default
    private Map<String, Object> metadata = new LinkedHashMap<>();

    public void addEvidence(RetrievalEvidence evidence) {
        if (evidence != null) {
            this.evidences.add(evidence);
            recalculateAggregateScore();
        }
    }

    public void recalculateAggregateScore() {
        if (evidences.isEmpty()) return;
        double sumRel = 0, sumFresh = 0, sumConf = 0, sumAuth = 0, sumQual = 0;
        for (RetrievalEvidence e : evidences) {
            EvidenceScore s = e.getScore() != null ? e.getScore() : new EvidenceScore();
            sumRel += s.getRelevanceScore();
            sumFresh += s.getFreshnessScore();
            sumConf += s.getConfidenceScore();
            sumAuth += s.getSourceAuthorityScore();
            sumQual += s.getQualityScore();
        }
        int count = evidences.size();
        this.aggregateScore = EvidenceScore.builder()
                .relevanceScore(sumRel / count)
                .freshnessScore(sumFresh / count)
                .confidenceScore(sumConf / count)
                .sourceAuthorityScore(sumAuth / count)
                .qualityScore(sumQual / count)
                .build();
        this.aggregateScore.calculateOverallScore();
        this.confidence = this.aggregateScore.getConfidenceScore();
    }
}
