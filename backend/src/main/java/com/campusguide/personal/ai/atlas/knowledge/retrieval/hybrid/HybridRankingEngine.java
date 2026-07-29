package com.campusguide.personal.ai.atlas.knowledge.retrieval.hybrid;

import com.campusguide.personal.ai.atlas.knowledge.artifact.KnowledgeArtifact;
import com.campusguide.personal.ai.atlas.knowledge.vector.VectorMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Hybrid ranking engine combining vector similarity, keyword matching (BM25 / token overlap),
 * structured category/domain matching, and metadata filtering.
 */
@Component
@Slf4j
public class HybridRankingEngine {

    public List<HybridCandidate> rankCandidates(
            List<KnowledgeArtifact> candidates,
            String queryText,
            Map<String, Double> semanticScores,
            VectorMetadata structuredFilter) {

        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        Set<String> queryTokens = extractTokens(queryText);
        List<HybridCandidate> scoredList = new ArrayList<>();

        for (KnowledgeArtifact artifact : candidates) {
            double vectorSim = semanticScores.getOrDefault(artifact.getId().getValue(), 0.0);
            double keywordScore = calculateKeywordScore(artifact, queryTokens);
            double metadataMatchScore = calculateMetadataMatchScore(artifact, structuredFilter);

            // Composite Hybrid Score Formula
            double combinedScore = (vectorSim * 0.50) + (keywordScore * 0.35) + (metadataMatchScore * 0.15);

            scoredList.add(new HybridCandidate(artifact, combinedScore, vectorSim, keywordScore, metadataMatchScore));
        }

        scoredList.sort((a, b) -> {
            int cmp = Double.compare(b.hybridScore(), a.hybridScore());
            if (cmp != 0) return cmp;
            return a.artifact().getId().getValue().compareTo(b.artifact().getId().getValue());
        });

        return scoredList;
    }

    private double calculateKeywordScore(KnowledgeArtifact artifact, Set<String> queryTokens) {
        if (queryTokens.isEmpty() || artifact == null || artifact.getContent() == null) {
            return 0.0;
        }

        String docText = artifact.getContent().toLowerCase(Locale.ROOT);
        String titleText = artifact.getSource() != null && artifact.getSource().getTitle() != null
                ? artifact.getSource().getTitle().toLowerCase(Locale.ROOT)
                : "";

        int matches = 0;
        int titleMatches = 0;

        for (String token : queryTokens) {
            if (docText.contains(token)) {
                matches++;
            }
            if (!titleText.isEmpty() && titleText.contains(token)) {
                titleMatches++;
            }
        }

        double tokenOverlapRatio = (double) matches / queryTokens.size();
        double titleBonus = titleMatches > 0 ? 0.20 : 0.0;

        return Math.min(1.0, tokenOverlapRatio + titleBonus);
    }

    private double calculateMetadataMatchScore(KnowledgeArtifact artifact, VectorMetadata filter) {
        if (filter == null || artifact == null) return 1.0;

        double score = 1.0;
        if (filter.getCategory() != null && artifact.getMetadata() != null) {
            if (filter.getCategory().equalsIgnoreCase(artifact.getMetadata().getCategory())) {
                score += 0.2;
            } else {
                score -= 0.3;
            }
        }

        if (filter.getDomain() != null && artifact.getMetadata() != null) {
            if (filter.getDomain().equalsIgnoreCase(artifact.getMetadata().getDomain())) {
                score += 0.2;
            } else {
                score -= 0.3;
            }
        }

        return Math.max(0.0, Math.min(1.0, score));
    }

    private Set<String> extractTokens(String text) {
        if (text == null || text.isBlank()) return Set.of();
        String[] words = text.toLowerCase(Locale.ROOT).split("[^a-zA-Z0-9]+");
        Set<String> tokens = new HashSet<>();
        for (String w : words) {
            if (w.length() > 2) {
                tokens.add(w);
            }
        }
        return tokens;
    }

    public record HybridCandidate(
            KnowledgeArtifact artifact,
            double hybridScore,
            double vectorSimilarity,
            double keywordScore,
            double metadataMatchScore
    ) {}
}
