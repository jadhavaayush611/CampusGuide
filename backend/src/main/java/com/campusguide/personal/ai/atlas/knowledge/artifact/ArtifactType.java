package com.campusguide.personal.ai.atlas.knowledge.artifact;

/**
 * Universal artifact type enum representing the type/source format of a KnowledgeArtifact.
 */
public enum ArtifactType {
    DOCUMENT,
    CHUNK,
    PDF,
    DOCX,
    MARKDOWN,
    TXT,
    HTML,
    OCR,
    CSV,
    URL,
    SECTION;

    public static ArtifactType fromExtensionOrMime(String filenameOrMime) {
        if (filenameOrMime == null) {
            return TXT;
        }
        String lower = filenameOrMime.toLowerCase();
        if (lower.endsWith(".pdf") || lower.contains("application/pdf")) {
            return PDF;
        }
        if (lower.endsWith(".docx") || lower.contains("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            return DOCX;
        }
        if (lower.endsWith(".md") || lower.endsWith(".markdown") || lower.contains("text/markdown")) {
            return MARKDOWN;
        }
        if (lower.endsWith(".html") || lower.endsWith(".htm") || lower.contains("text/html")) {
            return HTML;
        }
        if (lower.endsWith(".csv") || lower.contains("text/csv")) {
            return CSV;
        }
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            return URL;
        }
        return TXT;
    }
}
