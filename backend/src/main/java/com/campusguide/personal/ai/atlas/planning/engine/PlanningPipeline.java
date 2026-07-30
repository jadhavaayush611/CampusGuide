package com.campusguide.personal.ai.atlas.planning.engine;

import com.campusguide.personal.ai.atlas.planning.constraint.ConstraintResolution;
import com.campusguide.personal.ai.atlas.planning.constraint.PlanningConstraintSolver;
import com.campusguide.personal.ai.atlas.planning.context.PlanningContext;
import com.campusguide.personal.ai.atlas.planning.decomposition.GoalDecomposer;
import com.campusguide.personal.ai.atlas.planning.decomposition.GoalHierarchy;
import com.campusguide.personal.ai.atlas.planning.explanation.PlanningExplanation;
import com.campusguide.personal.ai.atlas.planning.explanation.PlanningExplanationEngine;
import com.campusguide.personal.ai.atlas.planning.graph.DependencyType;
import com.campusguide.personal.ai.atlas.planning.graph.TaskDependency;
import com.campusguide.personal.ai.atlas.planning.graph.TaskGraph;
import com.campusguide.personal.ai.atlas.planning.graph.TaskState;
import com.campusguide.personal.ai.atlas.planning.metrics.PlanningMetrics;
import com.campusguide.personal.ai.atlas.planning.model.*;
import com.campusguide.personal.ai.atlas.planning.optimization.OptimizationResult;
import com.campusguide.personal.ai.atlas.planning.optimization.PlanOptimizer;
import com.campusguide.personal.ai.atlas.planning.scheduling.Schedule;
import com.campusguide.personal.ai.atlas.planning.scheduling.Scheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

/**
 * Pipeline coordinating goal decomposition, task graph generation, constraint solving,
 * scheduling, plan optimization, explainability, and metrics collection.
 */
@Slf4j
@Component("planningPipeline")
public class PlanningPipeline {

    private final GoalDecomposer decomposer;
    private final PlanningConstraintSolver constraintSolver;
    private final Scheduler scheduler;
    private final PlanOptimizer optimizer;
    private final PlanningExplanationEngine explanationEngine;

    public PlanningPipeline(GoalDecomposer decomposer,
                            PlanningConstraintSolver constraintSolver,
                            Scheduler scheduler,
                            PlanOptimizer optimizer,
                            PlanningExplanationEngine explanationEngine) {
        this.decomposer = decomposer;
        this.constraintSolver = constraintSolver;
        this.scheduler = scheduler;
        this.optimizer = optimizer;
        this.explanationEngine = explanationEngine;
    }

    public ExecutionPlan execute(PlanningContext context, PlanningStrategy strategy) {
        long startTime = System.currentTimeMillis();
        String planId = "plan_" + UUID.randomUUID().toString().substring(0, 8);

        if (context == null) {
            log.warn("Null PlanningContext provided to PlanningPipeline");
            return ExecutionPlan.fallback(planId, "Null PlanningContext provided");
        }

        try {
            // 1. Goal Decomposition
            GoalHierarchy hierarchy = decomposer.decompose(context);

            // 2. Task Graph Generation
            TaskGraph taskGraph = generateTaskGraph(hierarchy, context);

            // 3. Constraint Verification & Solving
            ConstraintResolution constraintRes = null;
            if (strategy.isEnableConstraintSolving()) {
                constraintRes = constraintSolver.solve(taskGraph, context);
                if (constraintRes.getAdjustedTaskGraph() != null) {
                    taskGraph = constraintRes.getAdjustedTaskGraph();
                }
            }

            // 4. Scheduling
            long schedStart = System.currentTimeMillis();
            Schedule schedule = scheduler.schedule(taskGraph, context);
            long schedLatency = System.currentTimeMillis() - schedStart;

            // 5. Initial Plan Construction
            List<PlanningTask> orderedTasks = taskGraph.hasCycle() ? taskGraph.getTaskList() : taskGraph.topologicalSort();
            double confidence = context.getDecisionOutcome() != null && context.getDecisionOutcome().getSelectedAction() != null
                    ? context.getDecisionOutcome().getSelectedAction().getConfidenceScore() : 0.85;

            ExecutionPlan plan = ExecutionPlan.builder()
                    .planId(planId)
                    .goal(hierarchy.getRootGoal())
                    .tasks(orderedTasks)
                    .dependencies(taskGraph.getDependencies())
                    .schedule(schedule)
                    .confidence(confidence)
                    .rationale("Deterministic plan synthesized for goal: " + hierarchy.getRootGoal().getTitle())
                    .status(PlanStatus.VALIDATED)
                    .metadata(PlanningMetadata.createDefault(context.getContextId(), strategy.getStrategyName()))
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            // 6. Plan Optimization
            OptimizationResult optResult = null;
            if (strategy.isEnableOptimization()) {
                optResult = optimizer.optimize(plan, taskGraph, context);
                if (optResult.getOptimizedPlan() != null) {
                    plan = optResult.getOptimizedPlan();
                }
            }

            // 7. Explainability Generation
            PlanningExplanation explanation = null;
            if (strategy.isEnableExplainability()) {
                explanation = explanationEngine.generateExplanation(context, hierarchy, taskGraph, constraintRes, schedule, optResult);
                plan.setExplanation(explanation);
            }

            // 8. Compute Operational Metrics (Observability without sensitive data)
            long totalLatency = System.currentTimeMillis() - startTime;
            List<PlanningTask> criticalPath = taskGraph.calculateCriticalPath();

            Map<String, Integer> countsByState = new HashMap<>();
            for (PlanningTask t : orderedTasks) {
                countsByState.merge(t.getState().name(), 1, Integer::sum);
            }

            int violationCount = constraintRes != null ? constraintRes.getViolations().size() : 0;
            double complexityScore = orderedTasks.size() * 1.5 + taskGraph.getDependencies().size() * 2.0;

            PlanningMetrics metrics = PlanningMetrics.builder()
                    .planningLatencyMs(totalLatency)
                    .schedulingLatencyMs(schedLatency)
                    .totalTasks(orderedTasks.size())
                    .totalDependencies(taskGraph.getDependencies().size())
                    .criticalPathLength(criticalPath.size())
                    .optimizationEffectiveness(optResult != null ? optResult.getOverallImprovementRatio() : 0.0)
                    .constraintViolationCount(violationCount)
                    .planComplexityScore(complexityScore)
                    .taskCountsByState(countsByState)
                    .build();

            plan.setMetrics(metrics);
            plan.setStatus(PlanStatus.READY);
            return plan;

        } catch (Exception e) {
            log.error("Error executing PlanningPipeline for contextId={}", context.getContextId(), e);
            return ExecutionPlan.fallback(planId, "Pipeline execution error: " + e.getMessage());
        }
    }

    private TaskGraph generateTaskGraph(GoalHierarchy hierarchy, PlanningContext context) {
        TaskGraph graph = new TaskGraph();
        if (hierarchy == null || hierarchy.getRootGoal() == null) {
            return graph;
        }

        PlanningGoal root = hierarchy.getRootGoal();
        String rootTaskId = "task_root_" + root.getGoalId();
        PlanningTask rootTask = PlanningTask.builder()
                .taskId(rootTaskId)
                .goalId(root.getGoalId())
                .title(root.getTitle())
                .description(root.getDescription())
                .state(TaskState.READY)
                .estimatedDurationMinutes(5.0)
                .mandatory(root.isMandatory())
                .parallelizable(false)
                .conditional(false)
                .steps(createDefaultSteps(rootTaskId, "Initialize"))
                .build();
        graph.addTask(rootTask);

        String lastTaskId = rootTaskId;
        int depCounter = 1;

        for (SubGoal subGoal : hierarchy.getSubGoals()) {
            String subTaskId = "task_" + subGoal.getSubGoalId();
            PlanningTask subTask = PlanningTask.builder()
                    .taskId(subTaskId)
                    .goalId(root.getGoalId())
                    .title(subGoal.getTitle())
                    .description(subGoal.getDescription())
                    .state(TaskState.PENDING)
                    .estimatedDurationMinutes(10.0)
                    .mandatory(subGoal.isMandatory())
                    .parallelizable(!subGoal.isMandatory()) // optional subgoals parallelizable
                    .conditional(!subGoal.isMandatory())
                    .steps(createDefaultSteps(subTaskId, subGoal.getTitle()))
                    .build();
            graph.addTask(subTask);

            // Add dependency link from predecessor to subTask
            TaskDependency dep = TaskDependency.builder()
                    .dependencyId("dep_" + (depCounter++))
                    .predecessorTaskId(lastTaskId)
                    .successorTaskId(subTaskId)
                    .dependencyType(subGoal.isMandatory() ? DependencyType.HARD : DependencyType.SOFT)
                    .lagMinutes(0.0)
                    .build();
            graph.addDependency(dep);

            if (subGoal.isMandatory()) {
                lastTaskId = subTaskId;
            }
        }

        return graph;
    }

    private List<PlanningStep> createDefaultSteps(String taskId, String prefix) {
        List<PlanningStep> steps = new ArrayList<>();
        steps.add(PlanningStep.builder()
                .stepId("step_" + taskId + "_1")
                .taskId(taskId)
                .title(prefix + " - Validate Input")
                .orderIndex(1)
                .stepType("VALIDATION")
                .mandatory(true)
                .status("PENDING")
                .build());
        steps.add(PlanningStep.builder()
                .stepId("step_" + taskId + "_2")
                .taskId(taskId)
                .title(prefix + " - Execute Action")
                .orderIndex(2)
                .stepType("EXECUTION")
                .mandatory(true)
                .status("PENDING")
                .build());
        return steps;
    }
}
