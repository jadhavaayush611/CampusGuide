package com.campusguide.personal.ai.atlas.planning.model;

import com.campusguide.personal.ai.atlas.planning.graph.TaskDependency;
import com.campusguide.personal.ai.atlas.planning.graph.TaskState;
import com.campusguide.personal.ai.atlas.planning.metrics.PlanningMetrics;
import com.campusguide.personal.ai.atlas.planning.scheduling.Schedule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class PlanningModelTest {

    @Test
    @DisplayName("ExecutionPlan fallback should produce valid degraded plan")
    void testExecutionPlanFallback() {
        ExecutionPlan plan = ExecutionPlan.fallback("plan_fb_1", "Low confidence decision");

        assertThat(plan).isNotNull();
        assertThat(plan.getPlanId()).isEqualTo("plan_fb_1");
        assertThat(plan.getStatus()).isEqualTo(PlanStatus.DEGRADED);
        assertThat(plan.getConfidence()).isEqualTo(0.1);
        assertThat(plan.getTasks()).hasSize(1);
        assertThat(plan.getGoal()).isNotNull();
        assertThat(plan.getGoal().getTitle()).isEqualTo("Fallback Goal");
    }

    @Test
    @DisplayName("PlanningGoal and SubGoal builder and state transitions")
    void testPlanningGoalAndSubGoal() {
        SubGoal subGoal = SubGoal.builder()
                .subGoalId("sub_1")
                .parentGoalId("goal_1")
                .title("Sub Task")
                .mandatory(true)
                .fulfilled(false)
                .weight(0.5)
                .state(GoalState.IDENTIFIED)
                .build();

        PlanningGoal goal = PlanningGoal.builder()
                .goalId("goal_1")
                .title("Parent Goal")
                .priority(8)
                .mandatory(true)
                .state(GoalState.DECOMPOSED)
                .subGoals(Collections.singletonList(subGoal))
                .build();

        assertThat(goal.getGoalId()).isEqualTo("goal_1");
        assertThat(goal.getSubGoals()).hasSize(1);
        assertThat(goal.getSubGoals().get(0).getSubGoalId()).isEqualTo("sub_1");
    }

    @Test
    @DisplayName("PlanningTask, PlanningStep and PlanningMetadata defaults")
    void testPlanningTaskAndStep() {
        PlanningStep step = PlanningStep.builder()
                .stepId("step_1")
                .taskId("task_1")
                .title("Step 1")
                .orderIndex(1)
                .mandatory(true)
                .status("PENDING")
                .build();

        PlanningTask task = PlanningTask.builder()
                .taskId("task_1")
                .goalId("goal_1")
                .title("Main Task")
                .state(TaskState.READY)
                .estimatedDurationMinutes(15.0)
                .mandatory(true)
                .steps(Collections.singletonList(step))
                .build();

        PlanningMetadata metadata = PlanningMetadata.createDefault("trace_100", "DEFAULT");

        assertThat(task.getTaskId()).isEqualTo("task_1");
        assertThat(task.getSteps()).hasSize(1);
        assertThat(metadata.getTraceId()).isEqualTo("trace_100");
        assertThat(metadata.getVersion()).isEqualTo("1.0.0");
    }
}
