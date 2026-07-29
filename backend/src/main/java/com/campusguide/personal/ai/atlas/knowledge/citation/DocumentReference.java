package com.campusguide.personal.ai.atlas.knowledge.citation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Encapsulates document-level boundary metadata for a Citation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentReference implements Serializable {

    private static final long serialVersionUID = 1L;

    private String documentId;
    private String documentTitle;
    private String collectionId;
    private String category;
}
