package com.campusguide.personal.ai.atlas.knowledge.ingestion;

import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactMetadata;
import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactSource;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Extracts structured metadata and source provenance from RawDocument and ParsedDocument.
 */
@Component
public class MetadataExtractor {

    public ArtifactMetadata extractMetadata(RawDocument rawDocument, ParsedDocument parsedDocument) {
        ArtifactMetadata metadata = new ArtifactMetadata();
        String title = parsedDocument.getTitle() != null ? parsedDocument.getTitle() : rawDocument.getTitle();
        metadata.setName(title != null ? title : rawDocument.getFilename());
        metadata.setCategory(rawDocument.getMimeType());
        metadata.setDomain("campus_knowledge");
        metadata.setLanguage("en");
        metadata.setSizeInBytes(rawDocument.getBytes() != null ? (long) rawDocument.getBytes().length : 0L);

        Map<String, Object> attributes = new HashMap<>();
        if (parsedDocument.getMetadata() != null) {
            attributes.putAll(parsedDocument.getMetadata());
        }
        if (parsedDocument.getPageCount() != null) {
            attributes.put("pageCount", parsedDocument.getPageCount());
        }
        if (parsedDocument.getSections() != null) {
            attributes.put("sectionCount", parsedDocument.getSections().size());
        }
        if (rawDocument.getAttributes() != null) {
            attributes.putAll(rawDocument.getAttributes());
        }

        metadata.setAttributes(attributes);
        return metadata;
    }

    public ArtifactSource extractSource(RawDocument rawDocument, ParsedDocument parsedDocument) {
        return ArtifactSource.builder()
                .sourceUri(rawDocument.getUri())
                .sourceType(rawDocument.getMimeType())
                .title(parsedDocument.getTitle() != null ? parsedDocument.getTitle() : rawDocument.getTitle())
                .author(parsedDocument.getAuthor() != null ? parsedDocument.getAuthor() : rawDocument.getAuthor())
                .pageNumber(parsedDocument.getPageCount())
                .creationTimestamp(Instant.now())
                .attributes(new HashMap<>(rawDocument.getAttributes()))
                .build();
    }
}
