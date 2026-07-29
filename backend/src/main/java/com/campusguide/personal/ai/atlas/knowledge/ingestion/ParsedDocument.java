package com.campusguide.personal.ai.atlas.knowledge.ingestion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Representation of a parsed document with structure, sections, page info, and metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedDocument {

    private String rawText;
    private String normalizedContent;
    private String title;
    private String author;
    private Integer pageCount;
    @Builder.Default
    private List<DocumentSection> sections = new ArrayList<>();
    @Builder.Default
    private List<DocumentPage> pages = new ArrayList<>();
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentSection {
        private String heading;
        private int level;
        private String content;
        private Integer pageNumber;
        private Integer startLine;
        private Integer endLine;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentPage {
        private int pageNumber;
        private String text;
        @Builder.Default
        private List<String> headings = new ArrayList<>();
    }
}
