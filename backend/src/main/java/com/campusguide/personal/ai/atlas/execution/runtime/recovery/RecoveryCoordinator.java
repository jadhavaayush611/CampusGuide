package com.campusguide.personal.ai.atlas.execution.runtime.recovery;

import com.campusguide.personal.ai.atlas.execution.runtime.checkpoint.CheckpointManager;
import com.campusguide.personal.ai.atlas.execution.runtime.checkpoint.RuntimeCheckpoint;
import com.campusguide.personal.ai.atlas.execution.runtime.human.ExecutionControlService;
import com.campusguide.personal.ai.atlas.execution.runtime.rollback.RollbackExecutor;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Coordinates recovery decisions and execution upon workflow or stage failure.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecoveryCoordinator {

    private final CheckpointManager checkpointManager;
    private final RollbackExecutor rollbackExecutor;
    private final ExecutionControlService controlService;

    public RecoveryDecision evaluateAndRecover(WorkflowInstance instance, String failedUnitId, String error) {
        if (instance == null) {
            return RecoveryDecision.rollback("Cannot recover null WorkflowInstance");
        }

        log.warn("Evaluating recovery for workflow instance {} unit {} (Error: {})",
                instance.getInstanceId(), failedUnitId, error);

        // Check if latest checkpoint exists
        Optional<RuntimeCheckpoint> latestCheckpoint = checkpointManager.getLatestCheckpoint(instance.getWorkflowId());
        if (latestCheckpoint.isPresent()) {
            String chkId = latestCheckpoint.get().getCheckpointId();
            log.info("Restoring workflow instance {} from checkpoint {}", instance.getInstanceId(), chkId);
            checkpointManager.restoreFromCheckpoint(instance, chkId);
            return RecoveryDecision.restoreCheckpoint(chkId, "Restored to checkpoint " + chkId + " following error: " + error);
        }

        // If rollback plan present, execute rollback
        log.info("Executing rollback for workflow instance {} following unrecoverable failure", instance.getInstanceId());
        boolean rolledBack = rollbackExecutor.rollbackWorkflow(instance, "Workflow failure recovery triggered rollback: " + error);

        if (!rolledBack) {
            controlService.recordIntervention(instance.getWorkflowId(), instance.getInstanceId(), failedUnitId,
                    "FAILURE_RECOVERY", "system", "WAIT_FOR_HUMAN", "Rollback failed or incomplete");
            return RecoveryDecision.waitForHuman("Rollback failed, human intervention required");
        }

        return RecoveryDecision.rollback("Rollback executed successfully for workflow " + instance.getWorkflowId());
    }
}
