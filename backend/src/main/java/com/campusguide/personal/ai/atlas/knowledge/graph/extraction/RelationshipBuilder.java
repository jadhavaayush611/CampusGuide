package com.campusguide.personal.ai.atlas.knowledge.graph.extraction;

import com.campusguide.personal.ai.atlas.knowledge.graph.edge.EdgeMetadata;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.RelationshipStrength;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.RelationshipType;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;

import java.time.Instant;

/**
 * Fluent builder for creating KnowledgeEdge instances cleanly during relationship extraction.
 */
public class RelationshipBuilder {

    private NodeIdentifier sourceNodeId;
    private NodeIdentifier targetNodeId;
    private RelationshipType relationshipType = RelationshipType.RELATED_TO;
    private RelationshipStrength strength = RelationshipStrength.MEDIUM;
    private boolean bidirectional = false;
    private String extractorName = "rule-based";
    private String provenance = "inferred";
    private String sourceArtifactId;

    public static RelationshipBuilder create() {
        return new RelationshipBuilder();
    }

    public RelationshipBuilder source(NodeIdentifier sourceNodeId) {
        this.sourceNodeId = sourceNodeId;
        return this;
    }

    public RelationshipBuilder target(NodeIdentifier targetNodeId) {
        this.targetNodeId = targetNodeId;
        return this;
    }

    public RelationshipBuilder relationship(RelationshipType type) {
        if (type != null) this.relationshipType = type;
        return this;
    }

    public RelationshipBuilder strength(RelationshipStrength strength) {
        if (strength != null) this.strength = strength;
        return this;
    }

    public RelationshipBuilder strength(double weight) {
        this.strength = RelationshipStrength.of(weight);
        return this;
    }

    public RelationshipBuilder bidirectional(boolean bidirectional) {
        this.bidirectional = bidirectional;
        return this;
    }

    public RelationshipBuilder extractor(String extractorName) {
        if (extractorName != null) this.extractorName = extractorName;
        return this;
    }

    public RelationshipBuilder provenance(String provenance) {
        if (provenance != null) this.provenance = provenance;
        return this;
    }

    public RelationshipBuilder sourceArtifactId(String sourceArtifactId) {
        this.sourceArtifactId = sourceArtifactId;
        return this;
    }

    public KnowledgeEdge build() {
        if (sourceNodeId == null || targetNodeId == null) {
            throw new IllegalStateException("RelationshipBuilder requires both source and target node identifiers");
        }

        String edgeId = KnowledgeEdge.generateId(sourceNodeId, targetNodeId, relationshipType);

        EdgeMetadata metadata = EdgeMetadata.builder()
                .extractorName(extractorName)
                .provenance(provenance)
                .sourceArtifactId(sourceArtifactId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return KnowledgeEdge.builder()
                .id(edgeId)
                .sourceNodeId(sourceNodeId)
                .targetNodeId(targetNodeId)
                .relationshipType(relationshipType)
                .strength(strength)
                .bidirectional(bidirectional)
                .metadata(metadata)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
