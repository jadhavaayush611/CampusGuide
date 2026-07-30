package com.campusguide.personal.ai.atlas.execution.runtime.engine;

import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionStage;
import com.campusguide.personal.ai.atlas.execution.runtime.recovery.RecoveryCoordinator;
import com.campusguide.personal.ai.atlas.execution.runtime.recovery.RecoveryDecision;
import com.campusguide.personal.ai.atlas.execution.runtime.statemachine.ExecutionStateMachine;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowInstance;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Coordinates stage-by-stage workflow execution, managing stage ordering, preconditions, and failure handling.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExecutionCoordinator {

    private final ExecutionPipeline pipeline;
    private final ExecutionStateMachine stateMachine;
    private final RecoveryCoordinator recoveryCoordinator;

    public boolean executeWorkflow(WorkflowInstance instance) {
        if (instance == null || instance.getWorkflow() == null) {
            log.error("Cannot execute null WorkflowInstance or ExecutableWorkflow");
            return false;
        }

        ExecutableWorkflow workflow = instance.getWorkflow();
        List<ExecutionStage> stages = workflow.getStages();
        if (stages == null || stages.isEmpty()) {
            log.info("No stages present in workflow {}. Completing immediately.", instance.getWorkflowId());
            stateMachine.transition(instance, WorkflowState.COMPLETED, "Workflow contains no stages");
            return true;
        }

        // Sort stages by orderIndex
        List<ExecutionStage> sortedStages = stages.stream()
                .sorted(Comparator.comparingInt(ExecutionStage::getOrderIndex))
                .collect(Collectors.toList());

        stateMachine.transition(instance, WorkflowState.RUNNING, "Starting stage execution");

        for (int i = 0; i < sortedStages.size(); i++) {
            instance.setCurrentStageIndex(i);
            ExecutionStage stage = sortedStages.get(i);

            // Check if workflow paused or cancelled mid-execution
            if (instance.getState().isPausedOrWaiting() || instance.getState() == WorkflowState.CANCELLED) {
                log.info("Execution stopped for instance {} because state is {}", instance.getInstanceId(), instance.getState());
                return false;
            }

            boolean stageSuccess = pipeline.processStage(instance, stage);

            if (!stageSuccess) {
                log.warn("Stage {} failed during execution of workflow instance {}", stage.getStageId(), instance.getInstanceId());
                stateMachine.transition(instance, WorkflowState.FAILED, "Stage " + stage.getStageId() + " failed");

                RecoveryDecision recovery = recoveryCoordinator.evaluateAndRecover(instance, "stage_failure_" + stage.getStageId(), "Stage execution failed");
                log.info("Recovery decision for workflow {}: {}", instance.getWorkflowId(), recovery.getAction());
                return false;
            }
        }

        stateMachine.transition(instance, WorkflowState.COMPLETED, "All stages completed successfully");
        log.info("Successfully completed workflow instance {}", instance.getInstanceId());
        return true;
    }
}
