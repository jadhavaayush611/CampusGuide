package com.campusguide.personal.ai.atlas.execution.resource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Status report of available resources vs required resources without actually allocating resources.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceAllocation implements Serializable {

    private static final long serialVersionUID = 1L;

    private String allocationId;

    @Builder.Default
    private List<ResourceRequirement> requirements = new ArrayList<>();

    @Builder.Default
    private boolean allRequirementsSatisfied = true;

    @Builder.Default
    private int missingResourceCount = 0;

    @Builder.Default
    private String allocationStrategy = "DRY_RUN_ANALYSIS";

    public static ResourceAllocation empty() {
        return ResourceAllocation.builder()
                .allocationId("alloc_empty")
                .requirements(new ArrayList<>())
                .allRequirementsSatisfied(true)
                .missingResourceCount(0)
                .allocationStrategy("DRY_RUN_ANALYSIS")
                .build();
    }
}
