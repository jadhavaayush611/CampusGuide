package com.campusguide.personal.ai.atlas.knowledge.graph.extension;

import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.context.GraphContext;
import java.util.List;

/**
 * Extension point for multi-agent graph context consensus and federated reasoning.
 */
public interface MultiAgentReasoningExtension {
    List<String> synthesizeMultiAgentConsensus(List<GraphContext> agentContexts);
}
