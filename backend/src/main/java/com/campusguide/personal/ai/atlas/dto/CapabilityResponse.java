package com.campusguide.personal.ai.atlas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapabilityResponse {
    private String atlasVersion;
    private String apiVersion;
    private String status;
    private List<String> registeredCapabilities;
    private List<String> availableWorkflows;
    private List<String> supportedFeatures;
    private List<String> supportedModels;
    private String provider;
    private Map<String, Object> limits;
}
