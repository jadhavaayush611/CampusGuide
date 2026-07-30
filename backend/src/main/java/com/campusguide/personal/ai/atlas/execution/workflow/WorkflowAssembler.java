package com.campusguide.personal.ai.atlas.execution.workflow;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.CheckpointType;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionCheckpoint;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionRetryPolicy;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionRollbackPolicy;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionStage;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnit;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnitType;
import com.campusguide.personal.ai.atlas.execution.model.StageCompletionPolicy;
import com.campusguide.personal.ai.atlas.execution.model.UnitPreparationStatus;
import com.campusguide.personal.ai.atlas.planning.graph.TaskDependency;
import com.campusguide.personal.ai.atlas.planning.model.ExecutionPlan;
import com.campusguide.personal.ai.atlas.planning.model.PlanningStep;
import com.campusguide.personal.ai.atlas.planning.model.PlanningTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Assembles ExecutionStage, ExecutionUnit, and ExecutionCheckpoint objects
 * from an ExecutionPlan and ExecutionContext.
 */
@Slf4j
@Component
public class WorkflowAssembler {

    public List<ExecutionStage> assembleStages(ExecutionContext context) {
        ExecutionPlan plan = context.getExecutionPlan();
        if (plan == null || plan.getTasks() == null || plan.getTasks().isEmpty()) {
            return createDefaultStage();
        }

        List<ExecutionStage> stages = new ArrayList<>();
        List<PlanningTask> tasks = plan.getTasks();
        Map<String, List<String>> taskDependencies = mapDependencies(plan.getDependencies());

        int stageOrder = 1;
        for (PlanningTask task : tasks) {
            String stageId = "stage_" + task.getTaskId();
            List<ExecutionUnit> units = assembleUnitsForTask(task, taskDependencies.getOrDefault(task.getTaskId(), new ArrayList<>()));

            ExecutionStage stage = ExecutionStage.builder()
                    .stageId(stageId)
                    .stageName("Stage - " + task.getTitle())
                    .orderIndex(stageOrder++)
                    .executionUnits(units)
                    .parallel(task.isParallelizable())
                    .completionPolicy(StageCompletionPolicy.ALL_MUST_SUCCEED)
                    .stagePrecondition(task.getPrecondition())
                    .build();

            stages.add(stage);
        }

        return stages;
    }

    public List<ExecutionCheckpoint> assembleCheckpoints(List<ExecutionStage> stages) {
        List<ExecutionCheckpoint> checkpoints = new ArrayList<>();
        if (stages == null || stages.isEmpty()) {
            return checkpoints;
        }

        for (ExecutionStage stage : stages) {
            // Pre-stage checkpoint
            ExecutionCheckpoint preCheckpoint = ExecutionCheckpoint.builder()
                    .checkpointId("chk_pre_" + stage.getStageId())
                    .stageId(stage.getStageId())
                    .checkpointName("Pre-Stage Verification: " + stage.getStageName())
                    .type(CheckpointType.PRE_STAGE)
                    .rollbackTrigger(false)
                    .requiredApproval(false)
                    .build();

            // Post-stage checkpoint
            ExecutionCheckpoint postCheckpoint = ExecutionCheckpoint.builder()
                    .checkpointId("chk_post_" + stage.getStageId())
                    .stageId(stage.getStageId())
                    .checkpointName("Post-Stage Verification: " + stage.getStageName())
                    .type(CheckpointType.POST_STAGE)
                    .rollbackTrigger(true)
                    .requiredApproval(false)
                    .build();

            checkpoints.add(preCheckpoint);
            checkpoints.add(postCheckpoint);
        }

        return checkpoints;
    }

    private List<ExecutionUnit> assembleUnitsForTask(PlanningTask task, List<String> predTaskIds) {
        List<ExecutionUnit> units = new ArrayList<>();

        if (task.getSteps() != null && !task.getSteps().isEmpty()) {
            for (PlanningStep step : task.getSteps()) {
                ExecutionUnit unit = ExecutionUnit.builder()
                        .unitId("unit_" + step.getStepId())
                        .taskId(task.getTaskId())
                        .stepId(step.getStepId())
                        .title(step.getTitle())
                        .description("Step in task: " + task.getTitle())
                        .unitType(determineUnitType(step.getStepType()))
                        .targetCapability(extractCapability(step))
                        .payload(step.getParameters() != null ? step.getParameters() : new HashMap<>())
                        .dependencies(predTaskIds)
                        .timeoutSeconds(60L)
                        .retryPolicy(ExecutionRetryPolicy.defaultConfig())
                        .rollbackPolicy(ExecutionRollbackPolicy.defaultConfig())
                        .mandatory(step.isMandatory())
                        .status(UnitPreparationStatus.READY)
                        .build();
                units.add(unit);
            }
        } else {
            // Task has no steps; convert task itself into a single ExecutionUnit
            ExecutionUnit unit = ExecutionUnit.builder()
                    .unitId("unit_" + task.getTaskId())
                    .taskId(task.getTaskId())
                    .title(task.getTitle())
                    .description(task.getDescription())
                    .unitType(ExecutionUnitType.ACTION)
                    .targetCapability("cap_" + task.getTaskId())
                    .payload(task.getExecutionPayload() != null ? task.getExecutionPayload() : new HashMap<>())
                    .dependencies(predTaskIds)
                    .timeoutSeconds(60L)
                    .retryPolicy(ExecutionRetryPolicy.defaultConfig())
                    .rollbackPolicy(ExecutionRollbackPolicy.defaultConfig())
                    .mandatory(task.isMandatory())
                    .status(UnitPreparationStatus.READY)
                    .build();
            units.add(unit);
        }

        return units;
    }

    private Map<String, List<String>> mapDependencies(List<TaskDependency> dependencies) {
        Map<String, List<String>> map = new HashMap<>();
        if (dependencies == null) {
            return map;
        }
        for (TaskDependency dep : dependencies) {
            map.computeIfAbsent(dep.getSuccessorTaskId(), k -> new ArrayList<>()).add(dep.getPredecessorTaskId());
        }
        return map;
    }

    private ExecutionUnitType determineUnitType(String stepType) {
        if (stepType == null) return ExecutionUnitType.ACTION;
        String upper = stepType.toUpperCase();
        if (upper.contains("QUERY") || upper.contains("READ")) return ExecutionUnitType.QUERY;
        if (upper.contains("API") || upper.contains("CALL")) return ExecutionUnitType.API_CALL;
        if (upper.contains("NOTIFY") || upper.contains("EMAIL")) return ExecutionUnitType.NOTIFICATION;
        if (upper.contains("MUTAT") || upper.contains("WRITE") || upper.contains("UPDATE")) return ExecutionUnitType.MUTATION;
        if (upper.contains("VALIDAT") || upper.contains("CHECK")) return ExecutionUnitType.VALIDATION;
        if (upper.contains("COMPUT")) return ExecutionUnitType.COMPUTATION;
        return ExecutionUnitType.ACTION;
    }

    private String extractCapability(PlanningStep step) {
        if (step.getParameters() != null && step.getParameters().containsKey("capability")) {
            return String.valueOf(step.getParameters().get("capability"));
        }
        return "cap_" + step.getStepId();
    }

    private List<ExecutionStage> createDefaultStage() {
        ExecutionUnit unit = ExecutionUnit.builder()
                .unitId("unit_default")
                .taskId("task_default")
                .title("Default Execution Step")
                .description("Default initial step for plan execution")
                .unitType(ExecutionUnitType.ACTION)
                .status(UnitPreparationStatus.READY)
                .build();

        ExecutionStage stage = ExecutionStage.builder()
                .stageId("stage_default")
                .stageName("Default Stage")
                .orderIndex(1)
                .executionUnits(List.of(unit))
                .parallel(false)
                .completionPolicy(StageCompletionPolicy.ALL_MUST_SUCCEED)
                .build();

        return List.of(stage);
    }
}
