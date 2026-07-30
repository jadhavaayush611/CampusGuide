package com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.engine;

import com.campusguide.personal.ai.atlas.knowledge.graph.confidence.ConfidenceCalculator;
import com.campusguide.personal.ai.atlas.knowledge.graph.confidence.ConfidenceFactors;
import com.campusguide.personal.ai.atlas.knowledge.graph.confidence.ReasoningConfidence;
import com.campusguide.personal.ai.atlas.knowledge.graph.explainability.ExplanationEngine;
import com.campusguide.personal.ai.atlas.knowledge.graph.explainability.ReasoningExplanation;
import com.campusguide.personal.ai.atlas.knowledge.graph.inference.InferenceEngine;
import com.campusguide.personal.ai.atlas.knowledge.graph.inference.InferenceResult;
import com.campusguide.personal.ai.atlas.knowledge.graph.metrics.GraphReasoningMetrics;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.KnowledgeNode;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import com.campusguide.personal.ai.atlas.knowledge.graph.path.EvidencePath;
import com.campusguide.personal.ai.atlas.knowledge.graph.path.ReasoningChain;
import com.campusguide.personal.ai.atlas.knowledge.graph.path.ReasoningPathFinder;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.context.GraphContext;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.context.ReasoningObjective;
import com.campusguide.personal.ai.atlas.knowledge.graph.view.KnowledgeGraphView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Pipeline orchestrator executing graph view validation, rule inference, path discovery, confidence calculation, and explanation generation.
 */
@Component
@Slf4j
public class ReasoningPipeline {

    private final InferenceEngine inferenceEngine;
    private final ReasoningPathFinder pathFinder;
    private final ConfidenceCalculator confidenceCalculator;
    private final ExplanationEngine explanationEngine;

    @Autowired
    public ReasoningPipeline(InferenceEngine inferenceEngine,
                              ReasoningPathFinder pathFinder,
                              ConfidenceCalculator confidenceCalculator,
                              ExplanationEngine explanationEngine) {
        this.inferenceEngine = inferenceEngine != null ? inferenceEngine : new InferenceEngine(null);
        this.pathFinder = pathFinder != null ? pathFinder : new ReasoningPathFinder();
        this.confidenceCalculator = confidenceCalculator != null ? confidenceCalculator : new ConfidenceCalculator();
        this.explanationEngine = explanationEngine != null ? explanationEngine : new ExplanationEngine();
    }

    public ReasoningEvidence execute(GraphContext graphContext, GraphReasoningMetrics metrics) {
        long startTime = System.nanoTime();

        if (graphContext == null || graphContext.getGraphView() == null) {
            log.warn("Null GraphContext or KnowledgeGraphView provided to ReasoningPipeline");
            return ReasoningEvidence.empty();
        }

        // 1. Operate exclusively on KnowledgeGraphView
        KnowledgeGraphView view = graphContext.getGraphView();

        // 2. Rule Inference (non-mutating)
        InferenceResult inferenceResult = inferenceEngine.infer(view, metrics);

        // 3. Path Discovery
        long pathStart = System.nanoTime();
        List<EvidencePath> discoveredPaths = discoverPathsForObjective(graphContext, view, inferenceResult);
        long pathLatencyMs = (System.nanoTime() - pathStart) / 1_000_000;
        if (metrics != null) {
            metrics.recordPathFinderLatency(pathLatencyMs);
        }

        EvidencePath primaryPath = !discoveredPaths.isEmpty() ? discoveredPaths.get(0) : null;

        // Record metrics for discovered paths
        if (metrics != null && primaryPath != null) {
            metrics.recordPath(primaryPath.getHopCount(), primaryPath.getConfidence());
        }

        // 4. Reasoning Chain Construction
        ReasoningChain chain = buildReasoningChain(graphContext, primaryPath, discoveredPaths);

        // 5. Deterministic Confidence Calculation
        ConfidenceFactors factors = buildConfidenceFactors(primaryPath, inferenceResult);
        ReasoningConfidence confidence = confidenceCalculator.calculate(factors);

        // 6. Explanation Generation
        ReasoningExplanation explanation = explanationEngine.explain(view, chain, primaryPath, inferenceResult, confidence, metrics);

        // 7. Reasoning Summary Text & Citation Extraction
        List<String> nodeNames = primaryPath != null ? primaryPath.getNodes().stream().map(KnowledgeNode::getName).filter(Objects::nonNull).collect(Collectors.toList()) : Collections.emptyList();
        List<String> relTypes = primaryPath != null ? primaryPath.getEdges().stream().map(e -> e.getRelationshipType().name()).collect(Collectors.toList()) : Collections.emptyList();

        String summaryText = generateSummaryText(graphContext.getObjective(), primaryPath, inferenceResult, confidence);

        long pipelineLatencyMs = (System.nanoTime() - startTime) / 1_000_000;
        if (metrics != null) {
            metrics.recordReasoningLatency(pipelineLatencyMs);
        }

        log.debug("ReasoningPipeline execution complete contextId={} viewId={} confidence=%.2f latencyMs={}",
                graphContext.getContextId(), view.getViewId(), confidence.getOverallScore(), pipelineLatencyMs);

        return ReasoningEvidence.builder()
                .evidenceId("rev_" + UUID.randomUUID().toString().substring(0, 8))
                .objectiveDescription(graphContext.getObjective().getDescription())
                .confidence(confidence.getOverallScore())
                .reasoningSummaryText(summaryText)
                .explanation(explanation)
                .citedNodeNames(nodeNames)
                .citedRelationshipTypes(relTypes)
                .build();
    }

    private List<EvidencePath> discoverPathsForObjective(GraphContext ctx, KnowledgeGraphView view, InferenceResult inference) {
        Set<NodeIdentifier> roots = ctx.getRootNodes();
        ReasoningObjective obj = ctx.getObjective();
        Set<NodeIdentifier> targets = obj.getTargetNodes();

        List<EvidencePath> allPaths = new ArrayList<>();

        if (roots != null && !roots.isEmpty() && targets != null && !targets.isEmpty()) {
            for (NodeIdentifier r : roots) {
                for (NodeIdentifier t : targets) {
                    if (r.equals(t)) continue;
                    List<EvidencePath> p = pathFinder.findPaths(
                            view,
                            inference.getInferredEdges(),
                            r,
                            t,
                            ReasoningPathFinder.PathStrategy.HIGHEST_CONFIDENCE,
                            ctx.getMaxReasoningDepth(),
                            ctx.getActiveCollections()
                    );
                    allPaths.addAll(p);
                }
            }
        } else if (roots != null && roots.size() >= 2) {
            List<NodeIdentifier> rootList = new ArrayList<>(roots);
            for (int i = 0; i < rootList.size() - 1; i++) {
                List<EvidencePath> p = pathFinder.findPaths(
                        view,
                        inference.getInferredEdges(),
                        rootList.get(i),
                        rootList.get(i + 1),
                        ReasoningPathFinder.PathStrategy.SHORTEST,
                        ctx.getMaxReasoningDepth(),
                        ctx.getActiveCollections()
                );
                allPaths.addAll(p);
            }
        }
        allPaths.sort((a, b) -> Double.compare(b.getConfidence(), a.getConfidence()));
        return allPaths;
    }

    private ReasoningChain buildReasoningChain(GraphContext ctx, EvidencePath primaryPath, List<EvidencePath> allPaths) {
        if (primaryPath == null) {
            return ReasoningChain.empty();
        }

        List<String> logicalSteps = new ArrayList<>();
        List<KnowledgeNode> nodes = primaryPath.getNodes();
        for (int i = 0; i < primaryPath.getEdges().size(); i++) {
            String src = i < nodes.size() ? nodes.get(i).getName() : "Node";
            String tgt = (i + 1) < nodes.size() ? nodes.get(i + 1).getName() : "Node";
            String rel = primaryPath.getEdges().get(i).getRelationshipType().name();
            logicalSteps.add(String.format("Step %d: %s is linked to %s via [%s]", i + 1, src, tgt, rel));
        }

        return ReasoningChain.builder()
                .chainId("chain_" + UUID.randomUUID().toString().substring(0, 8))
                .evidencePaths(allPaths)
                .logicalSteps(logicalSteps)
                .overallChainConfidence(primaryPath.getConfidence())
                .rationale("Reasoning path constructed via " + primaryPath.getPathType() + " traversal")
                .build();
    }

    private ConfidenceFactors buildConfidenceFactors(EvidencePath primaryPath, InferenceResult inference) {
        if (primaryPath == null) {
            return ConfidenceFactors.defaults();
        }
        double relStrength = primaryPath.getCumulativeScore();
        double conf = primaryPath.getConfidence();
        int hops = primaryPath.getHopCount();
        double infConf = (inference != null && !inference.getInferredEdges().isEmpty()) ? 0.85 : 0.95;

        return ConfidenceFactors.builder()
                .relationshipStrength(relStrength)
                .retrievalConfidence(conf)
                .evidenceQuality(0.90)
                .traversalDepth(hops > 0 ? hops : 1)
                .inferenceConfidence(infConf)
                .build();
    }

    private String generateSummaryText(ReasoningObjective obj, EvidencePath path, InferenceResult inference, ReasoningConfidence conf) {
        if (path == null || path.getNodes().isEmpty()) {
            return String.format("Graph reasoning evaluated objective '%s'. No connecting evidence paths discovered.", obj.getDescription());
        }

        String pathStr = path.getNodes().stream().map(KnowledgeNode::getName).filter(Objects::nonNull).collect(Collectors.joining(" -> "));
        int inferredCount = inference != null ? inference.getInferredEdges().size() : 0;

        return String.format("Graph reasoning discovered path: [%s] across %d hops with %.2f confidence (%s). Applied %d inferred relationships.",
                pathStr, path.getHopCount(), conf.getOverallScore(), conf.getLevel().name(), inferredCount);
    }
}
