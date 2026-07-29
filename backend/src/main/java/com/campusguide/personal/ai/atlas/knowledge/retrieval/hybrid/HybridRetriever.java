package com.campusguide.personal.ai.atlas.knowledge.retrieval.hybrid;

import com.campusguide.personal.ai.atlas.knowledge.artifact.KnowledgeArtifact;
import com.campusguide.personal.ai.atlas.knowledge.retrieval.collection.CollectionSelector.KnowledgeCollectionSelection;
import com.campusguide.personal.ai.atlas.knowledge.retrieval.semantic.SemanticRetriever;
import com.campusguide.personal.ai.atlas.knowledge.retrieval.semantic.SemanticRetriever.SemanticMatch;
import com.campusguide.personal.ai.atlas.knowledge.vector.VectorMetadata;
import com.campusguide.personal.ai.atlas.knowledge.vector.VectorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Hybrid retriever unifying vector similarity search, structured metadata queries, keyword search,
 * and collection-scoped retrieval into a single ranked KnowledgeArtifact list.
 */
@Component
@Slf4j
public class HybridRetriever {

    private final SemanticRetriever semanticRetriever;
    private final VectorRepository vectorRepository;
    private final HybridRankingEngine hybridRankingEngine;

    @Autowired
    public HybridRetriever(
            SemanticRetriever semanticRetriever,
            VectorRepository vectorRepository,
            HybridRankingEngine hybridRankingEngine) {
        this.semanticRetriever = semanticRetriever;
        this.vectorRepository = vectorRepository;
        this.hybridRankingEngine = hybridRankingEngine;
    }

    public List<HybridRankingEngine.HybridCandidate> retrieveHybrid(
            String queryText,
            List<KnowledgeCollectionSelection> collections,
            int topK,
            double minSimilarity,
            VectorMetadata structuredFilter) {

        if (queryText == null || queryText.isBlank() || topK <= 0) {
            return List.of();
        }

        Map<String, KnowledgeArtifact> candidateMap = new HashMap<>();
        Map<String, Double> semanticScores = new HashMap<>();

        // If collection selections are provided, search each collection
        if (collections != null && !collections.isEmpty()) {
            for (KnowledgeCollectionSelection sel : collections) {
                String collectionId = sel.collection().getCollectionId();

                VectorMetadata filterCopy = structuredFilter != null
                        ? VectorMetadata.builder()
                        .documentId(structuredFilter.getDocumentId())
                        .category(structuredFilter.getCategory())
                        .domain(structuredFilter.getDomain())
                        .sourceType(structuredFilter.getSourceType())
                        .collectionId(collectionId)
                        .fields(structuredFilter.getFields())
                        .build()
                        : VectorMetadata.builder().collectionId(collectionId).build();

                List<SemanticMatch> matches = semanticRetriever.retrieveSemantic(queryText, topK * 2, minSimilarity, filterCopy);
                for (SemanticMatch m : matches) {
                    String id = m.artifact().getId().getValue();
                    candidateMap.put(id, m.artifact());
                    semanticScores.put(id, Math.max(semanticScores.getOrDefault(id, 0.0), m.similarityScore()));
                }
            }
        } else {
            // Fallback un-scoped search
            List<SemanticMatch> matches = semanticRetriever.retrieveSemantic(queryText, topK * 2, minSimilarity, structuredFilter);
            for (SemanticMatch m : matches) {
                String id = m.artifact().getId().getValue();
                candidateMap.put(id, m.artifact());
                semanticScores.put(id, m.similarityScore());
            }
        }

        List<KnowledgeArtifact> candidates = new ArrayList<>(candidateMap.values());

        List<HybridRankingEngine.HybridCandidate> ranked = hybridRankingEngine.rankCandidates(
                candidates, queryText, semanticScores, structuredFilter);

        log.debug("Hybrid retrieval produced {} ranked candidates for query", ranked.size());

        return ranked.stream().limit(topK).toList();
    }
}
