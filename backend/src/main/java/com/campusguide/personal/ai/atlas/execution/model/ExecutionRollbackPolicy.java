package com.campusguide.personal.ai.atlas.execution.model;

import com.campusguide.personal.ai.atlas.execution.rollback.RecoveryStrategy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Rollback policy configuration for an execution unit.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionRollbackPolicy implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private boolean rollbackOnFailure = true;

    private String compensatingUnitId;

    @Builder.Default
    private RecoveryStrategy recoveryStrategy = RecoveryStrategy.AUTOMATIC_COMPENSATING_ACTION;

    public static ExecutionRollbackPolicy defaultConfig() {
        return ExecutionRollbackPolicy.builder()
                .rollbackOnFailure(true)
                .recoveryStrategy(RecoveryStrategy.AUTOMATIC_COMPENSATING_ACTION)
                .build();
    }
}
