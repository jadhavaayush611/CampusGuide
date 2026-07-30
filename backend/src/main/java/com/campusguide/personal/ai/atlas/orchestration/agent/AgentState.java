package com.campusguide.personal.ai.atlas.orchestration.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * State snapshot for an Atlas specialized agent.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentState {

    private String agentId;
    @Builder.Default
    private AgentLifecycle lifecycle = AgentLifecycle.UNINITIALIZED;
    private String currentTaskId;
    private String currentWorkflowId;
    @Builder.Default
    private int activeLoad = 0;
    @Builder.Default
    private Instant lastActiveTimestamp = Instant.now();
    private String errorMessage;

    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();

    public static AgentState ready(String agentId) {
        return AgentState.builder()
                .agentId(agentId)
                .lifecycle(AgentLifecycle.READY)
                .activeLoad(0)
                .lastActiveTimestamp(Instant.now())
                .build();
    }

    public void updateActivity() {
        this.lastActiveTimestamp = Instant.now();
    }

    public void incrementLoad() {
        this.activeLoad++;
        updateActivity();
    }

    public void decrementLoad() {
        if (this.activeLoad > 0) {
            this.activeLoad--;
        }
        updateActivity();
    }
}
