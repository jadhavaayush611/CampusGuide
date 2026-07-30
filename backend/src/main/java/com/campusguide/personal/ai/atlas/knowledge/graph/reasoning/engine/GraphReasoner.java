package com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.engine;

import com.campusguide.personal.ai.atlas.knowledge.graph.metrics.GraphReasoningMetrics;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.context.GraphContext;

/**
 * Interface for graph reasoning strategies executing over GraphContext.
 */
public interface GraphReasoner {

    /**
     * Executes graph reasoning on a GraphContext instance.
     */
    ReasoningEvidence reason(GraphContext context);

    /**
     * Executes graph reasoning with metric recording.
     */
    ReasoningEvidence reason(GraphContext context, GraphReasoningMetrics metrics);
}
