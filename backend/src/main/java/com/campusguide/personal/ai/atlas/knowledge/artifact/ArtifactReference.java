package com.campusguide.personal.ai.atlas.knowledge.artifact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * References and structural links between KnowledgeArtifacts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtifactReference implements Serializable {

    private static final long serialVersionUID = 1L;

    private String targetArtifactId;
    private ReferenceType referenceType;
    @Builder.Default
    private Map<String, Object> relationshipMetadata = new HashMap<>();

    public enum ReferenceType {
        PARENT_DOCUMENT,
        CHILD_CHUNK,
        CROSS_REFERENCE,
        SECTION_PARENT,
        PREVIOUS_CHUNK,
        NEXT_CHUNK
    }

    public static ArtifactReference parent(String parentId) {
        return ArtifactReference.builder()
                .targetArtifactId(parentId)
                .referenceType(ReferenceType.PARENT_DOCUMENT)
                .build();
    }

    public static ArtifactReference child(String childId) {
        return ArtifactReference.builder()
                .targetArtifactId(childId)
                .referenceType(ReferenceType.CHILD_CHUNK)
                .build();
    }
}
