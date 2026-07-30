package com.campusguide.personal.ai.atlas.execution.explanation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Supporting evidence for an execution preparation decision.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionEvidence implements Serializable {

    private static final long serialVersionUID = 1L;

    private String evidenceId;
    private String type;
    private String source;
    private String description;
    private double confidence;
}
