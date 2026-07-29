package com.campusguide.personal.ai.atlas.context.retrieval.rag;

import com.campusguide.personal.ai.atlas.context.evidence.RetrievalEvidence;
import com.campusguide.personal.ai.atlas.context.query.QueryContext;

import java.util.List;

/**
 * Interface for hybrid BM25 + vector retrieval.
 */
public interface HybridRetrievalProvider {
    List<RetrievalEvidence> retrieveHybridEvidence(QueryContext queryContext, int topK);
}
