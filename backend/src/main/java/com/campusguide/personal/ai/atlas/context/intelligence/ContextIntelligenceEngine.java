package com.campusguide.personal.ai.atlas.context.intelligence;

import com.campusguide.personal.ai.atlas.context.AtlasContext;
import com.campusguide.personal.ai.atlas.context.evidence.EvidenceBundle;
import com.campusguide.personal.ai.atlas.context.evidence.EvidenceScore;
import com.campusguide.personal.ai.atlas.context.evidence.EvidenceSource;
import com.campusguide.personal.ai.atlas.context.evidence.EvidenceType;
import com.campusguide.personal.ai.atlas.context.evidence.RetrievalEvidence;
import com.campusguide.personal.ai.atlas.context.fusion.ContextFusionEngine;
import com.campusguide.personal.ai.atlas.context.fusion.ContextFusionEngine.ContextFusionResult;
import com.campusguide.personal.ai.atlas.context.metrics.ContextMetrics;
import com.campusguide.personal.ai.atlas.context.optimization.ContextCache;
import com.campusguide.personal.ai.atlas.context.optimization.ContextCache.CacheDiagnostics;
import com.campusguide.personal.ai.atlas.context.optimization.LatencyBudgetManager;
import com.campusguide.personal.ai.atlas.context.optimization.LatencyBudgetManager.LatencyBudgetSummary;
import com.campusguide.personal.ai.atlas.context.prioritization.ContextPrioritizer;
import com.campusguide.personal.ai.atlas.context.prioritization.PrioritizationDecision;
import com.campusguide.personal.ai.atlas.context.query.QueryContext;
import com.campusguide.personal.ai.atlas.context.retrieval.RetrievalContext;
import com.campusguide.personal.ai.atlas.context.service.knowledge.CampusKnowledgeService;
import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Orchestrator of Atlas Context Intelligence Layer.
 * Coordinates evidence analysis, context fusion, conflict resolution, prioritization, and optimization
 * before AtlasContext is finalized.
 */
@Component
@Getter
@Slf4j
public class ContextIntelligenceEngine {

    private final ContextFusionEngine fusionEngine;
    private final ContextPrioritizer prioritizer;
    private final ContextCache contextCache;
    private final LatencyBudgetManager latencyBudgetManager;
    private final CampusKnowledgeService campusKnowledgeService;

    @Autowired
    public ContextIntelligenceEngine(@Autowired(required = false) ContextFusionEngine fusionEngine,
                                      @Autowired(required = false) ContextPrioritizer prioritizer,
                                      @Autowired(required = false) ContextCache contextCache,
                                      @Autowired(required = false) LatencyBudgetManager latencyBudgetManager,
                                      @Autowired(required = false) CampusKnowledgeService campusKnowledgeService) {
        this.fusionEngine = fusionEngine != null ? fusionEngine : new ContextFusionEngine();
        this.prioritizer = prioritizer != null ? prioritizer : new ContextPrioritizer();
        this.contextCache = contextCache != null ? contextCache : new ContextCache();
        this.latencyBudgetManager = latencyBudgetManager != null ? latencyBudgetManager : new LatencyBudgetManager();
        this.campusKnowledgeService = campusKnowledgeService != null ? campusKnowledgeService : new CampusKnowledgeService(null);
    }

    public ContextIntelligenceEngine() {
        this.fusionEngine = new ContextFusionEngine();
        this.prioritizer = new ContextPrioritizer();
        this.contextCache = new ContextCache();
        this.latencyBudgetManager = new LatencyBudgetManager();
        this.campusKnowledgeService = new CampusKnowledgeService(null);
    }

    /**
     * Executes the complete Context Intelligence lifecycle on an AtlasContext instance.
     */
    public void process(AtlasChatRequest request, QueryContext queryContext, RetrievalContext retrievalContext, AtlasContext context) {
        long startTimeMs = System.currentTimeMillis();

        if (context == null) return;

        // 1. Evidence Analysis & Bundle Harvesting
        List<EvidenceBundle> rawBundles = analyzeAndHarvestEvidence(request, queryContext, context);

        // Check Cache for existing domain bundles if available
        if (queryContext != null && queryContext.getNormalizedQuery() != null) {
            String cacheKey = "query:" + queryContext.getNormalizedQuery().hashCode();
            Optional<EvidenceBundle> cached = contextCache.get(cacheKey);
            cached.ifPresent(rawBundles::add);
        }

        // 2. Context Fusion & Conflict Resolution
        ContextFusionResult fusionResult = fusionEngine.fuse(context, rawBundles);

        // 3. Context Prioritization
        List<PrioritizationDecision> prioritizationDecisions = prioritizer.prioritize(context, queryContext, 10);

        // Cache fused domain bundles for future lookup
        fusionResult.fusedBundles().forEach((domain, bundle) -> {
            if (queryContext != null && queryContext.getNormalizedQuery() != null) {
                contextCache.put("domain:" + domain + ":" + queryContext.getNormalizedQuery().hashCode(), bundle);
            }
        });

        // 4. Latency Budget & Optimization Check
        long latencyMs = retrievalContext != null ? retrievalContext.getTotalLatencyMs() : (System.currentTimeMillis() - startTimeMs);
        boolean budgetExceeded = !latencyBudgetManager.hasRemainingBudget(startTimeMs, 500L);
        LatencyBudgetSummary budgetSummary = latencyBudgetManager.createSummary(startTimeMs, 500L, budgetExceeded);

        // 5. Observability Data Capture
        CacheDiagnostics cacheDiag = contextCache.getDiagnostics();
        Map<String, String> evidenceSummaries = new LinkedHashMap<>();
        context.getEvidenceBundles().forEach((domain, bundle) -> {
            evidenceSummaries.put(domain, bundle.getSourceSummary());
        });

        IntelligenceMetrics intelMetrics = IntelligenceMetrics.builder()
                .fusionDecisions(fusionResult.fusionDecisions())
                .conflictResolutions(fusionResult.conflictResolutions())
                .prioritizationDecisions(prioritizationDecisions)
                .evidenceSummaries(evidenceSummaries)
                .cacheHits(cacheDiag.hits())
                .cacheMisses(cacheDiag.misses())
                .cacheHitRatio(cacheDiag.hitRatio())
                .allocatedLatencyBudgetMs(budgetSummary.allocatedBudgetMs())
                .usedLatencyBudgetMs(budgetSummary.usedBudgetMs())
                .budgetExceeded(budgetExceeded)
                .degradedMode(budgetSummary.degradedModeActive())
                .build();

        context.setIntelligenceMetrics(intelMetrics);

        // Sync into ContextMetrics for unified diagnostics
        ContextMetrics cm = context.getMetrics();
        if (cm != null) {
            cm.setFusionDecisions(fusionResult.fusionDecisions());
            cm.setConflictResolutions(fusionResult.conflictResolutions());
            cm.setPrioritizationDecisions(prioritizationDecisions);
            cm.setEvidenceSummaries(evidenceSummaries);
            cm.setCacheHits(cacheDiag.hits());
            cm.setCacheMisses(cacheDiag.misses());
            cm.setCacheHitRatio(cacheDiag.hitRatio());
            cm.setAllocatedLatencyBudgetMs(budgetSummary.allocatedBudgetMs());
            cm.setUsedLatencyBudgetMs(budgetSummary.usedBudgetMs());
            cm.setBudgetExceeded(budgetExceeded);
        }
    }

    private List<EvidenceBundle> analyzeAndHarvestEvidence(AtlasChatRequest request, QueryContext queryContext, AtlasContext context) {
        List<EvidenceBundle> harvested = new ArrayList<>();

        // Harvest from UserContext
        if (context.getUserContext() != null) {
            RetrievalEvidence ev = RetrievalEvidence.builder()
                    .type(EvidenceType.DOMAIN_SERVICE)
                    .source(EvidenceSource.DATABASE)
                    .entityKey("userProfile")
                    .contentSnippet(context.getUserContext().getSummary() != null ? context.getUserContext().getSummary() : context.getUserContext().getName())
                    .rationale("User profile details retrieved for personalized query response")
                    .score(EvidenceScore.builder().relevanceScore(0.95).confidenceScore(0.95).sourceAuthorityScore(0.95).qualityScore(0.90).build())
                    .build();
            ev.getScore().calculateOverallScore();
            harvested.add(EvidenceBundle.builder()
                    .targetDomain("userProfile")
                    .evidences(List.of(ev))
                    .confidence(0.95)
                    .sourceSummary("Harvested user profile context")
                    .build());
        }

        // Harvest from AcademicContext
        if (context.getAcademicContext() != null) {
            RetrievalEvidence ev = RetrievalEvidence.builder()
                    .type(EvidenceType.DOMAIN_SERVICE)
                    .source(EvidenceSource.ACADEMIC_SERVICE)
                    .entityKey("academic")
                    .contentSnippet(context.getAcademicContext().getSummary() != null ? context.getAcademicContext().getSummary() : context.getAcademicContext().getDepartment())
                    .rationale("Academic department and program context retrieved")
                    .score(EvidenceScore.builder().relevanceScore(0.90).confidenceScore(0.90).sourceAuthorityScore(0.85).qualityScore(0.85).build())
                    .build();
            ev.getScore().calculateOverallScore();
            harvested.add(EvidenceBundle.builder()
                    .targetDomain("academic")
                    .evidences(List.of(ev))
                    .confidence(0.90)
                    .sourceSummary("Harvested academic context")
                    .build());
        }

        // Harvest from CampusContext & CampusKnowledgeService
        if (context.getCampusContext() != null) {
            RetrievalEvidence ev = RetrievalEvidence.builder()
                    .type(EvidenceType.CAMPUS_KNOWLEDGE)
                    .source(EvidenceSource.CAMPUS_SERVICE)
                    .entityKey("campus")
                    .contentSnippet(context.getCampusContext().getSummary() != null ? context.getCampusContext().getSummary() : context.getCampusContext().getLocation())
                    .rationale("Campus location and active notices context retrieved")
                    .score(EvidenceScore.builder().relevanceScore(0.85).confidenceScore(0.90).sourceAuthorityScore(0.90).qualityScore(0.85).build())
                    .build();
            ev.getScore().calculateOverallScore();

            List<RetrievalEvidence> campusEvList = new ArrayList<>();
            campusEvList.add(ev);

            // Enrich with CampusKnowledgeService if prompt contains matching entities
            if (queryContext != null && queryContext.getRawQuery() != null) {
                String q = queryContext.getRawQuery();
                campusKnowledgeService.getBuilding(q).ifPresent(b -> {
                    RetrievalEvidence bEv = RetrievalEvidence.builder()
                            .type(EvidenceType.CAMPUS_KNOWLEDGE)
                            .source(EvidenceSource.KNOWLEDGE_BASE)
                            .entityKey("building:" + b.getCode())
                            .contentSnippet(String.format("Building %s (%s): %s. Hours: %s", b.getName(), b.getCode(), b.getAddress(), b.getOperatingHours()))
                            .rationale("Knowledge service match for building query")
                            .score(EvidenceScore.builder().relevanceScore(0.98).confidenceScore(0.95).sourceAuthorityScore(0.90).qualityScore(0.95).build())
                            .build();
                    bEv.getScore().calculateOverallScore();
                    campusEvList.add(bEv);
                });
            }

            EvidenceBundle campusBundle = EvidenceBundle.builder()
                    .targetDomain("campus")
                    .evidences(campusEvList)
                    .confidence(0.88)
                    .sourceSummary("Harvested campus knowledge context")
                    .build();
            campusBundle.recalculateAggregateScore();
            harvested.add(campusBundle);
        }

        // Harvest existing EvidenceBundles already added to context
        harvested.addAll(context.getEvidenceBundles().values());

        return harvested;
    }
}
