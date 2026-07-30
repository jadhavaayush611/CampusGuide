package com.campusguide.personal.ai.atlas.knowledge.graph.explainability;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * Individual step breakdown in a structured graph reasoning explanation.
 */
@Data
@Builder
public class ExplanationStep implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int stepIndex;
    private final String sourceNodeId;
    private final String sourceNodeName;
    private final String targetNodeId;
    private final String targetNodeName;
    private final String relationshipType;
    private final double stepConfidence;
    private final String explanationText;
}
