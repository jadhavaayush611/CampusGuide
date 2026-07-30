package com.campusguide.personal.ai.atlas.execution.runtime.recovery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Outcome of failure recovery evaluation detailing chosen recovery policy and actions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecoveryDecision implements Serializable {

    private static final long serialVersionUID = 1L;

    private RecoveryPolicy action;
    private String targetCheckpointId;
    private String retryUnitId;
    private String reason;
    private boolean requiresHumanIntervention;

    public static RecoveryDecision rollback(String reason) {
        return RecoveryDecision.builder()
                .action(RecoveryPolicy.ROLLBACK_WORKFLOW)
                .reason(reason)
                .build();
    }

    public static RecoveryDecision restoreCheckpoint(String checkpointId, String reason) {
        return RecoveryDecision.builder()
                .action(RecoveryPolicy.RESTORE_CHECKPOINT)
                .targetCheckpointId(checkpointId)
                .reason(reason)
                .build();
    }

    public static RecoveryDecision waitForHuman(String reason) {
        return RecoveryDecision.builder()
                .action(RecoveryPolicy.WAIT_FOR_HUMAN)
                .reason(reason)
                .requiresHumanIntervention(true)
                .build();
    }
}
