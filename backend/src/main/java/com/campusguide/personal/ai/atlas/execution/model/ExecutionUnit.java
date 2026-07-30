package com.campusguide.personal.ai.atlas.execution.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Granular unit of execution derived from PlanningTask/PlanningStep.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionUnit implements Serializable {

    private static final long serialVersionUID = 1L;

    private String unitId;
    private String taskId;
    private String stepId;
    private String title;
    private String description;

    @Builder.Default
    private ExecutionUnitType unitType = ExecutionUnitType.ACTION;

    private String targetCapability;

    @Builder.Default
    private Map<String, Object> payload = new HashMap<>();

    @Builder.Default
    private List<String> dependencies = new ArrayList<>();

    @Builder.Default
    private long timeoutSeconds = 60L;

    @Builder.Default
    private ExecutionRetryPolicy retryPolicy = ExecutionRetryPolicy.defaultConfig();

    @Builder.Default
    private ExecutionRollbackPolicy rollbackPolicy = ExecutionRollbackPolicy.defaultConfig();

    @Builder.Default
    private boolean approvalRequired = false;

    @Builder.Default
    private boolean mandatory = true;

    @Builder.Default
    private UnitPreparationStatus status = UnitPreparationStatus.READY;

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}
