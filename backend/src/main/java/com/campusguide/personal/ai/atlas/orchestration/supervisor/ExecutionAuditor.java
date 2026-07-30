package com.campusguide.personal.ai.atlas.orchestration.supervisor;

import com.campusguide.personal.ai.atlas.orchestration.agent.AgentRuntime;
import com.campusguide.personal.ai.atlas.orchestration.agent.AtlasAgent;
import com.campusguide.personal.ai.atlas.orchestration.registry.AgentDescriptor;
import com.campusguide.personal.ai.atlas.orchestration.registry.AgentRegistry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for auditing agent execution states, detecting deadlocks, stalled workflows, and unhealthy agents.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionAuditor {

    private final AgentRegistry agentRegistry;
    private final AgentRuntime agentRuntime;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuditReport {
        private int totalAgentsAudited;
        @Builder.Default
        private List<String> stalledAgentIds = new ArrayList<>();
        @Builder.Default
        private List<String> unhealthyAgentIds = new ArrayList<>();
        @Builder.Default
        private List<String> deadlockRiskAgentIds = new ArrayList<>();
        @Builder.Default
        private Instant timestamp = Instant.now();
    }

    public AuditReport auditExecution(long stallThresholdMs) {
        List<AtlasAgent> agents = agentRuntime.getAllAgents();
        List<String> stalled = new ArrayList<>();
        List<String> unhealthy = new ArrayList<>();
        List<String> deadlockRisks = new ArrayList<>();

        Instant now = Instant.now();

        for (AtlasAgent agent : agents) {
            String agentId = agent.getAgentId();
            AgentDescriptor descriptor = agentRegistry.getDescriptor(agentId).orElse(null);

            if (descriptor != null && descriptor.getHealthStatus() == AgentDescriptor.HealthStatus.UNHEALTHY) {
                unhealthy.add(agentId);
            }

            if (agent.getState().getLifecycle().isAvailable() && agent.getState().getActiveLoad() > 0) {
                Instant lastActive = agent.getState().getLastActiveTimestamp();
                if (lastActive != null && Duration.between(lastActive, now).toMillis() > stallThresholdMs) {
                    log.warn("ExecutionAuditor detected stalled agent {} (Inactivity: {} ms)",
                            agentId, Duration.between(lastActive, now).toMillis());
                    stalled.add(agentId);
                }
            }

            if (agent.getState().getActiveLoad() >= (descriptor != null ? descriptor.getMaxCapacity() : 10)) {
                deadlockRisks.add(agentId);
            }
        }

        log.info("ExecutionAuditor audited {} agents. Found {} stalled, {} unhealthy, {} deadlock risks",
                agents.size(), stalled.size(), unhealthy.size(), deadlockRisks.size());

        return AuditReport.builder()
                .totalAgentsAudited(agents.size())
                .stalledAgentIds(stalled)
                .unhealthyAgentIds(unhealthy)
                .deadlockRiskAgentIds(deadlockRisks)
                .timestamp(now)
                .build();
    }
}
