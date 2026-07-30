package com.campusguide.personal.ai.atlas.knowledge.graph.extraction;

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
 * Extracts BELONGS_TO and CONTAINS relationships between KnowledgeCollections and KnowledgeArtifacts.
 */
@Component
public class CollectionRelationshipExtractor implements RelationshipExtractor {

    @Override
    public String getName() {
        return "collection-relationship-extractor";
    }

    @Override
    public int getPriority() {
        return 20;
    }

    @Override
    public List<KnowledgeEdge> extractFromArtifact(KnowledgeArtifact artifact) {
        if (artifact == null || artifact.getId() == null || artifact.getCollectionId() == null) {
            return Collections.emptyList();
        }

        NodeIdentifier artifactNodeId = NodeIdentifier.ofArtifact(artifact.getId());
        NodeIdentifier collectionNodeId = NodeIdentifier.ofCollection(artifact.getCollectionId());

        KnowledgeEdge edge = RelationshipBuilder.create()
                .source(artifactNodeId)
                .target(collectionNodeId)
                .relationship(RelationshipType.BELONGS_TO)
                .strength(RelationshipStrength.DEFINITIVE)
                .extractor(getName())
                .provenance("artifact-collection-membership")
                .sourceArtifactId(artifact.getId().getValue())
                .build();

        return List.of(edge);
    }

    @Override
    public List<KnowledgeEdge> extractFromCollection(KnowledgeCollection collection, List<KnowledgeArtifact> artifacts) {
        if (collection == null || collection.getCollectionId() == null || artifacts == null) {
            return Collections.emptyList();
        }

        List<KnowledgeEdge> edges = new ArrayList<>();
        NodeIdentifier collectionNodeId = NodeIdentifier.ofCollection(collection.getCollectionId());

        for (KnowledgeArtifact artifact : artifacts) {
            if (artifact != null && artifact.getId() != null) {
                NodeIdentifier artifactNodeId = NodeIdentifier.ofArtifact(artifact.getId());

                // Collection CONTAINS Artifact
                edges.add(RelationshipBuilder.create()
                        .source(collectionNodeId)
                        .target(artifactNodeId)
                        .relationship(RelationshipType.CONTAINS)
                        .strength(RelationshipStrength.DEFINITIVE)
                        .extractor(getName())
                        .provenance("collection-contains-artifact")
                        .build());

                // Artifact BELONGS_TO Collection
                edges.add(RelationshipBuilder.create()
                        .source(artifactNodeId)
                        .target(collectionNodeId)
                        .relationship(RelationshipType.BELONGS_TO)
                        .strength(RelationshipStrength.DEFINITIVE)
                        .extractor(getName())
                        .provenance("artifact-belongs-to-collection")
                        .sourceArtifactId(artifact.getId().getValue())
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
}
