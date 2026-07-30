package com.campusguide.personal.ai.atlas.orchestration.supervisor;

import com.campusguide.personal.ai.atlas.orchestration.registry.AgentDescriptor;
import com.campusguide.personal.ai.atlas.orchestration.registry.AgentRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service enforcing multi-agent execution policy, resource quotas, and governance limits.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PolicySupervisor {

    private final AgentRegistry agentRegistry;

    public boolean validatePolicy(String agentId, int requestedLoad) {
        Optional<AgentDescriptor> descriptorOpt = agentRegistry.getDescriptor(agentId);
        if (descriptorOpt.isEmpty()) {
            log.warn("PolicySupervisor: Agent {} not found in registry", agentId);
            return false;
        }

        AgentDescriptor descriptor = descriptorOpt.get();
        if (!descriptor.isAvailable()) {
            log.warn("PolicySupervisor: Agent {} is not available (Health: {})", agentId, descriptor.getHealthStatus());
            return false;
        }

        if (descriptor.getCurrentLoad() + requestedLoad > descriptor.getMaxCapacity()) {
            log.warn("PolicySupervisor: Agent {} capacity exceeded. Requested {}, Current {}, Max {}",
                    agentId, requestedLoad, descriptor.getCurrentLoad(), descriptor.getMaxCapacity());
            return false;
        }

        return true;
    }
}
