package com.campusguide.personal.ai.atlas.service;

import com.campusguide.personal.ai.atlas.dto.AtlasHealthResponse;

public interface AtlasHealthService {

    AtlasHealthResponse getHealth();

    AtlasHealthResponse getReadiness();

    AtlasHealthResponse getLiveness();
}
