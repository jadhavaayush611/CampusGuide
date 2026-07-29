package com.campusguide.personal.ai.atlas.context.evidence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Structured evidence unit describing why a specific context segment was retrieved.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetrievalEvidence {

    @Builder.Default
    private String id = UUID.randomUUID().toString();

    private EvidenceType type;
    private EvidenceSource source;
    private String entityKey;
    private String contentSnippet;
    private String rationale;

    @Builder.Default
    private long timestamp = System.currentTimeMillis();

    @Builder.Default
    private EvidenceScore score = new EvidenceScore();

    @Builder.Default
    private Map<String, Object> metadata = new LinkedHashMap<>();
}
