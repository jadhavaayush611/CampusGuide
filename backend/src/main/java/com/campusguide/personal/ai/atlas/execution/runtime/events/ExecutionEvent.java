package com.campusguide.personal.ai.atlas.execution.runtime.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Detailed event published during execution stage, unit, tool, checkpoint, or rollback processing.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String eventId = "evt_exec_" + UUID.randomUUID().toString().substring(0, 8);

    private String workflowId;
    private String instanceId;
    private String stageId;
    private String unitId;
    private String eventType;

    @Builder.Default
    private Instant timestamp = Instant.now();

    @Builder.Default
    private Map<String, Object> details = new HashMap<>();
}
