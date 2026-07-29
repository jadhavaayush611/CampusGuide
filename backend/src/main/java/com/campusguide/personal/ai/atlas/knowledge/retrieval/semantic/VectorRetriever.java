package com.campusguide.personal.ai.atlas.knowledge.retrieval.semantic;

import com.campusguide.personal.ai.atlas.knowledge.vector.VectorMetadata;
import com.campusguide.personal.ai.atlas.knowledge.vector.VectorRecord;
import com.campusguide.personal.ai.atlas.knowledge.vector.VectorStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Executes low-level vector similarity search over vector stores given dense embeddings and metadata filters.
 */
@Component
@Slf4j
public class VectorRetriever {

    private final VectorStore vectorStore;

    @Autowired
    public VectorRetriever(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public List<VectorRecord> searchVectors(float[] queryVector, int topK, VectorMetadata filter) {
        if (queryVector == null || queryVector.length == 0 || topK <= 0) {
            return List.of();
        }
        return vectorStore.search(queryVector, topK, filter);
    }
}
