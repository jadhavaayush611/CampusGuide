package com.campusguide.personal.ai.atlas.provider;

import com.campusguide.personal.ai.atlas.model.AtlasNormalizedResponse;
import com.campusguide.personal.ai.atlas.model.AtlasPrompt;
import com.campusguide.personal.ai.atlas.model.ProviderMetadata;

public interface AIProvider {

    /**
     * Sends the assembled prompt to the AI provider and returns a normalized response.
     *
     * @param prompt the assembled prompt
     * @return provider-agnostic normalized response
     */
    AtlasNormalizedResponse sendPrompt(AtlasPrompt prompt);

    /**
     * Exposes provider metadata (name, version, supported models, active state).
     *
     * @return provider metadata
     */
    ProviderMetadata getMetadata();

    /**
     * Checks if the provider service is reachable and available.
     *
     * @return true if available, false otherwise
     */
    boolean isAvailable();

    /**
     * Normalizes raw provider response object into AtlasNormalizedResponse.
     *
     * @param rawProviderResponse the raw response body or DTO from the underlying provider
     * @return normalized response
     */
    AtlasNormalizedResponse normalizeResponse(Object rawProviderResponse);
}
