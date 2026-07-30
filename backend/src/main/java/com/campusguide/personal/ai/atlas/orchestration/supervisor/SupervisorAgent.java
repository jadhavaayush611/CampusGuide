package com.campusguide.personal.ai.atlas.orchestration.supervisor;

import com.campusguide.personal.ai.atlas.orchestration.agent.AgentLifecycle;
import com.campusguide.personal.ai.atlas.orchestration.agent.AtlasAgent;
import com.campusguide.personal.ai.atlas.orchestration.registry.AgentCapability;
import com.campusguide.personal.ai.atlas.orchestration.registry.AgentDescriptor;
import com.campusguide.personal.ai.atlas.orchestration.registry.AgentMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Specialized supervisor agent in Atlas Multi-Agent Orchestration Layer overseeing multi-agent executions.
 */
@Slf4j
@Component
public class SupervisorAgent extends AtlasAgent {

    public SupervisorAgent() {
        super(AgentDescriptor.builder()
                .agentId("agent_supervisor_primary")
                .name("Atlas Supervisor Agent")
                .agentType("SUPERVISOR")
                .version("1.0.0")
                .healthStatus(AgentDescriptor.HealthStatus.HEALTHY)
                .maxCapacity(100)
                .currentLoad(0)
                .capabilities(List.of(AgentCapability.of("Supervision", "Governance")))
                .metadata(AgentMetadata.defaultFor("Multi-Agent Supervision", "Governance"))
                .build());
        getState().setLifecycle(AgentLifecycle.READY);
    }

    public void logSupervisionEvent(String targetAgentId, String eventType, String details) {
        log.info("SupervisorAgent [Target={}] Event={}: {}", targetAgentId, eventType, details);
    }
}
