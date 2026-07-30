package com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.engine;

import com.campusguide.personal.ai.atlas.knowledge.graph.metrics.GraphReasoningMetrics;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.context.GraphContext;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Main Reasoning Engine component in Atlas Knowledge Graph infrastructure.
 */
@Component
@Slf4j
public class ReasoningEngine implements GraphReasoner {

    private final ReasoningPipeline pipeline;

    @Autowired
    public ReasoningEngine(ReasoningPipeline pipeline) {
        this.pipeline = pipeline != null ? pipeline : new ReasoningPipeline(null, null, null, null);
    }

    public ReasoningEngine() {
        this.pipeline = new ReasoningPipeline(null, null, null, null);
    }

    @Override
    public ReasoningEvidence reason(GraphContext context) {
        return reason(context, null);
    }

    @Override
    public ReasoningEvidence reason(GraphContext context, GraphReasoningMetrics metrics) {
        if (context == null) {
            log.warn("ReasoningEngine received null GraphContext");
            return ReasoningEvidence.empty();
        }

        log.debug("Executing graph reasoning for contextId={} objective={}",
                context.getContextId(), context.getObjective() != null ? context.getObjective().getType() : "NONE");

        return pipeline.execute(context, metrics);
    }
}
