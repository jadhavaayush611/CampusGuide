package com.campusguide.personal.ai.atlas.context.retrieval.rag;

import com.campusguide.personal.ai.atlas.context.evidence.RetrievalEvidence;

import java.util.List;

/**
 * Interface for external API and third-party knowledge retrieval.
 */
public interface ExternalKnowledgeProvider {
    List<RetrievalEvidence> retrieveExternalEvidence(String topicOrQuery);
}
