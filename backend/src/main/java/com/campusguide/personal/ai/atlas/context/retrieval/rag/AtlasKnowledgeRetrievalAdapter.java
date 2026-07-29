package com.campusguide.personal.ai.atlas.context.retrieval.rag;

import com.campusguide.personal.ai.atlas.context.evidence.RetrievalEvidence;
import com.campusguide.personal.ai.atlas.context.query.QueryContext;
import com.campusguide.personal.ai.atlas.knowledge.retrieval.KnowledgeRetrievalEngine;
import com.campusguide.personal.ai.atlas.knowledge.retrieval.KnowledgeRetrievalEngine.KnowledgeRetrievalResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adapter bridging KnowledgeRetrievalEngine to RAG retrieval provider interfaces.
 */
@Component
@Slf4j
public class AtlasKnowledgeRetrievalAdapter implements VectorRetrievalProvider, SemanticRetrievalProvider, HybridRetrievalProvider {

    private final KnowledgeRetrievalEngine knowledgeRetrievalEngine;

    @Autowired
    public AtlasKnowledgeRetrievalAdapter(KnowledgeRetrievalEngine knowledgeRetrievalEngine) {
        this.knowledgeRetrievalEngine = knowledgeRetrievalEngine;
    }

    @Override
    public List<RetrievalEvidence> retrieveVectorEvidence(QueryContext queryContext, int topK) {
        KnowledgeRetrievalResult result = knowledgeRetrievalEngine.executeRetrieval(
                queryContext, null, List.of(), null, null, topK, 0.0, null);
        return result.evidences();
    }

    @Override
    public List<RetrievalEvidence> retrieveSemanticEvidence(QueryContext queryContext, double minSimilarityScore) {
        KnowledgeRetrievalResult result = knowledgeRetrievalEngine.executeRetrieval(
                queryContext, null, List.of(), null, null, 10, minSimilarityScore, null);
        return result.evidences();
    }

    @Override
    public List<RetrievalEvidence> retrieveHybridEvidence(QueryContext queryContext, int topK) {
        KnowledgeRetrievalResult result = knowledgeRetrievalEngine.executeRetrieval(
                queryContext, null, List.of(), null, null, topK, 0.0, null);
        return result.evidences();
    }
}
