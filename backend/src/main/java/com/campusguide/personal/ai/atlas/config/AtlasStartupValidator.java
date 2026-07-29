package com.campusguide.personal.ai.atlas.config;

import com.campusguide.personal.ai.atlas.context.ContextEngine;
import com.campusguide.personal.ai.atlas.exception.AtlasConfigurationException;
import com.campusguide.personal.ai.atlas.prompt.PromptBuilder;
import com.campusguide.personal.ai.atlas.prompt.PromptTemplate;
import com.campusguide.personal.ai.atlas.prompt.persona.CampusGuideAssistantPersona;
import com.campusguide.personal.ai.atlas.provider.AIProvider;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AtlasStartupValidator {

    private final AtlasProperties atlasProperties;
    private final PromptBuilder promptBuilder;
    private final PromptTemplate promptTemplate;
    private final CampusGuideAssistantPersona persona;
    private final ContextEngine contextEngine;
    private final AIProvider aiProvider;

    public AtlasStartupValidator(
            AtlasProperties atlasProperties,
            @Qualifier("atlasPromptBuilder") PromptBuilder promptBuilder,
            PromptTemplate promptTemplate,
            CampusGuideAssistantPersona persona,
            ContextEngine contextEngine,
            AIProvider aiProvider
    ) {
        this.atlasProperties = atlasProperties;
        this.promptBuilder = promptBuilder;
        this.promptTemplate = promptTemplate;
        this.persona = persona;
        this.contextEngine = contextEngine;
        this.aiProvider = aiProvider;
    }

    @PostConstruct
    public void validateOnStartup() {
        if (!atlasProperties.isEnabled()) {
            log.info("Atlas AI subsystem is disabled; skipping startup validation.");
            return;
        }

        log.info("Performing Atlas startup readiness validation...");

        // 1. Validate Provider Configuration
        if (aiProvider == null) {
            throw new AtlasConfigurationException("Startup failed: AIProvider bean is missing");
        }
        if (atlasProperties.getProviders() == null || atlasProperties.getProviders().getOpenai() == null) {
            throw new AtlasConfigurationException("Startup failed: OpenAI provider configuration is missing");
        }

        // 2. Validate PromptBuilder & Personas
        if (promptBuilder == null) {
            throw new AtlasConfigurationException("Startup failed: PromptBuilder bean is missing");
        }
        if (persona == null || persona.renderPersonaBase() == null || persona.renderPersonaBase().isBlank()) {
            throw new AtlasConfigurationException("Startup failed: CampusGuideAssistantPersona is not correctly configured");
        }

        // 3. Validate Prompt Templates
        if (promptTemplate == null) {
            throw new AtlasConfigurationException("Startup failed: PromptTemplate bean is missing");
        }

        // 4. Validate Context Engine & Contributors
        if (contextEngine == null) {
            throw new AtlasConfigurationException("Startup failed: ContextEngine bean is missing");
        }
        if (contextEngine.getContributors() == null) {
            throw new AtlasConfigurationException("Startup failed: ContextEngine contributors list is null");
        }
        if (contextEngine.getContributors().isEmpty()) {
            log.warn("Atlas startup check: No ContextContributor beans currently registered in ContextEngine");
        }

        log.info("Atlas AI subsystem startup validation completed successfully. All components operational.");
    }
}
