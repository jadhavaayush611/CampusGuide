package com.campusguide.personal.ai.atlas.context.fusion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Record of a context fusion operation (e.g. duplicate removal, field merging).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FusionDecision {
    private String key;
    private String targetDomain;
    private String action; // e.g. "DEDUPLICATED", "MERGED", "PRESERVED_EVIDENCE"
    private String details;
    @Builder.Default
    private long timestamp = System.currentTimeMillis();
}
