package com.campusguide.personal.ai.atlas.knowledge.ingestion.parser;

import com.campusguide.personal.ai.atlas.knowledge.ingestion.ParsedDocument;
import com.campusguide.personal.ai.atlas.knowledge.ingestion.RawDocument;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Document parser for plain text files (.txt).
 */
@Component
public class TextDocumentParser implements DocumentParser {

    private static final Pattern HEADING_PATTERN = Pattern.compile("^(?:[A-Z0-9\\.\\s\\-]{3,60}:?|[0-9]+\\.\\s+[A-Z].*)$");

    @Override
    public boolean supports(RawDocument document) {
        if (document == null) return false;
        String name = document.getFilename() != null ? document.getFilename().toLowerCase() : "";
        String mime = document.getMimeType() != null ? document.getMimeType().toLowerCase() : "";
        return name.endsWith(".txt") || mime.contains("text/plain") || (name.isBlank() && mime.isBlank());
    }

    @Override
    public ParsedDocument parse(RawDocument document) {
        String content = extractText(document);
        String normalized = normalize(content);

        List<ParsedDocument.DocumentSection> sections = new ArrayList<>();
        String[] lines = normalized.split("\n");
        int currentStartLine = 1;
        StringBuilder sectionBuffer = new StringBuilder();
        String currentHeading = "Overview";

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            Matcher matcher = HEADING_PATTERN.matcher(line);
            if (!line.isEmpty() && line.toUpperCase().equals(line) && line.length() < 60 || matcher.matches()) {
                if (sectionBuffer.length() > 0) {
                    sections.add(ParsedDocument.DocumentSection.builder()
                            .heading(currentHeading)
                            .level(1)
                            .content(sectionBuffer.toString().trim())
                            .startLine(currentStartLine)
                            .endLine(i)
                            .build());
                    sectionBuffer.setLength(0);
                }
                currentHeading = line;
                currentStartLine = i + 1;
            } else {
                sectionBuffer.append(lines[i]).append("\n");
            }
        }

        if (sectionBuffer.length() > 0) {
            sections.add(ParsedDocument.DocumentSection.builder()
                    .heading(currentHeading)
                    .level(1)
                    .content(sectionBuffer.toString().trim())
                    .startLine(currentStartLine)
                    .endLine(lines.length)
                    .build());
        }

        ParsedDocument.DocumentPage singlePage = ParsedDocument.DocumentPage.builder()
                .pageNumber(1)
                .text(normalized)
                .headings(sections.stream().map(ParsedDocument.DocumentSection::getHeading).toList())
                .build();

        return ParsedDocument.builder()
                .rawText(content)
                .normalizedContent(normalized)
                .title(document.getTitle() != null ? document.getTitle() : document.getFilename())
                .author(document.getAuthor())
                .pageCount(1)
                .sections(sections)
                .pages(List.of(singlePage))
                .build();
    }

    private String extractText(RawDocument document) {
        if (document.getTextContent() != null) {
            return document.getTextContent();
        }
        if (document.getBytes() != null) {
            return new String(document.getBytes(), StandardCharsets.UTF_8);
        }
        return "";
    }

    private String normalize(String text) {
        if (text == null) return "";
        // Normalize line endings, remove null characters & unprintable control codes
        String cleaned = text.replace("\r\n", "\n").replace("\r", "\n").replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");
        // Remove trailing spaces on lines
        return cleaned.replaceAll("(?m)[ \t]+$", "").trim();
    }
}
