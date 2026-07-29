package com.campusguide.personal.ai.atlas.knowledge.retrieval;

import com.campusguide.personal.ai.atlas.context.evidence.EvidenceScore;
import com.campusguide.personal.ai.atlas.context.evidence.EvidenceSource;
import com.campusguide.personal.ai.atlas.context.evidence.EvidenceType;
import com.campusguide.personal.ai.atlas.context.evidence.RetrievalEvidence;
import com.campusguide.personal.ai.atlas.context.query.QueryContext;
import com.campusguide.personal.ai.atlas.knowledge.artifact.KnowledgeArtifact;
import com.campusguide.personal.ai.atlas.knowledge.citation.Citation;
import com.campusguide.personal.ai.atlas.knowledge.citation.CitationGenerator;
import com.campusguide.personal.ai.atlas.knowledge.ranking.ArtifactRankingService;
import com.campusguide.personal.ai.atlas.knowledge.ranking.ArtifactScore;
import com.campusguide.personal.ai.atlas.knowledge.retrieval.collection.CollectionFilter;
import com.campusguide.personal.ai.atlas.knowledge.retrieval.collection.CollectionRetrievalPolicy;
import com.campusguide.personal.ai.atlas.knowledge.retrieval.collection.CollectionSelector;
import com.campusguide.personal.ai.atlas.knowledge.retrieval.collection.CollectionSelector.KnowledgeCollectionSelection;
import com.campusguide.personal.ai.atlas.knowledge.retrieval.hybrid.HybridRankingEngine.HybridCandidate;
import com.campusguide.personal.ai.atlas.knowledge.retrieval.hybrid.HybridRetriever;
import com.campusguide.personal.ai.atlas.knowledge.vector.VectorMetadata;
import com.campusguide.personal.ai.atlas.metrics.AtlasMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Core engine running end-to-end collection-aware, hybrid semantic retrieval, artifact ranking,
 * citation generation, and evidence conversion for Atlas RAG Context Intelligence integration.
 */
@Component
@Slf4j
public class KnowledgeRetrievalEngine {

    private final CollectionSelector collectionSelector;
    private final HybridRetriever hybridRetriever;
    private final ArtifactRankingService rankingService;
    private final CitationGenerator citationGenerator;
    private final AtlasMetrics metrics;

    @Autowired
    public KnowledgeRetrievalEngine(
            CollectionSelector collectionSelector,
            HybridRetriever hybridRetriever,
            ArtifactRankingService rankingService,
            CitationGenerator citationGenerator,
            @Autowired(required = false) AtlasMetrics metrics) {
        this.collectionSelector = collectionSelector;
        this.hybridRetriever = hybridRetriever;
        this.rankingService = rankingService;
        this.citationGenerator = citationGenerator;
        this.metrics = metrics;
    }

    public KnowledgeRetrievalResult executeRetrieval(
            QueryContext queryContext,
            String userId,
            List<String> userRoles,
            CollectionRetrievalPolicy policy,
            CollectionFilter filter,
            int topK,
            double minSimilarity,
            VectorMetadata structuredFilter) {

        long startTime = System.currentTimeMillis();

        if (queryContext == null || queryContext.getRawQuery() == null || queryContext.getRawQuery().isBlank()) {
            return KnowledgeRetrievalResult.empty();
        }

        // 1. Collection Selection
        List<KnowledgeCollectionSelection> selectedCollections = collectionSelector.selectCollections(
                queryContext, userId, userRoles, policy, filter);

        // 2. Hybrid Semantic Retrieval
        List<HybridCandidate> candidates = hybridRetriever.retrieveHybrid(
                queryContext.getRawQuery(), selectedCollections, topK * 2, minSimilarity, structuredFilter);

        // 3. Multi-dimensional Artifact Ranking
        List<ArtifactScore> rankedScores = rankingService.rankArtifacts(candidates, queryContext);
        if (rankedScores.size() > topK) {
            rankedScores = rankedScores.subList(0, topK);
        }

        // 4. Citation Generation
        List<Citation> citations = citationGenerator.generateCitations(rankedScores);

        // 5. Convert Artifacts -> RetrievalEvidence for Context Intelligence Layer
        List<RetrievalEvidence> evidences = buildEvidences(rankedScores, citations);

        long latencyMs = System.currentTimeMillis() - startTime;
        if (metrics != null) {
            metrics.recordOrchestrationLatency(latencyMs);
        }

        log.info("KnowledgeRetrievalEngine executed in {} ms. Selected collections: {}, Candidates: {}, Ranked: {}, Citations: {}",
                latencyMs, selectedCollections.size(), candidates.size(), rankedScores.size(), citations.size());

        List<KnowledgeArtifact> artifacts = rankedScores.stream().map(ArtifactScore::getArtifact).toList();

        return new KnowledgeRetrievalResult(
                artifacts,
                rankedScores,
                citations,
                evidences,
                selectedCollections,
                latencyMs
        );
    }

    private List<RetrievalEvidence> buildEvidences(List<ArtifactScore> rankedScores, List<Citation> citations) {
        Map<String, Citation> citationMap = new HashMap<>();
        for (Citation c : citations) {
            citationMap.put(c.getArtifactId(), c);
        }

        List<RetrievalEvidence> evidences = new ArrayList<>();
        for (ArtifactScore score : rankedScores) {
            KnowledgeArtifact artifact = score.getArtifact();
            if (artifact == null) continue;

            Citation cite = citationMap.get(artifact.getId().getValue());
            String mark = cite != null ? cite.getCitationMark() : "";

            EvidenceScore evScore = EvidenceScore.builder()
                    .relevanceScore(score.getSemanticSimilarity())
                    .confidenceScore(score.getRetrievalConfidence())
                    .sourceAuthorityScore(score.getSourceAuthority())
                    .freshnessScore(score.getFreshnessScore())
                    .qualityScore(score.getEvidenceQuality())
                    .build();
            evScore.calculateOverallScore();

            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("collectionId", artifact.getCollectionId());
            meta.put("artifactId", artifact.getId().getValue());
            meta.put("totalScore", score.getTotalScore());
            meta.put("citationMark", mark);
            if (cite != null) {
                meta.put("citationId", cite.getCitationId());
            }

            RetrievalEvidence evidence = RetrievalEvidence.builder()
                    .id("ev_rag_" + artifact.getId().getValue())
                    .type(EvidenceType.RAG)
                    .source(EvidenceSource.KNOWLEDGE_BASE)
                    .entityKey(artifact.getId().getValue())
                    .contentSnippet(mark.isEmpty() ? artifact.getContent() : mark + " " + artifact.getContent())
                    .rationale("Retrieved via hybrid RAG retrieval. " + score.getExplanation())
                    .timestamp(System.currentTimeMillis())
                    .score(evScore)
                    .metadata(meta)
                    .build();

            evidences.add(evidence);
        }
        return evidences;
    }

    public record KnowledgeRetrievalResult(
            List<KnowledgeArtifact> artifacts,
            List<ArtifactScore> scores,
            List<Citation> citations,
            List<RetrievalEvidence> evidences,
            List<KnowledgeCollectionSelection> selectedCollections,
            long latencyMs
    ) {
        public static KnowledgeRetrievalResult empty() {
            return new KnowledgeRetrievalResult(List.of(), List.of(), List.of(), List.of(), List.of(), 0L);
        }
    }
}
