package com.campusguide.personal.ai.atlas.planning.explanation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Structured planning reason detailing why tasks were ordered, scheduled, or optimized.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningReason implements Serializable {

    private static final long serialVersionUID = 1L;

    private String reasonId;

    @Builder.Default
    private ReasonCategory category = ReasonCategory.TASK_ORDERING;

    private String summary;
    private String impact;
}
