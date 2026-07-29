package com.campusguide.personal.ai.atlas.context.retrieval.rag;

import com.campusguide.personal.ai.atlas.context.evidence.RetrievalEvidence;
import com.campusguide.personal.ai.atlas.context.query.QueryContext;

import java.util.List;

/**
 * Interface for vector-based semantic retrieval.
 */
public interface VectorRetrievalProvider {
    List<RetrievalEvidence> retrieveVectorEvidence(QueryContext queryContext, int topK);
}
