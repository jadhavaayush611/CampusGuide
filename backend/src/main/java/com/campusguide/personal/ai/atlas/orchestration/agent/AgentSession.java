package com.campusguide.personal.ai.atlas.orchestration.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Encapsulates an active session context for an agent execution task.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentSession {

    private String sessionId;
    private String agentId;
    private String taskId;
    private String workflowId;
    @Builder.Default
    private Instant startTime = Instant.now();
    private Instant endTime;
    @Builder.Default
    private AgentLifecycle status = AgentLifecycle.RUNNING;
    @Builder.Default
    private Map<String, Object> sessionData = new HashMap<>();

    public static AgentSession start(String agentId, String taskId, String workflowId) {
        return AgentSession.builder()
                .sessionId("session_" + UUID.randomUUID().toString().substring(0, 8))
                .agentId(agentId)
                .taskId(taskId)
                .workflowId(workflowId)
                .startTime(Instant.now())
                .status(AgentLifecycle.RUNNING)
                .build();
    }

    public void complete() {
        this.status = AgentLifecycle.READY;
        this.endTime = Instant.now();
    }

    public void fail(String reason) {
        this.status = AgentLifecycle.FAILED;
        this.endTime = Instant.now();
        if (sessionData == null) {
            sessionData = new HashMap<>();
        }
        sessionData.put("error", reason);
    }
}
