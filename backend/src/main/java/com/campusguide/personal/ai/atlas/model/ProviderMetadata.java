package com.campusguide.personal.ai.atlas.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderMetadata {
    private String name;
    private String version;
    private List<String> supportedModels;
    private boolean active;
}
