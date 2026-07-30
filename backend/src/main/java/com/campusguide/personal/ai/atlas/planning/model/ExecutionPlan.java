package com.campusguide.personal.ai.atlas.planning.model;

import com.campusguide.personal.ai.atlas.planning.explanation.PlanningExplanation;
import com.campusguide.personal.ai.atlas.planning.graph.TaskDependency;
import com.campusguide.personal.ai.atlas.planning.metrics.PlanningMetrics;
import com.campusguide.personal.ai.atlas.planning.scheduling.Schedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates goals, ordered tasks, dependencies, schedules, confidence,
 * rationale, explanation, and metadata while remaining execution-independent.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    private String planId;
    private PlanningGoal goal;

    @Builder.Default
    private List<PlanningTask> tasks = new ArrayList<>();

    @Builder.Default
    private List<TaskDependency> dependencies = new ArrayList<>();

    private Schedule schedule;
    private double confidence;
    private String rationale;
    private PlanningExplanation explanation;
    private PlanningMetadata metadata;
    private PlanningMetrics metrics;

    @Builder.Default
    private PlanStatus status = PlanStatus.DRAFT;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();

    public static ExecutionPlan fallback(String planId, String rationale) {
        PlanningGoal fallbackGoal = PlanningGoal.builder()
                .goalId("goal_fallback")
                .title("Fallback Goal")
                .description("Fallback plan generated due to error or missing context")
                .priority(1)
                .mandatory(true)
                .state(GoalState.IDENTIFIED)
                .build();

        PlanningTask fallbackTask = PlanningTask.builder()
                .taskId("task_fallback")
                .goalId("goal_fallback")
                .title("Fallback Execution Step")
                .description(rationale)
                .estimatedDurationMinutes(5.0)
                .mandatory(true)
                .parallelizable(false)
                .conditional(false)
                .build();

        return ExecutionPlan.builder()
                .planId(planId)
                .goal(fallbackGoal)
                .tasks(Collections.singletonList(fallbackTask))
                .dependencies(Collections.emptyList())
                .confidence(0.1)
                .rationale(rationale)
                .status(PlanStatus.DEGRADED)
                .metadata(PlanningMetadata.createDefault(null, "FALLBACK"))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
