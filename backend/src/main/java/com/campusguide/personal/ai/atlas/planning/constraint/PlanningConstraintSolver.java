package com.campusguide.personal.ai.atlas.planning.constraint;

import com.campusguide.personal.ai.atlas.planning.context.PlanningContext;
import com.campusguide.personal.ai.atlas.planning.graph.TaskDependency;
import com.campusguide.personal.ai.atlas.planning.graph.TaskGraph;
import com.campusguide.personal.ai.atlas.planning.model.PlanningTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Constraint Solver supporting temporal, dependency, resource, policy, and scheduling constraints.
 */
@Slf4j
@Component("planningConstraintSolver")
public class PlanningConstraintSolver {

    public ConstraintResolution solve(TaskGraph taskGraph, PlanningContext context) {
        if (taskGraph == null) {
            log.warn("Null TaskGraph provided to PlanningConstraintSolver");
            return ConstraintResolution.builder()
                    .satisfied(false)
                    .resolutionNotes("Null task graph")
                    .build();
        }

        List<ConstraintViolationInfo> violations = new ArrayList<>();

        // 1. Dependency Invariant: Cycle check
        if (taskGraph.hasCycle()) {
            violations.add(ConstraintViolationInfo.builder()
                    .constraintId("c_dep_cycle")
                    .type(ConstraintType.DEPENDENCY)
                    .message("Task graph contains cyclic dependencies")
                    .hardViolation(true)
                    .build());
        }

        // 2. Policy Constraint: Restricted Action Types
        if (context != null && context.getConstraints() != null) {
            Set<String> restricted = context.getConstraints().getRestrictedActionTypes();
            if (restricted != null && !restricted.isEmpty()) {
                for (PlanningTask t : taskGraph.getTaskList()) {
                    if (t.getExecutionPayload() != null && t.getExecutionPayload().containsKey("actionType")) {
                        String act = String.valueOf(t.getExecutionPayload().get("actionType"));
                        if (restricted.contains(act.toUpperCase())) {
                            violations.add(ConstraintViolationInfo.builder()
                                    .constraintId("c_pol_restricted")
                                    .type(ConstraintType.POLICY)
                                    .taskId(t.getTaskId())
                                    .message("Task contains restricted action type: " + act)
                                    .hardViolation(true)
                                    .build());
                        }
                    }
                }
            }
        }

        // 3. Temporal & Resource Constraints
        if (!taskGraph.hasCycle() && context != null && context.getConstraints() != null) {
            int maxParallel = context.getConstraints().getMaxParallelTasks();
            for (List<PlanningTask> batch : taskGraph.getParallelBatches()) {
                if (batch.size() > maxParallel) {
                    violations.add(ConstraintViolationInfo.builder()
                            .constraintId("c_res_parallel")
                            .type(ConstraintType.RESOURCE)
                            .message("Parallel task batch size (" + batch.size() + ") exceeds maximum capacity (" + maxParallel + ")")
                            .hardViolation(false) // Soft violation
                            .build());
                }
            }

            // Duration check against time horizon
            if (context.getTimeHorizon() != null) {
                double maxDuration = context.getTimeHorizon().getMaxDurationMinutes();
                double totalEstimated = 0.0;
                for (PlanningTask t : taskGraph.getTaskList()) {
                    totalEstimated += t.getEstimatedDurationMinutes();
                }
                if (totalEstimated > maxDuration * 2.0) { // Slack margin
                    violations.add(ConstraintViolationInfo.builder()
                            .constraintId("c_temp_horizon")
                            .type(ConstraintType.TEMPORAL)
                            .message("Total estimated task duration (" + totalEstimated + " min) exceeds time horizon (" + maxDuration + " min)")
                            .hardViolation(false)
                            .build());
                }
            }
        }

        // 4. Scheduling Invariant: Valid Predecessor Links
        for (TaskDependency dep : taskGraph.getDependencies()) {
            if (taskGraph.getTask(dep.getPredecessorTaskId()) == null || taskGraph.getTask(dep.getSuccessorTaskId()) == null) {
                violations.add(ConstraintViolationInfo.builder()
                        .constraintId("c_sched_dangling")
                        .type(ConstraintType.SCHEDULING)
                        .message("Dangling dependency link from " + dep.getPredecessorTaskId() + " to " + dep.getSuccessorTaskId())
                        .hardViolation(true)
                        .build());
            }
        }

        boolean hasHardViolation = violations.stream().anyMatch(ConstraintViolationInfo::isHardViolation);
        boolean satisfied = !hasHardViolation;

        return ConstraintResolution.builder()
                .satisfied(satisfied)
                .violations(violations)
                .adjustedTaskGraph(taskGraph)
                .resolutionNotes(satisfied ? "All mandatory constraints satisfied" : "Constraint violations detected: " + violations.size())
                .build();
    }
}
