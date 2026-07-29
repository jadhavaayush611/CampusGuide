package com.campusguide.personal.ai.atlas.knowledge.vector.provider;

import com.campusguide.personal.ai.atlas.knowledge.vector.VectorIndex;
import com.campusguide.personal.ai.atlas.knowledge.vector.VectorMetadata;
import com.campusguide.personal.ai.atlas.knowledge.vector.VectorRecord;
import com.campusguide.personal.ai.atlas.knowledge.vector.VectorStore;

import java.util.List;
import java.util.Optional;

/**
 * Extension point contract for Qdrant vector database integration.
 */
public class QdrantVectorStore implements VectorStore {

    private final VectorIndex index = VectorIndex.builder()
            .indexName("qdrant_knowledge_index")
            .dimension(1536)
            .metric(VectorIndex.MetricType.COSINE)
            .build();

    @Override
    public String getProviderName() {
        return "qdrant";
    }

    @Override
    public VectorIndex getIndex() {
        return index;
    }

    @Override
    public void index(VectorRecord record) {
        throw new UnsupportedOperationException("Qdrant integration requires Qdrant gRPC client.");
    }

    @Override
    public void indexAll(List<VectorRecord> records) {
        throw new UnsupportedOperationException("Qdrant integration requires Qdrant gRPC client.");
    }

    @Override
    public List<VectorRecord> search(float[] queryVector, int topK, VectorMetadata filter) {
        throw new UnsupportedOperationException("Qdrant integration requires Qdrant gRPC client.");
    }

    @Override
    public Optional<VectorRecord> get(String artifactId) {
        return Optional.empty();
    }

    @Override
    public boolean delete(String artifactId) {
        return false;
    }

    @Override
    public void clear() {}

    @Override
    public int count() {
        return 0;
    }
}
