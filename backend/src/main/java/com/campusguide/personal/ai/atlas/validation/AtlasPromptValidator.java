package com.campusguide.personal.ai.atlas.validation;

import com.campusguide.personal.ai.atlas.config.AtlasProperties;
import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import com.campusguide.personal.ai.atlas.exception.AtlasPromptValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AtlasPromptValidator {

    private final AtlasProperties atlasProperties;

    public void validateRequest(AtlasChatRequest request) {
        if (request == null) {
            throw new AtlasPromptValidationException("Chat request cannot be null");
        }

        if (request.getPrompt() == null || request.getPrompt().isBlank()) {
            throw new AtlasPromptValidationException("Prompt message cannot be empty or blank");
        }

        int maxLength = atlasProperties.getMaxPromptLength();
        if (request.getPrompt().length() > maxLength) {
            throw new AtlasPromptValidationException(
                    String.format("Prompt length (%d characters) exceeds maximum allowed limit of %d characters",
                            request.getPrompt().length(), maxLength));
        }

        if (request.getTemperature() != null && (request.getTemperature() < 0.0 || request.getTemperature() > 2.0)) {
            throw new AtlasPromptValidationException("Temperature must be between 0.0 and 2.0");
        }

        if (request.getMaxTokens() != null && request.getMaxTokens() <= 0) {
            throw new AtlasPromptValidationException("Max tokens must be a positive integer");
        }
    }
}
