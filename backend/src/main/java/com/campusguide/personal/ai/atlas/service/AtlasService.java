package com.campusguide.personal.ai.atlas.service;

import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import com.campusguide.personal.ai.atlas.dto.AtlasChatResponse;

import org.springframework.security.core.userdetails.UserDetails;

public interface AtlasService {

    /**
     * Processes a provider-agnostic chat request and returns a normalized response.
     *
     * @param request the chat request
     * @return normalized chat response
     */
    AtlasChatResponse chat(AtlasChatRequest request);

    /**
     * Processes a chat request for an authenticated user.
     *
     * @param request the chat request
     * @param userDetails authenticated user principal
     * @return normalized chat response
     */
    AtlasChatResponse chat(AtlasChatRequest request, UserDetails userDetails);
}
