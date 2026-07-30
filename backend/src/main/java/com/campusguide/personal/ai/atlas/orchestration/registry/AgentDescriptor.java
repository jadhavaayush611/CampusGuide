package com.campusguide.personal.ai.atlas.orchestration.registry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Descriptor containing complete specification of an Atlas agent.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentDescriptor {

    private String agentId;
    private String name;
    private String agentType;
    @Builder.Default
    private String version = "1.0.0";
    @Builder.Default
    private HealthStatus healthStatus = HealthStatus.HEALTHY;
    @Builder.Default
    private int maxCapacity = 10;
    @Builder.Default
    private int currentLoad = 0;
    @Builder.Default
    private List<AgentCapability> capabilities = new ArrayList<>();
    @Builder.Default
    private AgentMetadata metadata = new AgentMetadata();

    public enum HealthStatus {
        HEALTHY,
        DEGRADED,
        UNHEALTHY
    }

    public boolean supportsCapability(String capabilityName) {
        if (capabilities == null || capabilityName == null) {
            return false;
        }
        return capabilities.stream()
                .anyMatch(c -> capabilityName.equalsIgnoreCase(c.getName()) || capabilityName.equalsIgnoreCase(c.getCapabilityId()));
    }

    public boolean isAvailable() {
        return healthStatus != HealthStatus.UNHEALTHY && currentLoad < maxCapacity;
    }
}
