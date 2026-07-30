package com.campusguide.personal.ai.atlas.execution.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Execution checkpoint for state validation, monitoring, or rollback safety.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionCheckpoint implements Serializable {

    private static final long serialVersionUID = 1L;

    private String checkpointId;
    private String stageId;
    private String unitId;
    private String checkpointName;

    @Builder.Default
    private CheckpointType type = CheckpointType.POST_STAGE;

    @Builder.Default
    private Map<String, String> validationCriteria = new HashMap<>();

    @Builder.Default
    private boolean rollbackTrigger = false;

    @Builder.Default
    private boolean requiredApproval = false;
}
