package com.campusguide.personal.ai.atlas.knowledge.graph.extension;

import com.campusguide.personal.ai.atlas.knowledge.graph.node.KnowledgeNode;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.context.GraphContext;
import java.util.List;

/**
 * Extension point for personalized graph-based entity recommendations.
 */
public interface RecommendationReasoningExtension {
    List<KnowledgeNode> recommendRelatedEntities(GraphContext context, int limit);
}
