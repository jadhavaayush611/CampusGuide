package com.campusguide.personal.ai.atlas.execution.rollback;

import com.campusguide.personal.ai.atlas.execution.model.ExecutionCheckpoint;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic rollback plan and fallback workflow.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RollbackPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    private String planId;
    private String workflowId;

    @Builder.Default
    private List<ExecutionUnit> rollbackUnits = new ArrayList<>();

    @Builder.Default
    private RecoveryStrategy recoveryStrategy = RecoveryStrategy.AUTOMATIC_COMPENSATING_ACTION;

    @Builder.Default
    private boolean deterministic = true;

    @Builder.Default
    private long estimatedRollbackTimeSeconds = 30L;

    @Builder.Default
    private List<ExecutionCheckpoint> checkpointRestorations = new ArrayList<>();

    public static RollbackPlan empty(String workflowId) {
        return RollbackPlan.builder()
                .planId("rb_empty")
                .workflowId(workflowId)
                .rollbackUnits(new ArrayList<>())
                .recoveryStrategy(RecoveryStrategy.IGNORE_AND_CONTINUE)
                .deterministic(true)
                .estimatedRollbackTimeSeconds(0L)
                .build();
    }
}
