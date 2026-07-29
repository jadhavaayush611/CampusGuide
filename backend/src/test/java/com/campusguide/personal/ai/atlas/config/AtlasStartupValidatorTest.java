package com.campusguide.personal.ai.atlas.config;

import com.campusguide.personal.ai.atlas.context.ContextContributor;
import com.campusguide.personal.ai.atlas.context.ContextEngine;
import com.campusguide.personal.ai.atlas.exception.AtlasConfigurationException;
import com.campusguide.personal.ai.atlas.prompt.PromptBuilder;
import com.campusguide.personal.ai.atlas.prompt.PromptTemplate;
import com.campusguide.personal.ai.atlas.prompt.persona.CampusGuideAssistantPersona;
import com.campusguide.personal.ai.atlas.provider.AIProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtlasStartupValidatorTest {

    @Mock
    private PromptBuilder promptBuilder;

    @Mock
    private PromptTemplate promptTemplate;

    @Mock
    private CampusGuideAssistantPersona persona;

    @Mock
    private ContextEngine contextEngine;

    @Mock
    private ContextContributor contributor;

    @Mock
    private AIProvider aiProvider;

    private AtlasProperties atlasProperties;

    @BeforeEach
    void setUp() {
        atlasProperties = new AtlasProperties();
    }

    @Test
    void testStartupValidationSuccess() {
        when(persona.renderPersonaBase()).thenReturn("Persona Base");
        when(contextEngine.getContributors()).thenReturn(List.of(contributor));

        AtlasStartupValidator validator = new AtlasStartupValidator(
                atlasProperties,
                promptBuilder,
                promptTemplate,
                persona,
                contextEngine,
                aiProvider
        );

        assertDoesNotThrow(validator::validateOnStartup);
    }

    @Test
    void testStartupValidationFailsWhenContributorsNull() {
        when(persona.renderPersonaBase()).thenReturn("Persona Base");
        when(contextEngine.getContributors()).thenReturn(null);

        AtlasStartupValidator validator = new AtlasStartupValidator(
                atlasProperties,
                promptBuilder,
                promptTemplate,
                persona,
                contextEngine,
                aiProvider
        );

        assertThrows(AtlasConfigurationException.class, validator::validateOnStartup);
    }
}
