package com.campusguide.personal.ai.atlas.execution.runtime.workflow;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.runtime.checkpoint.RuntimeCheckpoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Tracks an active or completed workflow instance in the runtime engine.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String instanceId = "wfinst_" + UUID.randomUUID().toString().substring(0, 8);

    private String workflowId;
    private String contextId;

    @Builder.Default
    private WorkflowState state = WorkflowState.CREATED;

    private ExecutableWorkflow workflow;
    private ExecutionContext context;

    @Builder.Default
    private WorkflowSession session = new WorkflowSession();

    @Builder.Default
    private int currentStageIndex = 0;

    @Builder.Default
    private Instant startTime = Instant.now();

    private Instant endTime;

    @Builder.Default
    private List<RuntimeCheckpoint> checkpoints = Collections.synchronizedList(new ArrayList<>());

    @Builder.Default
    private List<String> executionLog = Collections.synchronizedList(new ArrayList<>());

    private String failureReason;

    public void addLog(String entry) {
        if (entry != null) {
            executionLog.add("[" + Instant.now() + "] " + entry);
        }
    }

    public void addCheckpoint(RuntimeCheckpoint checkpoint) {
        if (checkpoint != null) {
            checkpoints.add(checkpoint);
        }
    }

    public static WorkflowInstance create(ExecutionContext context, ExecutableWorkflow workflow) {
        String wfId = workflow != null ? workflow.getWorkflowId() : "wf_unknown";
        String ctxId = context != null ? context.getContextId() : "ctx_unknown";
        String userId = context != null ? context.getUserId() : "system";

        WorkflowSession sess = WorkflowSession.builder()
                .workflowId(wfId)
                .contextId(ctxId)
                .userId(userId)
                .build();

        return WorkflowInstance.builder()
                .instanceId("wfinst_" + UUID.randomUUID().toString().substring(0, 8))
                .workflowId(wfId)
                .contextId(ctxId)
                .state(WorkflowState.CREATED)
                .workflow(workflow)
                .context(context)
                .session(sess)
                .currentStageIndex(0)
                .startTime(Instant.now())
                .build();
    }
}
