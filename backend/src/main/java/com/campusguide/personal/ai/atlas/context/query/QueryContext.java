package com.campusguide.personal.ai.atlas.context.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Structured query analysis result produced by QueryAnalyzer.
 * Serves as input to intelligent context retrieval strategies.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryContext {
    private String rawQuery;
    private String normalizedQuery;
    private QueryIntent intent;
    private QueryDomain domainClassification;

    @Builder.Default
    private List<ExtractedEntity> entities = new ArrayList<>();

    private TemporalInformation temporalInformation;

    @Builder.Default
    private List<String> retrievalHints = new ArrayList<>();

    private double confidenceScore;
}
