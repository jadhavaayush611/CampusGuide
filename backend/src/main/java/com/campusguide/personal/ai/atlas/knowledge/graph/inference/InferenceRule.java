package com.campusguide.personal.ai.atlas.knowledge.graph.inference;

import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;
import com.campusguide.personal.ai.atlas.knowledge.graph.view.KnowledgeGraphView;

import java.util.List;

/**
 * Interface for declarative rule-based graph inference over a KnowledgeGraphView.
 */
public interface InferenceRule {

    String getRuleId();

    String getRuleName();

    /**
     * Evaluates rule against a read-only graph view and returns non-persisted virtual inferred edges.
     */
    List<KnowledgeEdge> evaluate(KnowledgeGraphView view);
}
