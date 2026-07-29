package com.campusguide.personal.ai.atlas.context.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Structured entity extracted from user queries.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExtractedEntity {
    private String name;
    private EntityType type;
    private String normalizedName;
    private double confidence;
    private int startPosition;
    private int endPosition;

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}
