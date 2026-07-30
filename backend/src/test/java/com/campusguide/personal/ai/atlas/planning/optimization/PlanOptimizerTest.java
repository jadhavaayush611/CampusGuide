package com.campusguide.personal.ai.atlas.planning.optimization;

import com.campusguide.personal.ai.atlas.decision.model.DecisionOutcome;
import com.campusguide.personal.ai.atlas.planning.context.PlanningContext;
import com.campusguide.personal.ai.atlas.planning.graph.TaskGraph;
import com.campusguide.personal.ai.atlas.planning.model.ExecutionPlan;
import com.campusguide.personal.ai.atlas.planning.optimization.strategy.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class PlanOptimizerTest {

    @Test
    @DisplayName("PlanOptimizer should apply requested optimization strategy")
    void testPlanOptimizer() {
        PlanOptimizer optimizer = new PlanOptimizer(Arrays.asList(
                new CompletionTimeOptimizationStrategy(),
                new DependencyReductionOptimizationStrategy(),
                new ResourceUtilizationOptimizationStrategy(),
                new UserConvenienceOptimizationStrategy(),
                new PlanSimplicityOptimizationStrategy()
        ));

        ExecutionPlan plan = ExecutionPlan.fallback("plan_opt_1", "Optimization rationale");
        TaskGraph graph = new TaskGraph();
        PlanningContext context = PlanningContext.fromDecisionOutcome(DecisionOutcome.fallback("out_opt", "Opt context"));

        OptimizationResult result = optimizer.optimize(plan, graph, context);
        assertThat(result).isNotNull();
        assertThat(result.getOptimizedPlan()).isNotNull();
        assertThat(result.getAppliedOptimizations()).isNotEmpty();
    }
}
