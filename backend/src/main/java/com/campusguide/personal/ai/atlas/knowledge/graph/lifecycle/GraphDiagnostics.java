package com.campusguide.personal.ai.atlas.knowledge.graph.lifecycle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Diagnostic tracking information for Knowledge Graph construction, updates, and health.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphDiagnostics implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private long buildDurationMs = 0L;

    @Builder.Default
    private int nodesCreated = 0;

    @Builder.Default
    private int nodesMerged = 0;

    @Builder.Default
    private int edgesCreated = 0;

    @Builder.Default
    private int edgesDeduplicated = 0;

    @Builder.Default
    private int danglingEdgesRemoved = 0;

    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    @Builder.Default
    private List<String> errors = new ArrayList<>();

    @Builder.Default
    private Map<String, Object> details = new HashMap<>();

    @Builder.Default
    private Instant lastDiagnosticsTime = Instant.now();

    public void addWarning(String warning) {
        if (this.warnings == null) {
            this.warnings = new ArrayList<>();
        }
        this.warnings.add(warning);
    }

    public void addError(String error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
    }

    public void addDetail(String key, Object value) {
        if (this.details == null) {
            this.details = new HashMap<>();
        }
        this.details.put(key, value);
    }
}
