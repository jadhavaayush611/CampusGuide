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
 * Document parser for Microsoft Word DOCX documents (.docx).
 */
@Component
public class DocxDocumentParser implements DocumentParser {

    private static final Pattern DOCX_HEADING = Pattern.compile("^(?:Heading\\s+\\d+|[A-Z0-9\\s\\-]{4,60}:?|[0-9]+\\.\\s+[A-Z].*)$");
    private static final Pattern WORD_XML_TEXT = Pattern.compile("<w:t[^>]*>(.*?)</w:t>");

    @Override
    public boolean supports(RawDocument document) {
        if (document == null) return false;
        String name = document.getFilename() != null ? document.getFilename().toLowerCase() : "";
        String mime = document.getMimeType() != null ? document.getMimeType().toLowerCase() : "";
        return name.endsWith(".docx") || mime.contains("wordprocessingml");
    }

    @Override
    public ParsedDocument parse(RawDocument document) {
        String content = extractText(document);
        String normalized = normalize(content);

        List<ParsedDocument.DocumentSection> sections = new ArrayList<>();
        String[] lines = normalized.split("\n");

        String currentHeading = document.getTitle() != null ? document.getTitle() : "Document Start";
        int currentStartLine = 1;
        StringBuilder sectionBuffer = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (DOCX_HEADING.matcher(line).matches()) {
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

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("format", "DOCX");

        return ParsedDocument.builder()
                .rawText(content)
                .normalizedContent(normalized)
                .title(document.getTitle() != null ? document.getTitle() : document.getFilename())
                .author(document.getAuthor())
                .pageCount(1)
                .sections(sections)
                .pages(List.of(singlePage))
                .metadata(metadata)
                .build();
    }

    private String extractText(RawDocument document) {
        if (document.getTextContent() != null) {
            return document.getTextContent();
        }
        if (document.getBytes() != null) {
            String rawStr = new String(document.getBytes(), StandardCharsets.ISO_8859_1);
            Matcher matcher = WORD_XML_TEXT.matcher(rawStr);
            StringBuilder xmlText = new StringBuilder();
            while (matcher.find()) {
                xmlText.append(matcher.group(1)).append(" ");
            }
            if (xmlText.length() > 0) {
                return xmlText.toString();
            }
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
