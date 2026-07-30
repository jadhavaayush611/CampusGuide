package com.campusguide.personal.ai.atlas.orchestration.registry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Registry service for dynamic agent registration, capability discovery, health tracking, and version management.
 */
@Slf4j
@Service
public class AgentRegistry {

    private final Map<String, AgentDescriptor> descriptors = new ConcurrentHashMap<>();

    public void registerAgent(AgentDescriptor descriptor) {
        if (descriptor == null || descriptor.getAgentId() == null) {
            throw new IllegalArgumentException("Descriptor and agentId must not be null");
        }
        descriptors.put(descriptor.getAgentId(), descriptor);
        log.info("AgentRegistry registered agent {} (Type: {}, Version: {})",
                descriptor.getAgentId(), descriptor.getAgentType(), descriptor.getVersion());
    }

    public void unregisterAgent(String agentId) {
        if (agentId != null) {
            descriptors.remove(agentId);
            log.info("AgentRegistry unregistered agent {}", agentId);
        }
    }

    public Optional<AgentDescriptor> getDescriptor(String agentId) {
        return Optional.ofNullable(descriptors.get(agentId));
    }

    public List<AgentDescriptor> getAllDescriptors() {
        return new ArrayList<>(descriptors.values());
    }

    public List<AgentDescriptor> findAgentsByCapability(String capabilityName) {
        if (capabilityName == null) return List.of();
        return descriptors.values().stream()
                .filter(AgentDescriptor::isAvailable)
                .filter(d -> d.supportsCapability(capabilityName))
                .collect(Collectors.toList());
    }

    public List<AgentDescriptor> findAgentsByDomain(String domain) {
        if (domain == null) return List.of();
        return descriptors.values().stream()
                .filter(AgentDescriptor::isAvailable)
                .filter(d -> d.getMetadata() != null && domain.equalsIgnoreCase(d.getMetadata().getDomain()))
                .collect(Collectors.toList());
    }

    public void updateHealthStatus(String agentId, AgentDescriptor.HealthStatus status) {
        AgentDescriptor descriptor = descriptors.get(agentId);
        if (descriptor != null) {
            descriptor.setHealthStatus(status);
            log.info("Updated health status for agent {} to {}", agentId, status);
        }
    }

    public void updateLoad(String agentId, int currentLoad) {
        AgentDescriptor descriptor = descriptors.get(agentId);
        if (descriptor != null) {
            descriptor.setCurrentLoad(currentLoad);
        }
    }
}
