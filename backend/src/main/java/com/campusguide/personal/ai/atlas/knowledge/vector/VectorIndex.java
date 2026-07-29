package com.campusguide.personal.ai.atlas.knowledge.vector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Definition of vector index metadata and similarity metric settings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorIndex implements Serializable {

    private static final long serialVersionUID = 1L;

    private String indexName;
    private int dimension;
    @Builder.Default
    private MetricType metric = MetricType.COSINE;
    @Builder.Default
    private Instant createdAt = Instant.now();

    public enum MetricType {
        COSINE,
        EUCLIDEAN,
        DOT_PRODUCT
    }

    public static VectorIndex defaultIndex() {
        return VectorIndex.builder()
                .indexName("atlas-knowledge-index")
                .dimension(1536)
                .metric(MetricType.COSINE)
                .createdAt(Instant.now())
                .build();
    }
}
