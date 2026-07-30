package com.campusguide.personal.ai.atlas.execution.tool;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionStage;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ToolResolver resolves required capabilities, validates availability against CapabilityRegistry,
 * detects missing tools, and recommends alternative capabilities.
 */
@Slf4j
@Component
public class ToolResolver {

    private final CapabilityRegistry registry;

    public ToolResolver(CapabilityRegistry registry) {
        this.registry = registry;
    }

    public ToolResolutionResult resolve(ExecutionContext context, List<ExecutionStage> stages) {
        log.debug("Resolving tool capabilities for contextId={}", context != null ? context.getContextId() : "unknown");

        List<ToolCapability> resolvedCapabilities = new ArrayList<>();
        List<String> missingCapabilities = new ArrayList<>();
        Map<String, List<ToolCapability>> alternativeRecommendations = new HashMap<>();

        if (stages != null) {
            for (ExecutionStage stage : stages) {
                if (stage.getExecutionUnits() != null) {
                    for (ExecutionUnit unit : stage.getExecutionUnits()) {
                        String capId = unit.getTargetCapability();
                        if (capId != null && !capId.isBlank()) {
                            if (registry.hasCapability(capId)) {
                                registry.getCapability(capId).ifPresent(cap -> {
                                    if (cap.isAvailable()) {
                                        resolvedCapabilities.add(cap);
                                    } else {
                                        missingCapabilities.add(capId);
                                        alternativeRecommendations.put(capId, findAlternatives(cap));
                                    }
                                });
                            } else {
                                missingCapabilities.add(capId);
                                alternativeRecommendations.put(capId, findAlternativesByDomain(unit.getUnitType().name()));
                            }
                        }
                    }
                }
            }
        }

        return ToolResolutionResult.builder()
                .resolvedCapabilities(resolvedCapabilities)
                .missingCapabilities(missingCapabilities)
                .alternativeRecommendations(alternativeRecommendations)
                .allCapabilitiesResolved(missingCapabilities.isEmpty())
                .build();
    }

    public List<ToolCapability> findAlternatives(ToolCapability unavailableCapability) {
        if (unavailableCapability == null) return new ArrayList<>();
        return registry.getCapabilitiesByDomain(unavailableCapability.getDomain());
    }

    public List<ToolCapability> findAlternativesByDomain(String domain) {
        return registry.getCapabilitiesByDomain(domain);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ToolResolutionResult implements Serializable {
        private static final long serialVersionUID = 1L;

        @Builder.Default
        private List<ToolCapability> resolvedCapabilities = new ArrayList<>();

        @Builder.Default
        private List<String> missingCapabilities = new ArrayList<>();

        @Builder.Default
        private Map<String, List<ToolCapability>> alternativeRecommendations = new HashMap<>();

        @Builder.Default
        private boolean allCapabilitiesResolved = true;
    }
}
