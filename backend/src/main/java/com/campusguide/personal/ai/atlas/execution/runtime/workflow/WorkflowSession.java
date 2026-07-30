package com.campusguide.personal.ai.atlas.execution.runtime.workflow;

import com.campusguide.personal.ai.atlas.execution.runtime.tool.ToolResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages runtime session state, variables, unit outputs, and isolation for an active workflow execution.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowSession implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String sessionId = "session_" + UUID.randomUUID().toString().substring(0, 8);

    private String workflowId;
    private String contextId;
    private String userId;

    @Builder.Default
    private Map<String, Object> variables = new ConcurrentHashMap<>();

    @Builder.Default
    private Map<String, ToolResult> unitResults = new ConcurrentHashMap<>();

    @Builder.Default
    private Set<String> completedUnitIds = Collections.synchronizedSet(new HashSet<>());

    @Builder.Default
    private Set<String> completedStageIds = Collections.synchronizedSet(new HashSet<>());

    @Builder.Default
    private Instant executionStartTime = Instant.now();

    private Instant executionEndTime;

    @Builder.Default
    private Map<String, Object> attributes = new ConcurrentHashMap<>();

    public void setVariable(String name, Object value) {
        if (name != null && value != null) {
            variables.put(name, value);
        }
    }

    public Object getVariable(String name) {
        return variables.get(name);
    }

    public void recordUnitResult(String unitId, ToolResult result) {
        if (unitId != null && result != null) {
            unitResults.put(unitId, result);
            if (result.getStatus() != null && result.getStatus().isSuccess()) {
                completedUnitIds.add(unitId);
            }
        }
    }

    public ToolResult getUnitResult(String unitId) {
        return unitResults.get(unitId);
    }

    public void markStageCompleted(String stageId) {
        if (stageId != null) {
            completedStageIds.add(stageId);
        }
    }

    public boolean isUnitCompleted(String unitId) {
        return completedUnitIds.contains(unitId);
    }

    public boolean isStageCompleted(String stageId) {
        return completedStageIds.contains(stageId);
    }
}
