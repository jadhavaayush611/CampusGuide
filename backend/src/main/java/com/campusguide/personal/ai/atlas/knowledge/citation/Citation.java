package com.campusguide.personal.ai.atlas.knowledge.citation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Structured citation representing an explicit reference to a retrieved KnowledgeArtifact.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Citation implements Serializable {

    private static final long serialVersionUID = 1L;

    private String citationId;
    private String artifactId;
    private String citationMark;
    private SourceReference sourceReference;
    private DocumentReference documentReference;
    private SectionReference sectionReference;
    private String snippet;
    private double confidenceScore;
}
