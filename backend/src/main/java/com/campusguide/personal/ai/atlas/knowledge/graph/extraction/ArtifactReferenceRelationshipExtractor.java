package com.campusguide.personal.ai.atlas.knowledge.graph.extraction;

import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactIdentifier;
import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactReference;
import com.campusguide.personal.ai.atlas.knowledge.artifact.KnowledgeArtifact;
import com.campusguide.personal.ai.atlas.knowledge.collection.KnowledgeCollection;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.RelationshipStrength;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.RelationshipType;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Extracts explicit references (REFERENCES, DEPENDS_ON, NEXT, PREVIOUS) between KnowledgeArtifacts.
 */
@Component
public class ArtifactReferenceRelationshipExtractor implements RelationshipExtractor {

    @Override
    public String getName() {
        return "artifact-reference-extractor";
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public List<KnowledgeEdge> extractFromArtifact(KnowledgeArtifact artifact) {
        if (artifact == null || artifact.getId() == null) {
            return Collections.emptyList();
        }

        List<KnowledgeEdge> edges = new ArrayList<>();
        NodeIdentifier sourceId = NodeIdentifier.ofArtifact(artifact.getId());

        // Extract from explicit references
        if (artifact.getReferences() != null) {
            for (ArtifactReference ref : artifact.getReferences()) {
                if (ref != null && ref.getTargetArtifactId() != null) {
                    NodeIdentifier targetId = NodeIdentifier.ofArtifact(ArtifactIdentifier.of(ref.getTargetArtifactId()));
                    String refTypeStr = ref.getReferenceType() != null ? ref.getReferenceType().name() : "REFERENCES";
                    RelationshipType relType = mapReferenceType(refTypeStr);

                    KnowledgeEdge edge = RelationshipBuilder.create()
                            .source(sourceId)
                            .target(targetId)
                            .relationship(relType)
                            .strength(RelationshipStrength.STRONG)
                            .extractor(getName())
                            .provenance("explicit-reference")
                            .sourceArtifactId(artifact.getId().getValue())
                            .build();

                    edges.add(edge);
                }
            }
        }

        return edges;
    }

    @Override
    public List<KnowledgeEdge> extractFromCollection(KnowledgeCollection collection, List<KnowledgeArtifact> artifacts) {
        if (artifacts == null || artifacts.size() < 2) {
            return Collections.emptyList();
        }
        List<KnowledgeEdge> edges = new ArrayList<>();
        // Infer sequence (NEXT / PREVIOUS) if ordered sequence hint is present
        for (int i = 0; i < artifacts.size() - 1; i++) {
            KnowledgeArtifact curr = artifacts.get(i);
            KnowledgeArtifact next = artifacts.get(i + 1);
            if (curr.getId() != null && next.getId() != null) {
                NodeIdentifier currId = NodeIdentifier.ofArtifact(curr.getId());
                NodeIdentifier nextId = NodeIdentifier.ofArtifact(next.getId());

                edges.add(RelationshipBuilder.create()
                        .source(currId)
                        .target(nextId)
                        .relationship(RelationshipType.NEXT)
                        .strength(RelationshipStrength.MEDIUM)
                        .extractor(getName())
                        .provenance("collection-sequence")
                        .build());
            }
        }
        return edges;
    }

    @Override
    public List<KnowledgeEdge> extractFromEntity(Object entity) {
        if (entity instanceof KnowledgeArtifact artifact) {
            return extractFromArtifact(artifact);
        }
        return Collections.emptyList();
    }

    private RelationshipType mapReferenceType(String refType) {
        if (refType == null) return RelationshipType.REFERENCES;
        return switch (refType.toUpperCase()) {
            case "DEPENDS_ON", "DEPENDENCY" -> RelationshipType.DEPENDS_ON;
            case "NEXT", "FOLLOWS" -> RelationshipType.NEXT;
            case "PREVIOUS", "PRECEDES" -> RelationshipType.PREVIOUS;
            case "PREREQUISITE" -> RelationshipType.PREREQUISITE;
            default -> RelationshipType.REFERENCES;
        };
    }
}
