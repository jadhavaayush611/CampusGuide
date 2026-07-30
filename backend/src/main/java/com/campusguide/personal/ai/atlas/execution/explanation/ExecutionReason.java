package com.campusguide.personal.ai.atlas.execution.explanation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Explanation reason item providing rationale for preparation decisions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionReason implements Serializable {

    private static final long serialVersionUID = 1L;

    private String reasonId;
    private ReasonCategory category;
    private String title;
    private String description;
    private double impactScore;
}
