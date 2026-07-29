package com.campusguide.personal.ai.atlas.context.fusion;

import com.campusguide.personal.ai.atlas.context.evidence.EvidenceScore;
import com.campusguide.personal.ai.atlas.context.evidence.EvidenceSource;
import com.campusguide.personal.ai.atlas.context.evidence.RetrievalEvidence;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves conflicting context entries and evidence items using multi-criteria scoring:
 * freshness, confidence, source priority, and evidence quality.
 */
@Component
@Slf4j
public class ConflictResolver {

    /**
     * Compares two pieces of evidence for the same entity or field and resolves which takes precedence.
     */
    public ConflictResolution resolve(RetrievalEvidence e1, RetrievalEvidence e2) {
        if (e1 == null && e2 == null) return null;
        if (e1 == null) {
            return createResolution(e2, e1, e2.getSource(), null, 1.0, 0.0, "Second evidence present while first was null");
        }
        if (e2 == null) {
            return createResolution(e1, e2, e1.getSource(), null, 1.0, 0.0, "First evidence present while second was null");
        }

        double score1 = calculateConflictScore(e1);
        double score2 = calculateConflictScore(e2);

        String entityKey = e1.getEntityKey() != null ? e1.getEntityKey() : e2.getEntityKey();
        String domain = e1.getMetadata() != null ? (String) e1.getMetadata().getOrDefault("domain", "general") : "general";

        if (score1 >= score2) {
            String reason = String.format("Evidence 1 won (score: %.3f vs %.3f) due to higher source authority, freshness, or confidence", score1, score2);
            return ConflictResolution.builder()
                    .entityKey(entityKey)
                    .targetDomain(domain)
                    .winningValue(e1.getContentSnippet())
                    .losingValue(e2.getContentSnippet())
                    .winningSource(e1.getSource())
                    .losingSource(e2.getSource())
                    .winningScore(score1)
                    .losingScore(score2)
                    .resolutionReason(reason)
                    .timestamp(System.currentTimeMillis())
                    .build();
        } else {
            String reason = String.format("Evidence 2 won (score: %.3f vs %.3f) due to higher source authority, freshness, or confidence", score2, score1);
            return ConflictResolution.builder()
                    .entityKey(entityKey)
                    .targetDomain(domain)
                    .winningValue(e2.getContentSnippet())
                    .losingValue(e1.getContentSnippet())
                    .winningSource(e2.getSource())
                    .losingSource(e1.getSource())
                    .winningScore(score2)
                    .losingScore(score1)
                    .resolutionReason(reason)
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }

    /**
     * Calculates composite conflict resolution score incorporating:
     * - Source Priority (40%)
     * - Freshness (25%)
     * - Confidence (20%)
     * - Evidence Quality (15%)
     */
    public double calculateConflictScore(RetrievalEvidence evidence) {
        if (evidence == null) return 0.0;

        EvidenceSource source = evidence.getSource() != null ? evidence.getSource() : EvidenceSource.HEURISTIC;
        double sourceWeight = source.getPriorityWeight() / 100.0;

        EvidenceScore es = evidence.getScore();
        double freshness = es != null ? es.getFreshnessScore() : calculateFreshnessFromTimestamp(evidence.getTimestamp());
        double confidence = es != null ? es.getConfidenceScore() : 0.5;
        double quality = es != null ? es.getQualityScore() : 0.5;

        return (sourceWeight * 0.40) + (freshness * 0.25) + (confidence * 0.20) + (quality * 0.15);
    }

    private double calculateFreshnessFromTimestamp(long timestamp) {
        if (timestamp <= 0) return 0.5;
        long ageMs = System.currentTimeMillis() - timestamp;
        if (ageMs <= 0) return 1.0;
        // Exponential decay: full score within 1 hour, degrades over 24 hours
        double ageHours = ageMs / 3600000.0;
        return Math.max(0.1, 1.0 / (1.0 + (ageHours / 12.0)));
    }

    private ConflictResolution createResolution(RetrievalEvidence winner, RetrievalEvidence loser,
                                                EvidenceSource winnerSource, EvidenceSource loserSource,
                                                double winnerScore, double loserScore, String reason) {
        return ConflictResolution.builder()
                .entityKey(winner != null ? winner.getEntityKey() : "unknown")
                .winningValue(winner != null ? winner.getContentSnippet() : null)
                .losingValue(loser != null ? loser.getContentSnippet() : null)
                .winningSource(winnerSource)
                .losingSource(loserSource)
                .winningScore(winnerScore)
                .losingScore(loserScore)
                .resolutionReason(reason)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
