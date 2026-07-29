package com.campusguide.personal.ai.atlas.knowledge.vector;

import java.util.List;
import java.util.Optional;

/**
 * Provider-independent interface for vector store index operations and similarity search.
 */
public interface VectorStore {

    /**
     * Index a single VectorRecord.
     */
    void index(VectorRecord record);

    /**
     * Index a batch of VectorRecords.
     */
    void indexAll(List<VectorRecord> records);

    /**
     * Perform similarity search against indexed vector records.
     */
    List<VectorRecord> search(float[] queryVector, int topK, VectorMetadata filter);

    /**
     * Retrieve a vector record by artifact ID.
     */
    Optional<VectorRecord> get(String artifactId);

    /**
     * Delete a vector record by artifact ID.
     */
    boolean delete(String artifactId);

    /**
     * Clear all indexed vector records.
     */
    void clear();

    /**
     * Return total number of indexed vector records.
     */
    int count();

    /**
     * Vector store provider name.
     */
    String getProviderName();

    /**
     * Vector store index metadata.
     */
    VectorIndex getIndex();
}
