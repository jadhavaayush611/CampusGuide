package com.campusguide.personal.ai.atlas.context;

import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;

/**
 * Interface for pluggable context contributors in Atlas AI.
 * Contributors enrich the AtlasContext with domain-specific knowledge (user profile, academic status, etc.).
 */
public interface ContextContributor {

    /**
     * Unique identifier or domain name of the contributor.
     *
     * @return contributor name (e.g. "userProfile", "planner", "calendar", "academic", "campus")
     */
    String getName();

    /**
     * Enriches the provided AtlasContext based on the current request and context domain.
     *
     * @param request current chat request
     * @param context atlas context model to enrich
     */
    void contribute(AtlasChatRequest request, AtlasContext context);
}
