package com.campusguide.personal.ai.atlas.knowledge.ingestion;

import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactVersion;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads raw documents from files, byte arrays, or strings into RawDocument containers.
 */
@Component
public class DocumentLoader {

    public RawDocument loadFromText(String content, String title, String sourceUri) {
        if (content == null) {
            content = "";
        }
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("checksum", ArtifactVersion.computeChecksum(content));

        return RawDocument.builder()
                .uri(sourceUri != null ? sourceUri : "text://inline")
                .filename(title != null ? title + ".txt" : "document.txt")
                .mimeType("text/plain")
                .textContent(content)
                .bytes(content.getBytes())
                .title(title != null ? title : "Inline Text")
                .loadedAt(Instant.now())
                .attributes(attributes)
                .build();
    }

    public RawDocument loadFromBytes(byte[] bytes, String filename, String mimeType, String sourceUri) {
        String contentStr = bytes != null ? new String(bytes) : "";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sizeInBytes", bytes != null ? (long) bytes.length : 0L);
        attributes.put("checksum", ArtifactVersion.computeChecksum(contentStr));

        return RawDocument.builder()
                .uri(sourceUri != null ? sourceUri : "file://" + filename)
                .filename(filename)
                .mimeType(mimeType != null ? mimeType : guessMimeType(filename))
                .bytes(bytes)
                .textContent(contentStr)
                .title(filename)
                .loadedAt(Instant.now())
                .attributes(attributes)
                .build();
    }

    public RawDocument loadFromFile(File file) {
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            return loadFromBytes(bytes, file.getName(), guessMimeType(file.getName()), file.toURI().toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load document from file: " + file.getAbsolutePath(), e);
        }
    }

    public RawDocument loadFromPath(Path path) {
        return loadFromFile(path.toFile());
    }

    private String guessMimeType(String filename) {
        if (filename == null) return "text/plain";
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".md") || lower.endsWith(".markdown")) return "text/markdown";
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html";
        if (lower.endsWith(".csv")) return "text/csv";
        return "text/plain";
    }
}
