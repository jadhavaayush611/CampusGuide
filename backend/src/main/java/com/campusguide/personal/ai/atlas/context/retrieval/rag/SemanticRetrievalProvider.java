package com.campusguide.personal.ai.atlas.context.retrieval.rag;

import com.campusguide.personal.ai.atlas.context.evidence.RetrievalEvidence;
import com.campusguide.personal.ai.atlas.context.query.QueryContext;

import java.util.List;

/**
 * Interface for semantic context retrieval.
 */
public interface SemanticRetrievalProvider {
    List<RetrievalEvidence> retrieveSemanticEvidence(QueryContext queryContext, double minSimilarityScore);
}
