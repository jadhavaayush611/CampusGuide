package com.campusguide.personal.ai.atlas.knowledge.graph.inference;

import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;
import com.campusguide.personal.ai.atlas.knowledge.graph.metrics.GraphReasoningMetrics;
import com.campusguide.personal.ai.atlas.knowledge.graph.view.KnowledgeGraphView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Inference Engine that evaluates declarative rules over KnowledgeGraphView without mutating underlying graphs.
 */
@Component
@Slf4j
public class InferenceEngine {

    private final InferenceRegistry registry;

    @Autowired
    public InferenceEngine(InferenceRegistry registry) {
        this.registry = registry != null ? registry : new InferenceRegistry();
    }

    public InferenceResult infer(KnowledgeGraphView view) {
        return infer(view, null);
    }

    public InferenceResult infer(KnowledgeGraphView view, GraphReasoningMetrics metrics) {
        long startTime = System.nanoTime();
        if (view == null) {
            return InferenceResult.empty();
        }

        List<KnowledgeEdge> allInferredEdges = new ArrayList<>();
        int appliedCount = 0;

        for (InferenceRule rule : registry.getRules()) {
            try {
                List<KnowledgeEdge> ruleEdges = rule.evaluate(view);
                if (ruleEdges != null && !ruleEdges.isEmpty()) {
                    allInferredEdges.addAll(ruleEdges);
                    appliedCount++;
                }
            } catch (Exception e) {
                log.warn("Failed executing inference ruleId={}: {}", rule.getRuleId(), e.getMessage());
            }
        }

        long latencyNs = System.nanoTime() - startTime;
        long latencyMs = latencyNs / 1_000_000;

        if (metrics != null) {
            metrics.recordInferenceLatency(latencyMs);
            metrics.recordInferencesApplied(allInferredEdges.size());
        }

        log.debug("Graph inference completed viewId={} inferredEdgesCount={} rulesApplied={} latencyMs={}",
                view.getViewId(), allInferredEdges.size(), appliedCount, latencyMs);

        return InferenceResult.builder()
                .inferredEdges(allInferredEdges)
                .rulesAppliedCount(appliedCount)
                .executionTimeMs(latencyMs)
                .engineId("graph_inference_engine")
                .build();
    }
}
