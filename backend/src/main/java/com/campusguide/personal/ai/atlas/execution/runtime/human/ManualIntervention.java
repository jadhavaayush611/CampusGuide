package com.campusguide.personal.ai.atlas.execution.runtime.human;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Historical record of a manual human intervention in workflow runtime execution.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManualIntervention implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String interventionId = "mitv_" + UUID.randomUUID().toString().substring(0, 8);

    private String workflowId;
    private String instanceId;
    private String unitId;
    private String interventionType; // APPROVAL, OVERRIDE, PAUSE, RESUME, CANCEL, RETRY
    private String operatorId;

    @Builder.Default
    private Instant timestamp = Instant.now();

    private String outcome;
    private String notes;
}
