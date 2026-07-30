package com.campusguide.personal.ai.atlas.orchestration.registry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Metadata descriptor for specialized agent configuration and classification.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentMetadata {

    private String specialization;
    private String domain;
    @Builder.Default
    private String author = "CampusGuide System";
    @Builder.Default
    private String version = "1.0.0";
    @Builder.Default
    private Map<String, String> tags = new HashMap<>();
    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();

    public static AgentMetadata defaultFor(String specialization, String domain) {
        return AgentMetadata.builder()
                .specialization(specialization)
                .domain(domain)
                .build();
    }
}
