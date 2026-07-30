package com.campusguide.personal.ai.atlas.execution.workflow;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionCheckpoint;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionMetadata;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionStage;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnit;
import com.campusguide.personal.ai.atlas.execution.model.WorkflowStatus;
import com.campusguide.personal.ai.atlas.planning.model.ExecutionPlan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Produces execution-ready workflow contracts from ExecutionPlan and ExecutionContext.
 */
@Slf4j
@Component
public class ExecutableWorkflowBuilder {

    private final WorkflowAssembler assembler;
    private final WorkflowRegistry registry;

    public ExecutableWorkflowBuilder(WorkflowAssembler assembler, WorkflowRegistry registry) {
        this.assembler = assembler;
        this.registry = registry;
    }

    public ExecutableWorkflow buildWorkflow(ExecutionContext context) {
        if (context == null) {
            log.warn("Null ExecutionContext provided to ExecutableWorkflowBuilder");
            return ExecutableWorkflow.fallback("wf_" + UUID.randomUUID().toString().substring(0, 8), "Null ExecutionContext");
        }

        ExecutionPlan plan = context.getExecutionPlan();
        String planId = plan != null ? plan.getPlanId() : "plan_unknown";
        String workflowId = "wf_" + planId + "_" + UUID.randomUUID().toString().substring(0, 6);

        log.debug("Building ExecutableWorkflow for workflowId={}, planId={}, contextId={}",
                workflowId, planId, context.getContextId());

        List<ExecutionStage> stages = assembler.assembleStages(context);
        List<ExecutionCheckpoint> checkpoints = assembler.assembleCheckpoints(stages);

        List<String> requiredCapabilities = stages.stream()
                .flatMap(s -> s.getExecutionUnits().stream())
                .map(ExecutionUnit::getTargetCapability)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .collect(Collectors.toList());

        ExecutionContract contract = ExecutionContract.builder()
                .contractId("ctr_" + workflowId)
                .workflowId(workflowId)
                .requiredCapabilities(requiredCapabilities)
                .expectedDurationSeconds(calculateTotalDuration(stages))
                .maxMemoryMb(512L)
                .slaLevel("STANDARD")
                .build();

        ExecutionMetadata metadata = ExecutionMetadata.createDefault(workflowId, planId, context.getContextId());

        return ExecutableWorkflow.builder()
                .workflowId(workflowId)
                .planId(planId)
                .contextId(context.getContextId())
                .stages(stages)
                .checkpoints(checkpoints)
                .contract(contract)
                .metadata(metadata)
                .status(WorkflowStatus.PREPARED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private long calculateTotalDuration(List<ExecutionStage> stages) {
        if (stages == null) return 60L;
        long total = 0L;
        for (ExecutionStage stage : stages) {
            long stageDuration = stage.getExecutionUnits().stream()
                    .mapToLong(ExecutionUnit::getTimeoutSeconds)
                    .max()
                    .orElse(60L);
            total += stageDuration;
        }
        return Math.max(total, 30L);
    }
}
