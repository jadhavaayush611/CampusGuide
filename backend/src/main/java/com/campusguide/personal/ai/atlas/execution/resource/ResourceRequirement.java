package com.campusguide.personal.ai.atlas.execution.resource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Specification of a required resource (tool, API, compute, permission, memory, storage, network).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceRequirement implements Serializable {

    private static final long serialVersionUID = 1L;

    private String resourceId;
    private ResourceType resourceType;
    private String name;
    private double amountRequired;
    private String unit;

    @Builder.Default
    private boolean mandatory = true;

    @Builder.Default
    private FulfillmentStatus fulfillmentStatus = FulfillmentStatus.SATISFIED;

    public enum ResourceType {
        TOOL,
        API,
        COMPUTE,
        PERMISSION,
        MEMORY,
        STORAGE,
        NETWORK
    }

    public enum FulfillmentStatus {
        SATISFIED,
        UNSATISFIED,
        ALTERNATE_AVAILABLE
    }
}
