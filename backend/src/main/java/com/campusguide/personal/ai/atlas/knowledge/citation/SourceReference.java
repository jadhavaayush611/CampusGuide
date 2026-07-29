package com.campusguide.personal.ai.atlas.knowledge.citation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Encapsulates source provenance metadata for a Citation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SourceReference implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sourceUri;
    private String sourceType;
    private String title;
    private String author;
}
