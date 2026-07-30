package com.campusguide.personal.ai.atlas.execution.tool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface/DTO representing a tool capability available to Atlas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolCapability implements Serializable {

    private static final long serialVersionUID = 1L;

    private String capabilityId;
    private String capabilityName;
    private String domain;
    private String toolId;

    @Builder.Default
    private List<String> requiredPermissions = new ArrayList<>();

    @Builder.Default
    private List<String> inputTypes = new ArrayList<>();

    @Builder.Default
    private List<String> outputTypes = new ArrayList<>();

    @Builder.Default
    private String version = "1.0.0";

    @Builder.Default
    private boolean available = true;

    @Builder.Default
    private String description = "";
}
