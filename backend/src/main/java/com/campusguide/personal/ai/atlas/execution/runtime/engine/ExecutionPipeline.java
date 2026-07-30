package com.campusguide.personal.ai.atlas.execution.runtime.engine;

import com.campusguide.personal.ai.atlas.execution.model.CheckpointType;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionStage;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnit;
import com.campusguide.personal.ai.atlas.execution.model.StageCompletionPolicy;
import com.campusguide.personal.ai.atlas.execution.runtime.checkpoint.CheckpointManager;
import com.campusguide.personal.ai.atlas.execution.runtime.tool.ToolResult;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Pipeline processor orchestrating stage execution, checkpointing, and completion validation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExecutionPipeline {

    private final ExecutionDispatcher dispatcher;
    private final CheckpointManager checkpointManager;

    public boolean processStage(WorkflowInstance instance, ExecutionStage stage) {
        if (instance == null || stage == null) {
            return false;
        }

        log.info("Processing stage {} ({}) for workflow {}", stage.getStageId(), stage.getStageName(), instance.getWorkflowId());

        // Pre-stage checkpoint
        checkpointManager.createCheckpoint(instance, stage.getStageId(), CheckpointType.PRE_STAGE, "Pre-stage checkpoint for " + stage.getStageId());

        // Execute units
        Map<String, ToolResult> results = dispatcher.dispatchUnits(instance, stage);

        // Evaluate stage completion policy
        boolean stageSuccess = evaluateCompletionPolicy(stage, results);

        // Post-stage checkpoint
        checkpointManager.createCheckpoint(instance, stage.getStageId(), CheckpointType.POST_STAGE, "Post-stage checkpoint for " + stage.getStageId());

        if (stageSuccess) {
            instance.getSession().markStageCompleted(stage.getStageId());
            log.info("Stage {} completed successfully", stage.getStageId());
        } else {
            log.warn("Stage {} failed stage completion policy {}", stage.getStageId(), stage.getCompletionPolicy());
        }

        return stageSuccess;
    }

    private boolean evaluateCompletionPolicy(ExecutionStage stage, Map<String, ToolResult> results) {
        List<ExecutionUnit> units = stage.getExecutionUnits();
        if (units == null || units.isEmpty()) {
            return true;
        }

        long successCount = units.stream()
                .filter(u -> {
                    ToolResult r = results.get(u.getUnitId());
                    return r != null && r.getStatus() != null && r.getStatus().isSuccess();
                })
                .count();

        StageCompletionPolicy policy = stage.getCompletionPolicy() != null ? stage.getCompletionPolicy() : StageCompletionPolicy.ALL_MUST_SUCCEED;

        switch (policy) {
            case ANY_MUST_SUCCEED:
            case AT_LEAST_ONE:
                return successCount >= 1;
            case MAJORITY_MUST_SUCCEED:
                return successCount > (units.size() / 2);
            case ALL_MUST_SUCCEED:
            default:
                return successCount == units.size();
        }
    }
}
