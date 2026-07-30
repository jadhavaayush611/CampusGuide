package com.campusguide.personal.ai.atlas.knowledge.graph.extension;

import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.context.GraphContext;
import java.util.List;

/**
 * Extension point for goal-oriented multi-step plan generation on GraphContext views.
 */
public interface PlanningReasoningExtension {
    List<String> generateActionPlan(GraphContext context, String goal);
}
