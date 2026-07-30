package com.campusguide.personal.ai.atlas.execution.model;

import com.campusguide.personal.ai.atlas.execution.approval.ApprovalRequirement;
import com.campusguide.personal.ai.atlas.execution.explanation.ExecutionExplanation;
import com.campusguide.personal.ai.atlas.execution.resource.ResourceRequirement;
import com.campusguide.personal.ai.atlas.execution.risk.ExecutionRisk;
import com.campusguide.personal.ai.atlas.execution.rollback.RollbackPlan;
import com.campusguide.personal.ai.atlas.execution.workflow.ExecutionContract;
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
 * Main aggregate representing an execution-ready workflow while remaining
 * completely independent of execution runtimes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutableWorkflow implements Serializable {

    private static final long serialVersionUID = 1L;

    private String workflowId;
    private String planId;
    private String contextId;

    @Builder.Default
    private List<ExecutionStage> stages = new ArrayList<>();

    @Builder.Default
    private List<ExecutionCheckpoint> checkpoints = new ArrayList<>();

    private ExecutionContract contract;
    private ExecutionMetadata metadata;
    private ExecutionExplanation explanation;
    private ExecutionRisk riskAssessment;
    private RollbackPlan rollbackPlan;
    private ApprovalRequirement approvalRequirement;

    @Builder.Default
    private List<ResourceRequirement> resourceRequirements = new ArrayList<>();

    @Builder.Default
    private WorkflowStatus status = WorkflowStatus.PREPARED;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();

    public static ExecutableWorkflow fallback(String workflowId, String rationale) {
        ExecutionUnit fallbackUnit = ExecutionUnit.builder()
                .unitId("unit_fallback")
                .taskId("task_fallback")
                .title("Fallback Unit")
                .description(rationale)
                .unitType(ExecutionUnitType.ACTION)
                .mandatory(true)
                .status(UnitPreparationStatus.READY)
                .build();

        ExecutionStage fallbackStage = ExecutionStage.builder()
                .stageId("stage_fallback")
                .stageName("Fallback Stage")
                .orderIndex(1)
                .executionUnits(Collections.singletonList(fallbackUnit))
                .parallel(false)
                .completionPolicy(StageCompletionPolicy.ALL_MUST_SUCCEED)
                .build();

        return ExecutableWorkflow.builder()
                .workflowId(workflowId)
                .planId("plan_fallback")
                .contextId("context_fallback")
                .stages(Collections.singletonList(fallbackStage))
                .checkpoints(Collections.emptyList())
                .contract(ExecutionContract.defaultContract(workflowId))
                .metadata(ExecutionMetadata.createDefault(workflowId, "plan_fallback", "context_fallback"))
                .explanation(ExecutionExplanation.defaultExplanation("Fallback workflow generated: " + rationale))
                .riskAssessment(ExecutionRisk.lowRisk())
                .rollbackPlan(RollbackPlan.empty(workflowId))
                .approvalRequirement(ApprovalRequirement.none(workflowId))
                .resourceRequirements(Collections.emptyList())
                .status(WorkflowStatus.DEGRADED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
