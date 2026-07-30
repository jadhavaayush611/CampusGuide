package com.campusguide.personal.ai.atlas.execution.runtime.checkpoint;

import com.campusguide.personal.ai.atlas.execution.runtime.tool.ToolResult;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Immutable snapshot of runtime state at a checkpoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StateSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String snapshotId = "snap_" + UUID.randomUUID().toString().substring(0, 8);

    private String workflowId;
    private int currentStageIndex;

    @Builder.Default
    private Map<String, Object> sessionVariables = new HashMap<>();

    @Builder.Default
    private Map<String, ToolResult> unitResults = new HashMap<>();

    @Builder.Default
    private Set<String> completedUnitIds = new HashSet<>();

    @Builder.Default
    private Set<String> completedStageIds = new HashSet<>();

    private WorkflowState state;

    @Builder.Default
    private Instant snapshotTime = Instant.now();
}
