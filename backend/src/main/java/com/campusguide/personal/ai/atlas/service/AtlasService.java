package com.campusguide.personal.ai.atlas.service;

import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import com.campusguide.personal.ai.atlas.dto.AtlasChatResponse;
import com.campusguide.personal.ai.atlas.dto.CapabilityResponse;
import com.campusguide.personal.ai.atlas.dto.ChatRequest;
import com.campusguide.personal.ai.atlas.dto.ChatResponse;
import org.springframework.security.core.userdetails.UserDetails;

public interface AtlasService {

    /**
     * Processes a provider-agnostic chat request and returns a normalized response.
     */
    AtlasChatResponse chat(AtlasChatRequest request);

    /**
     * Processes a chat request for an authenticated user.
     */
    AtlasChatResponse chat(AtlasChatRequest request, UserDetails userDetails);

    /**
     * Processes a ChatRequest DTO for an authenticated user.
     */
    ChatResponse chat(ChatRequest request, UserDetails userDetails);

    /**
     * Returns Atlas API platform capability discovery metadata.
     */
    CapabilityResponse getCapabilities();

    /**
     * Returns operational metadata and version info.
     */
    CapabilityResponse getOperationalInfo();
}
