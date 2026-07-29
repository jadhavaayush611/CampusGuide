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
 * Document parser for PDF documents (.pdf).
 */
@Component
public class PdfDocumentParser implements DocumentParser {

    private static final Pattern PAGE_MARKER = Pattern.compile("(?i)(?:---?\\s*Page\\s*(\\d+)\\s*---?|\f)");
    private static final Pattern PDF_HEADING = Pattern.compile("^(?:[0-9]+\\.[0-9]*\\s+[A-Z].*|[A-Z0-9\\s\\-]{4,60}:?)$");
    private static final Pattern PDF_TITLE_META = Pattern.compile("/Title\\s*\\((.*?)\\)");
    private static final Pattern PDF_AUTHOR_META = Pattern.compile("/Author\\s*\\((.*?)\\)");

    @Override
    public boolean supports(RawDocument document) {
        if (document == null) return false;
        String name = document.getFilename() != null ? document.getFilename().toLowerCase() : "";
        String mime = document.getMimeType() != null ? document.getMimeType().toLowerCase() : "";
        return name.endsWith(".pdf") || mime.contains("pdf");
    }

    @Override
    public ParsedDocument parse(RawDocument document) {
        String rawText = extractRawText(document);
        Map<String, Object> metadata = extractPdfMetadata(document);

        List<ParsedDocument.DocumentPage> pages = new ArrayList<>();
        List<ParsedDocument.DocumentSection> sections = new ArrayList<>();

        String[] pageSplits = PAGE_MARKER.split(rawText);
        if (pageSplits.length == 0 || (pageSplits.length == 1 && pageSplits[0].isBlank())) {
            pageSplits = new String[]{rawText};
        }

        StringBuilder fullNormalized = new StringBuilder();
        int pageNum = 1;

        for (String pageRaw : pageSplits) {
            if (pageRaw.isBlank()) continue;
            String normalizedPage = normalize(pageRaw);
            fullNormalized.append(normalizedPage).append("\n\n");

            List<String> pageHeadings = new ArrayList<>();
            String[] lines = normalizedPage.split("\n");
            for (String line : lines) {
                String trimmed = line.trim();
                if (PDF_HEADING.matcher(trimmed).matches()) {
                    pageHeadings.add(trimmed);
                }
            }

            pages.add(ParsedDocument.DocumentPage.builder()
                    .pageNumber(pageNum++)
                    .text(normalizedPage)
                    .headings(pageHeadings)
                    .build());
        }

        String completeNormalized = fullNormalized.toString().trim();
        String[] lines = completeNormalized.split("\n");
        String currentHeading = metadata.containsKey("title") ? (String) metadata.get("title") : "Document Start";
        int currentStartLine = 1;
        StringBuilder sectionBuffer = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (PDF_HEADING.matcher(line).matches()) {
                if (sectionBuffer.length() > 0) {
                    sections.add(ParsedDocument.DocumentSection.builder()
                            .heading(currentHeading)
                            .level(1)
                            .content(sectionBuffer.toString().trim())
                            .startLine(currentStartLine)
                            .endLine(i)
                            .pageNumber(findPageForLine(lines[i], pages))
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
                    .pageNumber(pages.isEmpty() ? 1 : pages.size())
                    .build());
        }

        String docTitle = (String) metadata.getOrDefault("title", document.getTitle() != null ? document.getTitle() : document.getFilename());
        String docAuthor = (String) metadata.getOrDefault("author", document.getAuthor());

        return ParsedDocument.builder()
                .rawText(rawText)
                .normalizedContent(completeNormalized)
                .title(docTitle)
                .author(docAuthor)
                .pageCount(pages.size())
                .sections(sections)
                .pages(pages)
                .metadata(metadata)
                .build();
    }

    private int findPageForLine(String line, List<ParsedDocument.DocumentPage> pages) {
        for (ParsedDocument.DocumentPage page : pages) {
            if (page.getText() != null && page.getText().contains(line)) {
                return page.getPageNumber();
            }
        }
        return 1;
    }

    private String extractRawText(RawDocument document) {
        if (document.getTextContent() != null) {
            return document.getTextContent();
        }
        if (document.getBytes() != null) {
            String rawStr = new String(document.getBytes(), StandardCharsets.ISO_8859_1);
            // Extracts Tj / TJ text blocks if raw PDF binary
            StringBuilder pdfText = new StringBuilder();
            Matcher matcher = Pattern.compile("\\((.*?)\\)\\s*Tj").matcher(rawStr);
            while (matcher.find()) {
                pdfText.append(matcher.group(1)).append(" ");
            }
            if (pdfText.length() > 0) {
                return pdfText.toString();
            }
            return new String(document.getBytes(), StandardCharsets.UTF_8);
        }
        return "";
    }

    private Map<String, Object> extractPdfMetadata(RawDocument document) {
        Map<String, Object> meta = new HashMap<>();
        if (document.getBytes() != null) {
            String rawStr = new String(document.getBytes(), StandardCharsets.ISO_8859_1);
            Matcher titleMatcher = PDF_TITLE_META.matcher(rawStr);
            if (titleMatcher.find()) {
                meta.put("title", titleMatcher.group(1));
            }
            Matcher authorMatcher = PDF_AUTHOR_META.matcher(rawStr);
            if (authorMatcher.find()) {
                meta.put("author", authorMatcher.group(1));
            }
        }
        return meta;
    }

    private String normalize(String text) {
        if (text == null) return "";
        String cleaned = text.replace("\r\n", "\n").replace("\r", "\n").replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");
        return cleaned.replaceAll("(?m)[ \t]+$", "").trim();
    }
}
