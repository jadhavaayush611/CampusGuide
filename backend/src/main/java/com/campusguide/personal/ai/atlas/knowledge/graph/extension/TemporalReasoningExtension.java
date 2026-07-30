package com.campusguide.personal.ai.atlas.knowledge.graph.extension;

import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.context.GraphContext;
import java.time.Instant;
import java.util.List;

/**
 * Extension point for time-window and snapshot-based temporal graph reasoning.
 */
public interface TemporalReasoningExtension {
    List<String> evaluateTemporalValidity(GraphContext context, Instant timeWindowStart, Instant timeWindowEnd);
}
