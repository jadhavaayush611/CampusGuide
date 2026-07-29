package com.campusguide.personal.ai.atlas.context.retrieval;

import com.campusguide.personal.ai.atlas.context.query.QueryContext;
import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Encapsulates execution state during intelligent context retrieval.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetrievalContext {

    private AtlasChatRequest request;
    private QueryContext queryContext;
    private RetrievalPolicy policy;

    public RetrievalContext(AtlasChatRequest request, QueryContext queryContext) {
        this.request = request;
        this.queryContext = queryContext;
        this.policy = new RetrievalPolicy();
        this.executedStrategies = new ArrayList<>();
        this.skippedStrategies = new ArrayList<>();
        this.retrievedContributions = new ConcurrentHashMap<>();
        this.startTimeMs = System.currentTimeMillis();
    }

    @Builder.Default
    private List<String> executedStrategies = new ArrayList<>();

    @Builder.Default
    private List<String> skippedStrategies = new ArrayList<>();

    @Builder.Default
    private Map<String, Object> retrievedContributions = new ConcurrentHashMap<>();

    private long startTimeMs;
    private long totalLatencyMs;
}
