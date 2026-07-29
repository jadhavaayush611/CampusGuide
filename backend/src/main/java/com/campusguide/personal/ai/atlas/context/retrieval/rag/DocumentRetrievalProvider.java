package com.campusguide.personal.ai.atlas.context.retrieval.rag;

import com.campusguide.personal.ai.atlas.context.evidence.RetrievalEvidence;

import java.util.List;

/**
 * Interface for unstructured document/pdf retrieval.
 */
public interface DocumentRetrievalProvider {
    List<RetrievalEvidence> retrieveDocumentEvidence(String query, List<String> documentCategories);
}
