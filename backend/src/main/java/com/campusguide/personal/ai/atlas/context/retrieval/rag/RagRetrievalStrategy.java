package com.campusguide.personal.ai.atlas.context.retrieval.rag;

import com.campusguide.personal.ai.atlas.context.AtlasContext;
import com.campusguide.personal.ai.atlas.context.evidence.EvidenceBundle;
import com.campusguide.personal.ai.atlas.context.evidence.EvidenceScore;
import com.campusguide.personal.ai.atlas.context.evidence.EvidenceSource;
import com.campusguide.personal.ai.atlas.context.evidence.EvidenceType;
import com.campusguide.personal.ai.atlas.context.evidence.RetrievalEvidence;
import com.campusguide.personal.ai.atlas.context.query.QueryContext;
import com.campusguide.personal.ai.atlas.context.retrieval.RetrievalContext;
import com.campusguide.personal.ai.atlas.context.retrieval.RetrievalPolicy;
import com.campusguide.personal.ai.atlas.context.retrieval.RetrievalStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Extensible RAG Strategy bridging vector search, hybrid retrieval, and memory search into AtlasContext.
 */
@Component
@Slf4j
public class RagRetrievalStrategy implements RetrievalStrategy {

    private final VectorRetrievalProvider vectorProvider;
    private final SemanticRetrievalProvider semanticProvider;
    private final MemoryRetrievalProvider memoryProvider;

    @Autowired
    public RagRetrievalStrategy(@Autowired(required = false) VectorRetrievalProvider vectorProvider,
                                 @Autowired(required = false) SemanticRetrievalProvider semanticProvider,
                                 @Autowired(required = false) MemoryRetrievalProvider memoryProvider) {
        this.vectorProvider = vectorProvider;
        this.semanticProvider = semanticProvider;
        this.memoryProvider = memoryProvider;
    }

    @Override
    public String getStrategyName() {
        return "rag";
    }

    @Override
    public boolean supports(QueryContext queryContext, RetrievalPolicy policy) {
        return vectorProvider != null || semanticProvider != null || memoryProvider != null;
    }

    @Override
    public double calculateRelevance(QueryContext queryContext) {
        if (queryContext == null || queryContext.getRawQuery() == null) return 0.0;
        // RAG is highly relevant for non-trivial queries
        return queryContext.getRawQuery().length() > 5 ? 0.75 : 0.30;
    }

    @Override
    public void retrieve(RetrievalContext retrievalContext, AtlasContext atlasContext) {
        List<RetrievalEvidence> evidences = new ArrayList<>();
        QueryContext q = retrievalContext.getQueryContext();

        if (vectorProvider != null && q != null) {
            evidences.addAll(vectorProvider.retrieveVectorEvidence(q, 3));
        }
        if (semanticProvider != null && q != null) {
            evidences.addAll(semanticProvider.retrieveSemanticEvidence(q, 0.70));
        }
        if (memoryProvider != null && q != null && atlasContext.getUserId() != null) {
            evidences.addAll(memoryProvider.retrieveMemoryEvidence(atlasContext.getUserId(), q));
        }

        if (!evidences.isEmpty()) {
            EvidenceBundle bundle = EvidenceBundle.builder()
                    .targetDomain("rag")
                    .evidences(evidences)
                    .sourceSummary("Retrieved " + evidences.size() + " RAG evidence items")
                    .timestamp(System.currentTimeMillis())
                    .build();
            bundle.recalculateAggregateScore();
            atlasContext.addEvidenceBundle(bundle);
        }
    }
}
