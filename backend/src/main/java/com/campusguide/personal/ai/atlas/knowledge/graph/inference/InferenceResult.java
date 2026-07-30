package com.campusguide.personal.ai.atlas.knowledge.graph.inference;

import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates inferred virtual edges and metadata resulting from rule evaluation without mutating the underlying graph.
 */
@Data
@Builder
public class InferenceResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private final List<KnowledgeEdge> inferredEdges = Collections.emptyList();

    private final int rulesAppliedCount;
    private final long executionTimeMs;
    private final String engineId;

    public static InferenceResult empty() {
        return InferenceResult.builder()
                .inferredEdges(Collections.emptyList())
                .rulesAppliedCount(0)
                .executionTimeMs(0L)
                .engineId("inference_engine_empty")
                .build();
    }
}
