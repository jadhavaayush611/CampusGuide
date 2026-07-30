package com.campusguide.personal.ai.atlas.orchestration.supervisor;

import com.campusguide.personal.ai.atlas.orchestration.agent.AgentRuntime;
import com.campusguide.personal.ai.atlas.orchestration.agent.AtlasAgent;
import com.campusguide.personal.ai.atlas.orchestration.registry.AgentDescriptor;
import com.campusguide.personal.ai.atlas.orchestration.registry.AgentRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Multi-agent orchestration recovery coordinator managing agent failovers, task reassignment, and escalation.
 */
@Slf4j
@Service("orchestrationSupervisorRecoveryCoordinator")
@RequiredArgsConstructor
public class RecoveryCoordinator {

    private final AgentRegistry agentRegistry;
    private final AgentRuntime agentRuntime;

    public boolean recoverStalledAgent(String stalledAgentId, String reason) {
        log.warn("RecoveryCoordinator attempting recovery for stalled agent {} (Reason: {})", stalledAgentId, reason);

        Optional<AtlasAgent> agentOpt = agentRuntime.getAgent(stalledAgentId);
        if (agentOpt.isEmpty()) {
            log.error("Cannot recover agent {}: Not found in AgentRuntime", stalledAgentId);
            return false;
        }

        AtlasAgent agent = agentOpt.get();
        synchronized (agent) {
            agent.getState().decrementLoad();
            agentRegistry.updateHealthStatus(stalledAgentId, AgentDescriptor.HealthStatus.DEGRADED);
            agent.resume();
        }

        log.info("Successfully recovered stalled agent {} to DEGRADED healthy state", stalledAgentId);
        return true;
    }

    public boolean escalateUnhealthyAgent(String agentId, String reason) {
        log.error("Escalating unhealthy agent {} to TERMINATED (Reason: {})", agentId, reason);
        agentRegistry.updateHealthStatus(agentId, AgentDescriptor.HealthStatus.UNHEALTHY);
        agentRuntime.terminateAgent(agentId);
        return true;
    }
}
