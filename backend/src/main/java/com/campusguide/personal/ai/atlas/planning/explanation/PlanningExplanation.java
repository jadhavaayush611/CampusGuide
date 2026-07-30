package com.campusguide.personal.ai.atlas.planning.explanation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates complete explainability for an ExecutionPlan.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningExplanation implements Serializable {

    private static final long serialVersionUID = 1L;

    private String planId;
    private String primaryRationale;
    private String orderingRationale;
    private String dependencyReasoning;
    private String schedulingRationale;
    private String optimizationRationale;

    @Builder.Default
    private List<PlanningReason> reasons = new ArrayList<>();

    @Builder.Default
    private List<PlanningEvidence> evidenceList = new ArrayList<>();
}
