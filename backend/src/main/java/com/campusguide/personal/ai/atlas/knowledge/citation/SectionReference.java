package com.campusguide.personal.ai.atlas.knowledge.citation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Encapsulates section-level and chunk offset metadata for a Citation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionReference implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sectionTitle;
    private Integer startOffset;
    private Integer endOffset;
    private Integer pageNumber;
    private Integer chunkIndex;
}
