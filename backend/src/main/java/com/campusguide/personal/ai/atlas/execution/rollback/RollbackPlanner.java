package com.campusguide.personal.ai.atlas.execution.rollback;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionCheckpoint;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionRetryPolicy;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionRollbackPolicy;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionStage;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnit;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnitType;
import com.campusguide.personal.ai.atlas.execution.model.UnitPreparationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * RollbackPlanner generates deterministic rollback plans and fallback workflows
 * for an ExecutableWorkflow.
 */
@Slf4j
@Component
public class RollbackPlanner {

    public RollbackPlan planRollback(ExecutionContext context, ExecutableWorkflow workflow) {
        log.debug("Generating rollback plan for workflowId={}", workflow != null ? workflow.getWorkflowId() : "unknown");

        String workflowId = workflow != null ? workflow.getWorkflowId() : "wf_unknown";
        List<ExecutionUnit> rollbackUnits = new ArrayList<>();
        List<ExecutionCheckpoint> restorations = new ArrayList<>();

        if (workflow != null && workflow.getStages() != null) {
            // Traverse stages in reverse order for rollback
            List<ExecutionStage> reverseStages = new ArrayList<>(workflow.getStages());
            Collections.reverse(reverseStages);

            for (ExecutionStage stage : reverseStages) {
                if (stage.getExecutionUnits() != null) {
                    List<ExecutionUnit> unitsInStage = new ArrayList<>(stage.getExecutionUnits());
                    Collections.reverse(unitsInStage);

                    for (ExecutionUnit unit : unitsInStage) {
                        if (unit.getUnitType() == ExecutionUnitType.MUTATION || unit.getUnitType() == ExecutionUnitType.ACTION) {
                            ExecutionUnit rollbackUnit = ExecutionUnit.builder()
                                    .unitId("rb_unit_" + unit.getUnitId())
                                    .taskId(unit.getTaskId())
                                    .title("Undo / Rollback: " + unit.getTitle())
                                    .description("Compensating action for unit " + unit.getUnitId())
                                    .unitType(ExecutionUnitType.MUTATION)
                                    .targetCapability(unit.getTargetCapability())
                                    .timeoutSeconds(unit.getTimeoutSeconds())
                                    .retryPolicy(ExecutionRetryPolicy.defaultConfig())
                                    .rollbackPolicy(ExecutionRollbackPolicy.builder()
                                            .rollbackOnFailure(false)
                                            .recoveryStrategy(RecoveryStrategy.BEST_EFFORT_CLEANUP)
                                            .build())
                                    .mandatory(false)
                                    .status(UnitPreparationStatus.READY)
                                    .build();

                            rollbackUnits.add(rollbackUnit);
                        }
                    }
                }
            }
        }

        if (workflow != null && workflow.getCheckpoints() != null) {
            restorations.addAll(workflow.getCheckpoints());
        }

        RecoveryStrategy overallStrategy = rollbackUnits.isEmpty() ?
                RecoveryStrategy.IGNORE_AND_CONTINUE : RecoveryStrategy.AUTOMATIC_COMPENSATING_ACTION;

        return RollbackPlan.builder()
                .planId("rb_plan_" + workflowId)
                .workflowId(workflowId)
                .rollbackUnits(rollbackUnits)
                .recoveryStrategy(overallStrategy)
                .deterministic(true)
                .estimatedRollbackTimeSeconds(rollbackUnits.size() * 15L)
                .checkpointRestorations(restorations)
                .build();
    }
}
