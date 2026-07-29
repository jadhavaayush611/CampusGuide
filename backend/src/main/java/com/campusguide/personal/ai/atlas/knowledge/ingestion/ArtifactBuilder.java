package com.campusguide.personal.ai.atlas.knowledge.ingestion;

import com.campusguide.personal.ai.atlas.knowledge.artifact.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;

/**
 * Builder component for creating initialized, valid KnowledgeArtifact instances.
 */
@Component
public class ArtifactBuilder {

    public KnowledgeArtifact buildDocumentArtifact(RawDocument raw, ParsedDocument parsed, ArtifactMetadata metadata, ArtifactSource source) {
        String content = parsed != null ? parsed.getNormalizedContent() : (raw != null ? raw.getTextContent() : "");
        ArtifactType type = ArtifactType.fromExtensionOrMime(raw != null ? raw.getFilename() : null);

        ArtifactIdentifier id = ArtifactIdentifier.generate();
        ArtifactVersion version = ArtifactVersion.initial(content);

        return KnowledgeArtifact.builder()
                .id(id)
                .type(type)
                .content(content)
                .metadata(metadata != null ? metadata : new ArtifactMetadata())
                .source(source != null ? source : new ArtifactSource())
                .version(version)
                .references(new ArrayList<>())
                .lifecycleState(ArtifactLifecycleState.PARSED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public KnowledgeArtifact buildChunkArtifact(KnowledgeArtifact parent, String chunkContent, int chunkIndex, ArtifactSource chunkSource) {
        ArtifactIdentifier chunkId = ArtifactIdentifier.generateChunkId(parent.getId(), chunkIndex);
        ArtifactVersion chunkVersion = ArtifactVersion.initial(chunkContent);

        ArtifactMetadata chunkMeta = ArtifactMetadata.builder()
                .name(parent.getMetadata().getName() + " [Chunk " + chunkIndex + "]")
                .category(parent.getMetadata().getCategory())
                .domain(parent.getMetadata().getDomain())
                .language(parent.getMetadata().getLanguage())
                .sizeInBytes((long) chunkContent.getBytes().length)
                .attributes(new java.util.HashMap<>(parent.getMetadata().getAttributes()))
                .build();
        chunkMeta.put("chunkIndex", chunkIndex);

        KnowledgeArtifact chunk = KnowledgeArtifact.builder()
                .id(chunkId)
                .type(ArtifactType.CHUNK)
                .content(chunkContent)
                .metadata(chunkMeta)
                .source(chunkSource != null ? chunkSource : parent.getSource())
                .version(chunkVersion)
                .references(new ArrayList<>())
                .lifecycleState(ArtifactLifecycleState.CHUNKED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        chunk.addReference(ArtifactReference.parent(parent.getId().getValue()));
        return chunk;
    }
}
