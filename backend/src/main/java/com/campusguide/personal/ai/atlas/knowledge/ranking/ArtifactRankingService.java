package com.campusguide.personal.ai.atlas.knowledge.ranking;

import com.campusguide.personal.ai.atlas.context.query.QueryContext;
import com.campusguide.personal.ai.atlas.knowledge.artifact.KnowledgeArtifact;
import com.campusguide.personal.ai.atlas.knowledge.collection.KnowledgeCollection;
import com.campusguide.personal.ai.atlas.knowledge.collection.KnowledgeCollectionRegistry;
import com.campusguide.personal.ai.atlas.knowledge.retrieval.hybrid.HybridRankingEngine.HybridCandidate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service evaluating candidate KnowledgeArtifacts across 7 weighted dimensions
 * and ordering them deterministically.
 */
@Service
@Slf4j
public class ArtifactRankingService {

    private final KnowledgeCollectionRegistry collectionRegistry;

    @Autowired
    public ArtifactRankingService(KnowledgeCollectionRegistry collectionRegistry) {
        this.collectionRegistry = collectionRegistry;
    }

    public List<ArtifactScore> rankArtifacts(
            List<HybridCandidate> candidates,
            QueryContext queryContext) {

        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        double confidence = queryContext != null ? queryContext.getConfidenceScore() : 0.8;

        List<ArtifactScore> scoredArtifacts = new ArrayList<>();

        for (HybridCandidate candidate : candidates) {
            KnowledgeArtifact artifact = candidate.artifact();
            if (artifact == null) continue;

            double semSim = candidate.vectorSimilarity();
            double kwOverlap = candidate.keywordScore();
            double freshness = calculateFreshnessScore(artifact.getUpdatedAt());
            double evidenceQuality = calculateEvidenceQuality(artifact);
            double sourceAuth = calculateSourceAuthority(artifact);
            double collectionPriority = calculateCollectionPriority(artifact.getCollectionId());

            double totalScore = (semSim * 0.35)
                    + (kwOverlap * 0.20)
                    + (freshness * 0.10)
                    + (evidenceQuality * 0.10)
                    + (sourceAuth * 0.10)
                    + (collectionPriority * 0.10)
                    + (confidence * 0.05);

            String explanation = String.format(
                    "Total: %.3f [Semantic: %.2f, Kw: %.2f, Freshness: %.2f, Quality: %.2f, Auth: %.2f, CollPri: %.2f, Conf: %.2f]",
                    totalScore, semSim, kwOverlap, freshness, evidenceQuality, sourceAuth, collectionPriority, confidence);

            ArtifactScore score = ArtifactScore.builder()
                    .artifact(artifact)
                    .totalScore(totalScore)
                    .semanticSimilarity(semSim)
                    .keywordOverlap(kwOverlap)
                    .freshnessScore(freshness)
                    .evidenceQuality(evidenceQuality)
                    .sourceAuthority(sourceAuth)
                    .collectionPriority(collectionPriority)
                    .retrievalConfidence(confidence)
                    .explanation(explanation)
                    .build();

            scoredArtifacts.add(score);
        }

        Collections.sort(scoredArtifacts);

        log.debug("ArtifactRankingService ranked {} artifacts (top totalScore: {})",
                scoredArtifacts.size(),
                scoredArtifacts.isEmpty() ? 0.0 : scoredArtifacts.get(0).getTotalScore());

        return scoredArtifacts;
    }

    private double calculateFreshnessScore(Instant updatedAt) {
        if (updatedAt == null) return 0.5;
        long daysOld = Duration.between(updatedAt, Instant.now()).toDays();
        if (daysOld <= 1) return 1.0;
        if (daysOld <= 7) return 0.9;
        if (daysOld <= 30) return 0.75;
        if (daysOld <= 90) return 0.6;
        if (daysOld <= 365) return 0.4;
        return 0.2;
    }

    private double calculateEvidenceQuality(KnowledgeArtifact artifact) {
        if (artifact == null) return 0.5;
        double quality = 0.5;

        if (artifact.getContent() != null && artifact.getContent().length() > 50) {
            quality += 0.2;
        }
        if (artifact.getSource() != null && artifact.getSource().getTitle() != null) {
            quality += 0.15;
        }
        if (artifact.getReferences() != null && !artifact.getReferences().isEmpty()) {
            quality += 0.15;
        }
        return Math.min(1.0, quality);
    }

    private double calculateSourceAuthority(KnowledgeArtifact artifact) {
        if (artifact == null || artifact.getSource() == null) return 0.5;
        String sourceType = artifact.getSource().getSourceType();
        if (sourceType == null) return 0.5;

        return switch (sourceType.toLowerCase()) {
            case "pdf", "official", "catalog", "syllabus" -> 1.0;
            case "docx", "markdown", "text" -> 0.85;
            case "faq", "web_page" -> 0.75;
            default -> 0.6;
        };
    }

    private double calculateCollectionPriority(String collectionId) {
        if (collectionId == null || collectionRegistry == null) return 0.5;
        return collectionRegistry.getCollection(collectionId)
                .map(KnowledgeCollection::getMetadata)
                .map(meta -> Math.min(1.0, meta.getPriority() / 2.0))
                .orElse(0.5);
    }
}
