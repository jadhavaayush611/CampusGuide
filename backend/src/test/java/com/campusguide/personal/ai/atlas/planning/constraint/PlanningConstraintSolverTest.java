package com.campusguide.personal.ai.atlas.planning.constraint;

import com.campusguide.personal.ai.atlas.decision.model.DecisionOutcome;
import com.campusguide.personal.ai.atlas.planning.context.PlanningContext;
import com.campusguide.personal.ai.atlas.planning.graph.TaskDependency;
import com.campusguide.personal.ai.atlas.planning.graph.TaskGraph;
import com.campusguide.personal.ai.atlas.planning.model.PlanningTask;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlanningConstraintSolverTest {

    @Test
    @DisplayName("ConstraintSolver should satisfy valid acyclic task graph")
    void testConstraintSolverValidGraph() {
        PlanningConstraintSolver solver = new PlanningConstraintSolver();
        TaskGraph graph = new TaskGraph();

        PlanningTask t1 = PlanningTask.builder().taskId("t1").estimatedDurationMinutes(5.0).build();
        graph.addTask(t1);

        PlanningContext context = PlanningContext.fromDecisionOutcome(DecisionOutcome.fallback("out_c", "Constraint test"));

        ConstraintResolution resolution = solver.solve(graph, context);
        assertThat(resolution.isSatisfied()).isTrue();
        assertThat(resolution.getViolations()).isEmpty();
    }

    @Test
    @DisplayName("ConstraintSolver should report violation on cyclic graph")
    void testConstraintSolverCycle() {
        PlanningConstraintSolver solver = new PlanningConstraintSolver();
        TaskGraph graph = new TaskGraph();

        PlanningTask t1 = PlanningTask.builder().taskId("t1").build();
        PlanningTask t2 = PlanningTask.builder().taskId("t2").build();
        graph.addTask(t1);
        graph.addTask(t2);

        graph.addDependency(TaskDependency.builder().dependencyId("d1").predecessorTaskId("t1").successorTaskId("t2").build());
        graph.addDependency(TaskDependency.builder().dependencyId("d2").predecessorTaskId("t2").successorTaskId("t1").build());

        PlanningContext context = PlanningContext.fromDecisionOutcome(DecisionOutcome.fallback("out_c2", "Constraint test cycle"));

        ConstraintResolution resolution = solver.solve(graph, context);
        assertThat(resolution.isSatisfied()).isFalse();
        assertThat(resolution.getViolations()).isNotEmpty();
        assertThat(resolution.getViolations().get(0).getType()).isEqualTo(ConstraintType.DEPENDENCY);
    }
}
