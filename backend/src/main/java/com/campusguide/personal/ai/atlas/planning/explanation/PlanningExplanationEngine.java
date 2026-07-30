package com.campusguide.personal.ai.atlas.planning.explanation;

import com.campusguide.personal.ai.atlas.decision.model.DecisionOutcome;
import com.campusguide.personal.ai.atlas.planning.constraint.ConstraintResolution;
import com.campusguide.personal.ai.atlas.planning.context.PlanningContext;
import com.campusguide.personal.ai.atlas.planning.decomposition.GoalHierarchy;
import com.campusguide.personal.ai.atlas.planning.graph.TaskDependency;
import com.campusguide.personal.ai.atlas.planning.graph.TaskGraph;
import com.campusguide.personal.ai.atlas.planning.optimization.OptimizationResult;
import com.campusguide.personal.ai.atlas.planning.scheduling.Schedule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Engine generating structured, deterministic explainability for ExecutionPlan.
 * Explains goal decomposition, task ordering, dependency reasoning, scheduling rationale,
 * optimization rationale, and supporting evidence.
 */
@Slf4j
@Component("planningExplanationEngine")
public class PlanningExplanationEngine {

    public PlanningExplanation generateExplanation(PlanningContext context,
                                                   GoalHierarchy hierarchy,
                                                   TaskGraph taskGraph,
                                                   ConstraintResolution constraintResolution,
                                                   Schedule schedule,
                                                   OptimizationResult optimizationResult) {

        String planId = context != null ? context.getContextId() : "plan_unknown";
        List<PlanningReason> reasons = new ArrayList<>();
        List<PlanningEvidence> evidenceList = new ArrayList<>();

        // 1. Goal Decomposition Rationale
        String primaryRationale = "Plan generated deterministically from DecisionOutcome for goal: "
                + (context != null && context.getObjective() != null ? context.getObjective().getPrimaryGoal() : "Default Goal");

        reasons.add(PlanningReason.builder()
                .reasonId("r_decomp_01")
                .category(ReasonCategory.GOAL_DECOMPOSITION)
                .summary("Goal decomposed into " + (hierarchy != null ? hierarchy.getTotalGoalCount() : 1) + " hierarchical goals")
                .impact("Establishes clear goal-subgoal alignment")
                .build());

        // 2. Task Ordering Rationale
        int taskCount = taskGraph != null ? taskGraph.getTasks().size() : 0;
        String orderingRationale = "Tasks topologically ordered into " + taskCount + " deterministic execution steps";
        reasons.add(PlanningReason.builder()
                .reasonId("r_order_01")
                .category(ReasonCategory.TASK_ORDERING)
                .summary("Topological DAG sorting applied to prevent execution deadlock")
                .impact("Guarantees valid execution sequence")
                .build());

        // 3. Dependency Reasoning
        int depCount = taskGraph != null ? taskGraph.getDependencies().size() : 0;
        String dependencyReasoning = "Resolved " + depCount + " hard and soft task dependencies cleanly";
        if (taskGraph != null) {
            for (TaskDependency dep : taskGraph.getDependencies()) {
                reasons.add(PlanningReason.builder()
                        .reasonId("r_dep_" + dep.getDependencyId())
                        .category(ReasonCategory.DEPENDENCY)
                        .summary("Task " + dep.getSuccessorTaskId() + " depends on " + dep.getPredecessorTaskId() + " (" + dep.getDependencyType() + ")")
                        .impact("Enforces prerequisite ordering constraint")
                        .build());
            }
        }

        // 4. Scheduling Rationale
        String strategyUsed = schedule != null ? schedule.getStrategyUsed() : "EARLIEST_COMPLETION";
        double duration = schedule != null ? schedule.getTotalDurationMinutes() : 0.0;
        String schedulingRationale = "Scheduled using " + strategyUsed + " strategy with total estimated makespan of " + duration + " minutes";
        reasons.add(PlanningReason.builder()
                .reasonId("r_sched_01")
                .category(ReasonCategory.SCHEDULING)
                .summary(schedulingRationale)
                .impact(schedule != null && schedule.isMeetsDeadline() ? "Satisfies target deadline" : "Requires deadline review")
                .build());

        // 5. Optimization Rationale
        String optSummary = optimizationResult != null && !optimizationResult.getAppliedOptimizations().isEmpty()
                ? String.join(", ", optimizationResult.getAppliedOptimizations()) : "Standard unoptimized plan";
        String optimizationRationale = "Applied optimizations: " + optSummary;
        reasons.add(PlanningReason.builder()
                .reasonId("r_opt_01")
                .category(ReasonCategory.OPTIMIZATION)
                .summary(optimizationRationale)
                .impact("Saved ~" + (optimizationResult != null ? optimizationResult.getTimeSavedMinutes() : 0) + " minutes")
                .build());

        // 6. Evidence Adaptation
        if (context != null && context.getDecisionOutcome() != null) {
            DecisionOutcome outcome = context.getDecisionOutcome();
            evidenceList.add(PlanningEvidence.builder()
                    .evidenceId("ev_decision_" + outcome.getOutcomeId())
                    .source("DecisionEngine")
                    .type("DECISION_OUTCOME")
                    .description("Selected action: " + (outcome.getSelectedAction() != null ? outcome.getSelectedAction().getCandidateId() : "N/A"))
                    .relevanceScore(1.0)
                    .build());
        }

        return PlanningExplanation.builder()
                .planId(planId)
                .primaryRationale(primaryRationale)
                .orderingRationale(orderingRationale)
                .dependencyReasoning(dependencyReasoning)
                .schedulingRationale(schedulingRationale)
                .optimizationRationale(optimizationRationale)
                .reasons(reasons)
                .evidenceList(evidenceList)
                .build();
    }
}
