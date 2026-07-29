package com.campusguide.personal.ai.atlas.knowledge.ingestion.parser;

import com.campusguide.personal.ai.atlas.knowledge.ingestion.ParsedDocument;
import com.campusguide.personal.ai.atlas.knowledge.ingestion.RawDocument;

/**
 * Strategy interface for parsing raw documents into structured ParsedDocuments.
 */
public interface DocumentParser {

    /**
     * Determines whether this parser supports the given raw document filename or MIME type.
     */
    boolean supports(RawDocument document);

    /**
     * Parses the raw document into a structured ParsedDocument.
     */
    ParsedDocument parse(RawDocument document);
}
