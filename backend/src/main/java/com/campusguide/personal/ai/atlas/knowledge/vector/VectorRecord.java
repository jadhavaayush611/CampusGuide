package com.campusguide.personal.ai.atlas.knowledge.vector;

import com.campusguide.personal.ai.atlas.knowledge.artifact.KnowledgeArtifact;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Universal vector record linking artifact ID, vector embedding, metadata, and KnowledgeArtifact payload.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private String artifactId;
    private float[] vector;
    @Builder.Default
    private VectorMetadata metadata = new VectorMetadata();
    private KnowledgeArtifact artifact;

    public static VectorRecord fromArtifact(KnowledgeArtifact artifact) {
        if (artifact == null || artifact.getEmbedding() == null) {
            throw new IllegalArgumentException("Artifact must possess an embedding to create a VectorRecord");
        }

        VectorMetadata vm = VectorMetadata.builder()
                .documentId(artifact.getId().getValue())
                .collectionId(artifact.getCollectionId())
                .sourceType(artifact.getSource().getSourceType())
                .category(artifact.getMetadata().getCategory())
                .domain(artifact.getMetadata().getDomain())
                .fields(new java.util.HashMap<>(artifact.getMetadata().getAttributes()))
                .build();

        return VectorRecord.builder()
                .artifactId(artifact.getId().getValue())
                .vector(artifact.getEmbedding().getVector())
                .metadata(vm)
                .artifact(artifact)
                .build();
    }
}
