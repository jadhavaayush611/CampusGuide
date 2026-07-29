package com.campusguide.personal.ai.atlas.knowledge.embedding;

/**
 * Extension interface for local on-device or in-process embedding providers (e.g., ONNX Runtime, DJL, Transformers).
 */
public interface LocalEmbeddingProvider extends EmbeddingProvider {

    /**
     * Checks whether local model artifacts are downloaded and loaded in memory.
     */
    boolean isLoaded();

    /**
     * Pre-warms or loads the local embedding model into memory.
     */
    void initialize();
}
