package com.campusguide.personal.ai.atlas.knowledge.ingestion.parser;

import com.campusguide.personal.ai.atlas.knowledge.ingestion.ParsedDocument;
import com.campusguide.personal.ai.atlas.knowledge.ingestion.RawDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentParserTest {

    @Test
    @DisplayName("TextDocumentParser should parse plain text and extract headings")
    void testTextDocumentParser() {
        TextDocumentParser parser = new TextDocumentParser();
        RawDocument doc = RawDocument.builder()
                .filename("campus.txt")
                .mimeType("text/plain")
                .textContent("ACADEMIC ADVISING\nAdvisor office hours are Monday to Friday.\n\nFINANCIAL AID\nScholarship deadlines are March 31.")
                .build();

        assertThat(parser.supports(doc)).isTrue();
        ParsedDocument parsed = parser.parse(doc);

        assertThat(parsed.getSections()).hasSize(2);
        assertThat(parsed.getSections().get(0).getHeading()).isEqualTo("ACADEMIC ADVISING");
        assertThat(parsed.getSections().get(1).getHeading()).isEqualTo("FINANCIAL AID");
        assertThat(parsed.getNormalizedContent()).contains("ACADEMIC ADVISING");
    }

    @Test
    @DisplayName("MarkdownDocumentParser should parse markdown headers and frontmatter")
    void testMarkdownDocumentParser() {
        MarkdownDocumentParser parser = new MarkdownDocumentParser();
        String md = "---\ntitle: Campus Life Guide\nauthor: Student Council\n---\n\n# Introduction\nWelcome to CampusGuide.\n\n## Facilities\nThe library is open 24/7.";

        RawDocument doc = RawDocument.builder()
                .filename("guide.md")
                .mimeType("text/markdown")
                .textContent(md)
                .build();

        assertThat(parser.supports(doc)).isTrue();
        ParsedDocument parsed = parser.parse(doc);

        assertThat(parsed.getTitle()).isEqualTo("Campus Life Guide");
        assertThat(parsed.getAuthor()).isEqualTo("Student Council");
        assertThat(parsed.getSections()).hasSize(2);
        assertThat(parsed.getSections().get(0).getHeading()).isEqualTo("Introduction");
        assertThat(parsed.getSections().get(1).getHeading()).isEqualTo("Facilities");
    }

    @Test
    @DisplayName("PdfDocumentParser should parse PDF page markers and sections")
    void testPdfDocumentParser() {
        PdfDocumentParser parser = new PdfDocumentParser();
        String pdfContent = "--- Page 1 ---\n1.0 ACADEMIC OVERVIEW\nThis is page 1.\n--- Page 2 ---\n2.0 COURSE REQUIREMENTS\nThis is page 2.";

        RawDocument doc = RawDocument.builder()
                .filename("handbook.pdf")
                .mimeType("application/pdf")
                .textContent(pdfContent)
                .build();

        assertThat(parser.supports(doc)).isTrue();
        ParsedDocument parsed = parser.parse(doc);

        assertThat(parsed.getPageCount()).isEqualTo(2);
        assertThat(parsed.getPages()).hasSize(2);
        assertThat(parsed.getSections()).isNotEmpty();
    }

    @Test
    @DisplayName("DocxDocumentParser should parse docx text structure")
    void testDocxDocumentParser() {
        DocxDocumentParser parser = new DocxDocumentParser();
        String docxXml = "<w:t>DEPARTMENT OF COMPUTER SCIENCE</w:t><w:t>Course Catalog 2026</w:t>";

        RawDocument doc = RawDocument.builder()
                .filename("courses.docx")
                .mimeType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                .bytes(docxXml.getBytes())
                .build();

        assertThat(parser.supports(doc)).isTrue();
        ParsedDocument parsed = parser.parse(doc);

        assertThat(parsed.getNormalizedContent()).contains("DEPARTMENT OF COMPUTER SCIENCE");
    }
}
