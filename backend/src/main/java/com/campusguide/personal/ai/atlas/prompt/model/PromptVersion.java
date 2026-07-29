package com.campusguide.personal.ai.atlas.prompt.model;

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
 * Diagnostics-only metadata tracking internal prompt versioning, included/skipped context sections,
 * and token count estimates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptVersion {
    public static final String CURRENT_VERSION = "1.0.0";

    @Builder.Default
    private String version = CURRENT_VERSION;

    @Builder.Default
    private List<String> sectionsIncluded = new ArrayList<>();

    @Builder.Default
    private List<String> sectionsSkipped = new ArrayList<>();

    @Builder.Default
    private Map<String, Integer> tokenEstimates = new HashMap<>();

    @Builder.Default
    private Instant createdAt = Instant.now();
}
