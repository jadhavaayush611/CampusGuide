package com.campusguide.personal.ai.atlas.knowledge.graph.explainability;

import com.campusguide.personal.ai.atlas.knowledge.graph.confidence.ReasoningConfidence;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;
import com.campusguide.personal.ai.atlas.knowledge.graph.inference.InferenceResult;
import com.campusguide.personal.ai.atlas.knowledge.graph.metrics.GraphReasoningMetrics;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.KnowledgeNode;
import com.campusguide.personal.ai.atlas.knowledge.graph.path.EvidencePath;
import com.campusguide.personal.ai.atlas.knowledge.graph.path.ReasoningChain;
import com.campusguide.personal.ai.atlas.knowledge.graph.view.KnowledgeGraphView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Engine responsible for generating structured, human and LLM readable reasoning explanations.
 */
@Component
@Slf4j
public class ExplanationEngine {

    public ReasoningExplanation explain(KnowledgeGraphView view,
                                        ReasoningChain chain,
                                        EvidencePath primaryPath,
                                        InferenceResult inferenceResult,
                                        ReasoningConfidence confidence,
                                        GraphReasoningMetrics metrics) {
        long startTime = System.nanoTime();

        List<ExplanationStep> steps = new ArrayList<>();
        List<String> citedEdges = new ArrayList<>();
        List<String> citedArtifacts = new ArrayList<>();
        List<String> assumptions = new ArrayList<>();

        if (primaryPath != null && primaryPath.getEdges() != null) {
            List<KnowledgeEdge> edges = primaryPath.getEdges();
            List<KnowledgeNode> nodes = primaryPath.getNodes();

            for (int i = 0; i < edges.size(); i++) {
                KnowledgeEdge e = edges.get(i);
                KnowledgeNode src = i < nodes.size() ? nodes.get(i) : null;
                KnowledgeNode tgt = (i + 1) < nodes.size() ? nodes.get(i + 1) : null;

                citedEdges.add(e.getId());

                if (src != null && src.getSourceArtifactId() != null && !citedArtifacts.contains(src.getSourceArtifactId())) {
                    citedArtifacts.add(src.getSourceArtifactId());
                }
                if (tgt != null && tgt.getSourceArtifactId() != null && !citedArtifacts.contains(tgt.getSourceArtifactId())) {
                    citedArtifacts.add(tgt.getSourceArtifactId());
                }

                String srcName = src != null ? src.getName() : e.getSourceNodeId().getValue();
                String tgtName = tgt != null ? tgt.getName() : e.getTargetNodeId().getValue();
                double stepConf = 0.8;
                if (e.getMetadata() != null && e.getMetadata().getProperty("confidenceScore") instanceof Number n) {
                    stepConf = n.doubleValue();
                }

                ExplanationStep step = ExplanationStep.builder()
                        .stepIndex(i + 1)
                        .sourceNodeId(e.getSourceNodeId().getValue())
                        .sourceNodeName(srcName)
                        .targetNodeId(e.getTargetNodeId().getValue())
                        .targetNodeName(tgtName)
                        .relationshipType(e.getRelationshipType().name())
                        .stepConfidence(stepConf)
                        .explanationText(String.format("Traversed [%s] via (%s) to [%s]", srcName, e.getRelationshipType().name(), tgtName))
                        .build();

                steps.add(step);
            }
        }

        if (inferenceResult != null && !inferenceResult.getInferredEdges().isEmpty()) {
            assumptions.add(String.format("Inferred %d implicit virtual relationships using rule-based reasoning", inferenceResult.getInferredEdges().size()));
            for (KnowledgeEdge ie : inferenceResult.getInferredEdges()) {
                citedEdges.add(ie.getId());
            }
        }

        if (confidence != null && confidence.getLevel() == ReasoningConfidence.ConfidenceLevel.LOW) {
            assumptions.add("Reasoning path relies on lower-confidence edge transitions");
        }

        int evaluatedNodes = view != null ? view.getNodeCount() : (primaryPath != null ? primaryPath.getNodes().size() : 0);
        int traversedEdges = primaryPath != null ? primaryPath.getHopCount() : 0;
        int inferencesApplied = inferenceResult != null ? inferenceResult.getInferredEdges().size() : 0;
        double overallConf = confidence != null ? confidence.getOverallScore() : 0.8;

        String summaryText = String.format("Evaluated %d nodes and %d hops with overall confidence %.2f (%s)",
                evaluatedNodes, traversedEdges, overallConf, confidence != null ? confidence.getLevel().name() : "HIGH");

        EvidenceSummary summary = EvidenceSummary.builder()
                .totalNodesEvaluated(evaluatedNodes)
                .totalEdgesTraversed(traversedEdges)
                .totalInferencesApplied(inferencesApplied)
                .overallConfidence(overallConf)
                .textualSummary(summaryText)
                .build();

        long latencyMs = (System.nanoTime() - startTime) / 1_000_000;
        if (metrics != null) {
            metrics.recordExplanationLatency(latencyMs);
        }

        return ReasoningExplanation.builder()
                .explanationId("exp_" + UUID.randomUUID().toString().substring(0, 8))
                .reasoningChain(chain != null ? chain : ReasoningChain.empty())
                .primaryEvidencePath(primaryPath)
                .confidence(confidence)
                .assumptions(assumptions)
                .citedArtifacts(citedArtifacts)
                .citedGraphEdges(citedEdges)
                .steps(steps)
                .summary(summary)
                .build();
    }
}
