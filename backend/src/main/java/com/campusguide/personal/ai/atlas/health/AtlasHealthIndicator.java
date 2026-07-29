package com.campusguide.personal.ai.atlas.health;

import com.campusguide.personal.ai.atlas.config.AtlasProperties;
import com.campusguide.personal.ai.atlas.context.ContextEngine;
import com.campusguide.personal.ai.atlas.prompt.PromptBuilder;
import com.campusguide.personal.ai.atlas.prompt.persona.CampusGuideAssistantPersona;
import com.campusguide.personal.ai.atlas.provider.AIProvider;
import com.campusguide.personal.ai.atlas.resilience.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AtlasHealthIndicator implements HealthIndicator {

    private final AtlasProperties atlasProperties;
    private final AIProvider aiProvider;
    private final PromptBuilder promptBuilder;
    private final ContextEngine contextEngine;
    private final CircuitBreaker circuitBreaker;

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();

        if (!atlasProperties.isEnabled()) {
            details.put("status", "DISABLED");
            details.put("reason", "Atlas AI subsystem is disabled via configuration");
            return Health.outOfService().withDetails(details).build();
        }

        boolean providerConfigValid = validateProviderConfig(details);
        boolean promptPipelineValid = validatePromptPipeline(details);
        boolean contextPipelineValid = validateContextPipeline(details);
        boolean circuitBreakerHealthy = validateCircuitBreaker(details);

        boolean ready = providerConfigValid && promptPipelineValid && contextPipelineValid && circuitBreakerHealthy;

        details.put("subsystemReadiness", ready ? "READY" : "NOT_READY");
        details.put("circuitBreakerState", circuitBreaker != null ? circuitBreaker.getState().name() : "N/A");

        if (ready) {
            return Health.up().withDetails(details).build();
        } else {
            return Health.down().withDetails(details).build();
        }
    }

    private boolean validateProviderConfig(Map<String, Object> details) {
        boolean valid = aiProvider != null && aiProvider.isAvailable();
        Map<String, Object> providerDetails = new HashMap<>();
        providerDetails.put("active", valid);
        if (aiProvider != null && aiProvider.getMetadata() != null) {
            providerDetails.put("providerName", aiProvider.getMetadata().getName());
            providerDetails.put("supportedModels", aiProvider.getMetadata().getSupportedModels());
        }
        details.put("providerConfig", providerDetails);
        return valid;
    }

    private boolean validatePromptPipeline(Map<String, Object> details) {
        boolean valid = promptBuilder != null;
        Map<String, Object> promptDetails = new HashMap<>();
        promptDetails.put("promptBuilderAvailable", promptBuilder != null);
        promptDetails.put("persona", CampusGuideAssistantPersona.NAME);
        details.put("promptPipeline", promptDetails);
        return valid;
    }

    private boolean validateContextPipeline(Map<String, Object> details) {
        boolean valid = contextEngine != null;
        Map<String, Object> contextDetails = new HashMap<>();
        contextDetails.put("contextEngineAvailable", contextEngine != null);
        if (contextEngine != null && contextEngine.getContributors() != null) {
            contextDetails.put("registeredContributorsCount", contextEngine.getContributors().size());
        }
        details.put("contextPipeline", contextDetails);
        return valid;
    }

    private boolean validateCircuitBreaker(Map<String, Object> details) {
        if (circuitBreaker == null) {
            return true;
        }
        return circuitBreaker.getState() != CircuitBreaker.State.OPEN;
    }
}
