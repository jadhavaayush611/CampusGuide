package com.campusguide.personal.ai.atlas.knowledge.chunking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration and metadata for chunking operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChunkMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private int chunkSize = 512;
    @Builder.Default
    private int overlap = 64;
    @Builder.Default
    private String strategyName = "FIXED_SIZE";
    @Builder.Default
    private Map<String, Object> extraAttributes = new HashMap<>();

    public static ChunkMetadata defaultOptions() {
        return ChunkMetadata.builder()
                .chunkSize(512)
                .overlap(64)
                .strategyName("SEMANTIC")
                .build();
    }
}
