package com.campusguide.personal.ai.atlas.knowledge.ingestion.parser;

import com.campusguide.personal.ai.atlas.knowledge.ingestion.ParsedDocument;
import com.campusguide.personal.ai.atlas.knowledge.ingestion.RawDocument;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Document parser for Markdown files (.md, .markdown).
 */
@Component
public class MarkdownDocumentParser implements DocumentParser {

    private static final Pattern HEADER_PATTERN = Pattern.compile("^(#{1,6})\\s+(.+)$");
    private static final Pattern FRONTMATTER_PATTERN = Pattern.compile("^---\\s*\\n(.*?)\\n---\\s*\\n", Pattern.DOTALL);

    @Override
    public boolean supports(RawDocument document) {
        if (document == null) return false;
        String name = document.getFilename() != null ? document.getFilename().toLowerCase() : "";
        String mime = document.getMimeType() != null ? document.getMimeType().toLowerCase() : "";
        return name.endsWith(".md") || name.endsWith(".markdown") || mime.contains("markdown");
    }

    @Override
    public ParsedDocument parse(RawDocument document) {
        String content = extractText(document);
        Map<String, Object> frontmatter = new HashMap<>();

        String mainContent = content;
        Matcher frontmatterMatcher = FRONTMATTER_PATTERN.matcher(content);
        if (frontmatterMatcher.find()) {
            String yaml = frontmatterMatcher.group(1);
            mainContent = content.substring(frontmatterMatcher.end());
            parseYamlFrontmatter(yaml, frontmatter);
        }

        String normalized = normalize(mainContent);
        List<ParsedDocument.DocumentSection> sections = new ArrayList<>();
        String[] lines = normalized.split("\n");

        String currentHeading = frontmatter.containsKey("title") ? (String) frontmatter.get("title") : "Document Overview";
        int currentLevel = 1;
        int startLine = 1;
        StringBuilder sectionBuffer = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            Matcher headerMatcher = HEADER_PATTERN.matcher(line.trim());
            if (headerMatcher.matches()) {
                if (sectionBuffer.length() > 0) {
                    sections.add(ParsedDocument.DocumentSection.builder()
                            .heading(currentHeading)
                            .level(currentLevel)
                            .content(sectionBuffer.toString().trim())
                            .startLine(startLine)
                            .endLine(i)
                            .build());
                    sectionBuffer.setLength(0);
                }
                currentLevel = headerMatcher.group(1).length();
                currentHeading = headerMatcher.group(2).trim();
                startLine = i + 1;
            } else {
                sectionBuffer.append(line).append("\n");
            }
        }

        if (sectionBuffer.length() > 0) {
            sections.add(ParsedDocument.DocumentSection.builder()
                    .heading(currentHeading)
                            .level(currentLevel)
                            .content(sectionBuffer.toString().trim())
                            .startLine(startLine)
                            .endLine(lines.length)
                            .build());
        }

        ParsedDocument.DocumentPage singlePage = ParsedDocument.DocumentPage.builder()
                .pageNumber(1)
                .text(normalized)
                .headings(sections.stream().map(ParsedDocument.DocumentSection::getHeading).toList())
                .build();

        String docTitle = frontmatter.containsKey("title") ? (String) frontmatter.get("title") : document.getTitle();
        if (docTitle == null) {
            docTitle = document.getFilename() != null ? document.getFilename() : "Untitled Markdown";
        }

        String docAuthor = frontmatter.containsKey("author") ? (String) frontmatter.get("author") : document.getAuthor();

        return ParsedDocument.builder()
                .rawText(content)
                .normalizedContent(normalized)
                .title(docTitle)
                .author(docAuthor)
                .pageCount(1)
                .sections(sections)
                .pages(List.of(singlePage))
                .metadata(frontmatter)
                .build();
    }

    private void parseYamlFrontmatter(String yaml, Map<String, Object> target) {
        for (String line : yaml.split("\n")) {
            int colonIndex = line.indexOf(':');
            if (colonIndex > 0) {
                String key = line.substring(0, colonIndex).trim();
                String val = line.substring(colonIndex + 1).trim().replaceAll("^[\"']|[\"']$", "");
                target.put(key, val);
            }
        }
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
        String cleaned = text.replace("\r\n", "\n").replace("\r", "\n").replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");
        return cleaned.replaceAll("(?m)[ \t]+$", "").trim();
    }
}
