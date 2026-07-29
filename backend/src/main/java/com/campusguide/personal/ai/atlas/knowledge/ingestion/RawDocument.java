package com.campusguide.personal.ai.atlas.knowledge.ingestion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.InputStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates raw input documents prior to parsing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawDocument {

    private String uri;
    private String filename;
    private String mimeType;
    private byte[] bytes;
    private String textContent;
    private String author;
    private String title;
    @Builder.Default
    private Instant loadedAt = Instant.now();
    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();
}
