package com.campusguide.personal.ai.atlas.knowledge.graph.metrics;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Captures reasoning observability metrics including latency, path lengths, confidence distributions, and explanation counts.
 * Strictly avoids logging raw graph contents.
 */
@Data
@Builder
public class GraphReasoningMetrics implements Serializable {

    private static final long serialVersionUID = 1L;

    private long projectionLatencyMs;
    private long reasoningLatencyMs;
    private long inferenceLatencyMs;
    private long pathFinderLatencyMs;
    private long explanationLatencyMs;

    private int nodesProjected;
    private int edgesProjected;
    private int inferencesApplied;
    private int pathsDiscovered;

    @Builder.Default
    private List<Integer> pathLengths = Collections.synchronizedList(new ArrayList<>());

    @Builder.Default
    private List<Double> confidenceScores = Collections.synchronizedList(new ArrayList<>());

    private double averageConfidence;
    private double maxConfidence;
    private double minConfidence;

    public void recordProjectionLatency(long ms) {
        this.projectionLatencyMs = ms;
    }

    public void recordReasoningLatency(long ms) {
        this.reasoningLatencyMs = ms;
    }

    public void recordInferenceLatency(long ms) {
        this.inferenceLatencyMs = ms;
    }

    public void recordPathFinderLatency(long ms) {
        this.pathFinderLatencyMs = ms;
    }

    public void recordExplanationLatency(long ms) {
        this.explanationLatencyMs = ms;
    }

    public void recordNodesProjected(int count) {
        this.nodesProjected = count;
    }

    public void recordEdgesProjected(int count) {
        this.edgesProjected = count;
    }

    public void recordInferencesApplied(int count) {
        this.inferencesApplied = count;
    }

    public void recordPath(int length, double confidence) {
        this.pathLengths.add(length);
        this.confidenceScores.add(confidence);
        recalculateConfidenceStats();
    }

    private void recalculateConfidenceStats() {
        if (confidenceScores.isEmpty()) return;
        double sum = 0.0;
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        for (double c : confidenceScores) {
            sum += c;
            if (c > max) max = c;
            if (c < min) min = c;
        }
        this.averageConfidence = sum / confidenceScores.size();
        this.maxConfidence = max;
        this.minConfidence = min;
    }
}
