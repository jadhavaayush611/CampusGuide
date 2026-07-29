package com.campusguide.personal.ai.atlas.context.retrieval.rag;

import com.campusguide.personal.ai.atlas.context.evidence.RetrievalEvidence;
import com.campusguide.personal.ai.atlas.context.query.QueryContext;

import java.util.List;

/**
 * Interface for episodic and long-term user memory retrieval.
 */
public interface MemoryRetrievalProvider {
    List<RetrievalEvidence> retrieveMemoryEvidence(String userId, QueryContext queryContext);
}
