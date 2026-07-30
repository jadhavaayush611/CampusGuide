package com.campusguide.personal.ai.atlas.orchestration.registry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Capability descriptor for an Atlas specialized agent.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentCapability {

    private String capabilityId;
    private String name;
    private String domain;
    @Builder.Default
    private String version = "1.0.0";
    @Builder.Default
    private double costWeight = 1.0;
    @Builder.Default
    private int maxConcurrency = 5;

    @Builder.Default
    private Map<String, Object> parameters = new HashMap<>();

    public static AgentCapability of(String name, String domain) {
        return AgentCapability.builder()
                .capabilityId("cap_" + name.toLowerCase().replaceAll("[^a-z0-9]", "_"))
                .name(name)
                .domain(domain)
                .build();
    }
}
