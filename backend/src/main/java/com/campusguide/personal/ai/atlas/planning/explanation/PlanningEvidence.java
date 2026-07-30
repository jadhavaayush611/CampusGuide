package com.campusguide.personal.ai.atlas.planning.explanation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Supporting evidence for planning decisions and task structures.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningEvidence implements Serializable {

    private static final long serialVersionUID = 1L;

    private String evidenceId;
    private String source;
    private String type;
    private String description;
    private double relevanceScore;
}
