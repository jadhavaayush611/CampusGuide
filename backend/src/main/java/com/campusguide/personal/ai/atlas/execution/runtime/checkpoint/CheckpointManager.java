package com.campusguide.personal.ai.atlas.execution.runtime.checkpoint;

import com.campusguide.personal.ai.atlas.execution.model.CheckpointType;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowInstance;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages creation, storage, retrieval, and restoration of execution checkpoints.
 */
@Slf4j
@Component
public class CheckpointManager {

    private final Map<String, List<RuntimeCheckpoint>> workflowCheckpoints = new ConcurrentHashMap<>();

    public RuntimeCheckpoint createCheckpoint(WorkflowInstance instance, String stageId, CheckpointType type, String description) {
        if (instance == null) {
            log.warn("Cannot create checkpoint for null WorkflowInstance");
            return null;
        }

        WorkflowSession session = instance.getSession();
        StateSnapshot snapshot = StateSnapshot.builder()
                .snapshotId("snap_" + UUID.randomUUID().toString().substring(0, 8))
                .workflowId(instance.getWorkflowId())
                .currentStageIndex(instance.getCurrentStageIndex())
                .sessionVariables(new HashMap<>(session.getVariables()))
                .unitResults(new HashMap<>(session.getUnitResults()))
                .completedUnitIds(new HashSet<>(session.getCompletedUnitIds()))
                .completedStageIds(new HashSet<>(session.getCompletedStageIds()))
                .state(instance.getState())
                .build();

        RuntimeCheckpoint checkpoint = RuntimeCheckpoint.builder()
                .checkpointId("chk_" + UUID.randomUUID().toString().substring(0, 8))
                .workflowId(instance.getWorkflowId())
                .stageId(stageId)
                .checkpointType(type != null ? type : CheckpointType.PRE_STAGE)
                .snapshot(snapshot)
                .description(description)
                .build();

        workflowCheckpoints.computeIfAbsent(instance.getWorkflowId(), k -> new ArrayList<>()).add(checkpoint);
        instance.addCheckpoint(checkpoint);

        log.info("Saved checkpoint {} for workflow {} stage {}", checkpoint.getCheckpointId(), instance.getWorkflowId(), stageId);
        return checkpoint;
    }

    public Optional<RuntimeCheckpoint> getLatestCheckpoint(String workflowId) {
        List<RuntimeCheckpoint> checkpoints = workflowCheckpoints.get(workflowId);
        if (checkpoints == null || checkpoints.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(checkpoints.get(checkpoints.size() - 1));
    }

    public List<RuntimeCheckpoint> getCheckpointsForWorkflow(String workflowId) {
        return workflowCheckpoints.getOrDefault(workflowId, new ArrayList<>());
    }

    public boolean restoreFromCheckpoint(WorkflowInstance instance, String checkpointId) {
        if (instance == null || checkpointId == null) {
            return false;
        }

        List<RuntimeCheckpoint> checkpoints = workflowCheckpoints.get(instance.getWorkflowId());
        if (checkpoints == null) {
            return false;
        }

        RuntimeCheckpoint target = checkpoints.stream()
                .filter(c -> checkpointId.equals(c.getCheckpointId()))
                .findFirst()
                .orElse(null);

        if (target == null || target.getSnapshot() == null) {
            log.warn("Checkpoint {} not found for workflow {}", checkpointId, instance.getWorkflowId());
            return false;
        }

        StateSnapshot snapshot = target.getSnapshot();
        instance.setCurrentStageIndex(snapshot.getCurrentStageIndex());

        WorkflowSession session = instance.getSession();
        session.getVariables().clear();
        session.getVariables().putAll(snapshot.getSessionVariables());

        session.getUnitResults().clear();
        session.getUnitResults().putAll(snapshot.getUnitResults());

        session.getCompletedUnitIds().clear();
        session.getCompletedUnitIds().addAll(snapshot.getCompletedUnitIds());

        session.getCompletedStageIds().clear();
        session.getCompletedStageIds().addAll(snapshot.getCompletedStageIds());

        instance.addLog("Restored state from checkpoint: " + checkpointId);
        log.info("Restored workflow instance {} to stage index {} using checkpoint {}",
                instance.getInstanceId(), snapshot.getCurrentStageIndex(), checkpointId);
        return true;
    }
}
