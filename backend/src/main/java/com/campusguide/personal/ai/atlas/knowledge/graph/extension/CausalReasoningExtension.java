package com.campusguide.personal.ai.atlas.knowledge.graph.extension;

import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.context.GraphContext;
import java.util.Map;

/**
 * Extension point for root-cause analysis and counterfactual causal reasoning.
 */
public interface CausalReasoningExtension {
    Map<String, Double> analyzeCausalImpact(GraphContext context, String causeNodeId, String effectNodeId);
}
