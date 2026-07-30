package com.campusguide.personal.ai.atlas.planning.engine;

import com.campusguide.personal.ai.atlas.decision.model.DecisionOutcome;
import com.campusguide.personal.ai.atlas.planning.context.PlanningContext;
import com.campusguide.personal.ai.atlas.planning.model.ExecutionPlan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Provider-independent Planning Intelligence Engine.
 * Transforms validated DecisionOutcomes into deterministic, explainable ExecutionPlans
 * without introducing execution logic.
 */
@Slf4j
@Service("planningEngine")
public class PlanningEngine {

    private final PlanningPipeline pipeline;

    public PlanningEngine(PlanningPipeline pipeline) {
        this.pipeline = pipeline;
    }

    /**
     * Primary entry point transforming a DecisionOutcome into an ExecutionPlan.
     */
    public ExecutionPlan generatePlan(DecisionOutcome decisionOutcome) {
        if (decisionOutcome == null) {
            log.warn("Null DecisionOutcome provided to PlanningEngine");
            return ExecutionPlan.fallback("plan_" + UUID.randomUUID().toString().substring(0, 8), "Null DecisionOutcome provided");
        }
        PlanningContext context = PlanningContext.fromDecisionOutcome(decisionOutcome);
        return generatePlan(context, PlanningStrategy.defaultStrategy());
    }

    /**
     * Entry point transforming PlanningContext into an ExecutionPlan using default strategy.
     */
    public ExecutionPlan generatePlan(PlanningContext context) {
        return generatePlan(context, PlanningStrategy.defaultStrategy());
    }

    /**
     * Entry point transforming PlanningContext into an ExecutionPlan using a specified PlanningStrategy.
     */
    public ExecutionPlan generatePlan(PlanningContext context, PlanningStrategy strategy) {
        if (context == null) {
            log.warn("Null PlanningContext provided to PlanningEngine");
            return ExecutionPlan.fallback("plan_" + UUID.randomUUID().toString().substring(0, 8), "Null PlanningContext provided");
        }
        if (strategy == null) {
            strategy = PlanningStrategy.defaultStrategy();
        }

        log.debug("PlanningEngine generating ExecutionPlan for contextId={} with strategy={}",
                context.getContextId(), strategy.getStrategyName());

        return pipeline.execute(context, strategy);
    }
}
