package com.campusguide.personal.ai.atlas.execution.runtime.events;

import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Event published when a workflow lifecycle or state transition occurs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String eventId = "evt_wf_" + UUID.randomUUID().toString().substring(0, 8);

    private String workflowId;
    private String instanceId;
    private String eventType;
    private WorkflowState previousState;
    private WorkflowState newState;

    @Builder.Default
    private Instant timestamp = Instant.now();

    private String message;
}
