package com.campusguide.personal.ai.atlas.knowledge.ingestion;

import org.springframework.stereotype.Component;

/**
 * Extracts and cleans normalized text content from parsed document models.
 */
@Component
public class ContentExtractor {

    public String extractNormalizedContent(ParsedDocument parsedDocument) {
        if (parsedDocument == null) return "";
        if (parsedDocument.getNormalizedContent() != null && !parsedDocument.getNormalizedContent().isBlank()) {
            return sanitize(parsedDocument.getNormalizedContent());
        }
        if (parsedDocument.getRawText() != null) {
            return sanitize(parsedDocument.getRawText());
        }
        return "";
    }

    private String sanitize(String input) {
        if (input == null) return "";
        String clean = input.replace("\r\n", "\n").replace("\r", "\n").replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");
        return clean.replaceAll("\n{3,}", "\n\n").trim();
    }
}
