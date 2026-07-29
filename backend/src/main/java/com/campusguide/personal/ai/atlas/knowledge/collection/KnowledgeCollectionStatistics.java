package com.campusguide.personal.ai.atlas.knowledge.collection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Encapsulates performance metrics, artifact counts, and indexing statistics for a KnowledgeCollection.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeCollectionStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private int totalArtifactCount = 0;

    @Builder.Default
    private int totalChunkCount = 0;

    @Builder.Default
    private int totalVectorCount = 0;

    @Builder.Default
    private long byteSize = 0L;

    private Instant lastIndexedAt;
    private Instant lastUpdatedAt;

    public void incrementArtifacts(int chunkCount, long bytes) {
        this.totalArtifactCount++;
        this.totalChunkCount += chunkCount;
        this.totalVectorCount += chunkCount;
        this.byteSize += bytes;
        this.lastUpdatedAt = Instant.now();
    }
}
