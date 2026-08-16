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
                String qLower = q.toLowerCase();

                // 1. Buildings match
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

                // 2. Departments match
                for (var d : campusKnowledgeService.getDepartments()) {
                    if (qLower.contains(d.getCode().toLowerCase()) || qLower.contains(d.getName().toLowerCase())) {
                        RetrievalEvidence dEv = RetrievalEvidence.builder()
                                .type(EvidenceType.CAMPUS_KNOWLEDGE)
                                .source(EvidenceSource.KNOWLEDGE_BASE)
                                .entityKey("department:" + d.getCode())
                                .contentSnippet(String.format("Department: %s (%s). Floor/Location: %s HOD Office and Staff Room. HOD: %s. Contact Email: %s. Phone: %s.",
                                        d.getName(), d.getCode(), 
                                        d.getCode().equals("ECS") ? "Ground Floor" :
                                        d.getCode().equals("AURO") ? "1st Floor" :
                                        d.getCode().equals("AIDS") ? "2nd Floor" :
                                        d.getCode().equals("CMPN") ? "3rd Floor" :
                                        d.getCode().equals("EXTC") ? "4th Floor" : "5th Floor",
                                        d.getHeadOfDepartment(), d.getContactEmail(), d.getPhone()))
                                .rationale("Knowledge service match for department query")
                                .score(EvidenceScore.builder().relevanceScore(0.98).confidenceScore(0.95).sourceAuthorityScore(0.90).qualityScore(0.95).build())
                                .build();
                        dEv.getScore().calculateOverallScore();
                        campusEvList.add(dEv);
                    }
                }

                // 3. Classrooms, Facilities, Lifts, Washrooms match
                boolean isLiftQuery = qLower.contains("lift") || qLower.contains("lifts") || qLower.contains("stairway");
                boolean isWashroomQuery = qLower.contains("washroom") || qLower.contains("washrooms") || qLower.contains("toilet") || qLower.contains("toilets");

                if (isLiftQuery) {
                    RetrievalEvidence liftEv = RetrievalEvidence.builder()
                            .type(EvidenceType.CAMPUS_KNOWLEDGE)
                            .source(EvidenceSource.KNOWLEDGE_BASE)
                            .entityKey("campus_lifts")
                            .contentSnippet("Lifts Layout: There are four lifts in total in the building: two Front lifts and two Rear lifts. The Front Lift Section faces the Rear Lift Section across the stairway.")
                            .rationale("Authoritative lift layout query match")
                            .score(EvidenceScore.builder().relevanceScore(0.99).confidenceScore(0.99).sourceAuthorityScore(0.99).qualityScore(0.99).build())
                            .build();
                    liftEv.getScore().calculateOverallScore();
                    campusEvList.add(liftEv);
                }

                if (isWashroomQuery) {
                    RetrievalEvidence wrEv = RetrievalEvidence.builder()
                            .type(EvidenceType.CAMPUS_KNOWLEDGE)
                            .source(EvidenceSource.KNOWLEDGE_BASE)
                            .entityKey("campus_washrooms")
                            .contentSnippet("Washrooms Layout: There are four washrooms per floor: two male and two female washrooms, positioned near the lift sections (Front Lift and Rear Lift).")
                            .rationale("Authoritative washrooms layout query match")
                            .score(EvidenceScore.builder().relevanceScore(0.99).confidenceScore(0.99).sourceAuthorityScore(0.99).qualityScore(0.99).build())
                            .build();
                    wrEv.getScore().calculateOverallScore();
                    campusEvList.add(wrEv);
                }

                for (var c : campusKnowledgeService.getClassrooms()) {
                    String roomLower = c.getRoomNumber().toLowerCase();
                    
                    if ((isLiftQuery && roomLower.contains("lift")) || 
                        (isWashroomQuery && (roomLower.contains("washroom") || roomLower.contains("toilet")))) {
                        continue;
                    }

                    boolean match = qLower.contains(roomLower);
                    if (!match) {
                        if (qLower.contains("library") && roomLower.contains("library")) match = true;
                        else if (qLower.contains("principal") && roomLower.contains("principal")) match = true;
                        else if (qLower.contains("amphitheatre") && roomLower.contains("amphi")) match = true;
                        else if (qLower.contains("workshop") && (roomLower.contains("workshop") || roomLower.contains("woodwork") || roomLower.contains("metalwork"))) match = true;
                        else if (qLower.contains("canteen") && roomLower.contains("canteen")) match = true;
                        else if (qLower.contains("common room") && roomLower.contains("common room")) match = true;
                    }
                    if (match) {
                        RetrievalEvidence cEv = RetrievalEvidence.builder()
                                .type(EvidenceType.CAMPUS_KNOWLEDGE)
                                .source(EvidenceSource.KNOWLEDGE_BASE)
                                .entityKey("classroom:" + c.getClassroomId())
                                .contentSnippet(String.format("Facility/Room: %s. Building: %s. Features: %s.",
                                        c.getRoomNumber(), c.getBuildingId(), String.join(", ", c.getFeatures())))
                                .rationale("Knowledge service match for facility/classroom query")
                                .score(EvidenceScore.builder().relevanceScore(0.98).confidenceScore(0.95).sourceAuthorityScore(0.90).qualityScore(0.95).build())
                                .build();
                        cEv.getScore().calculateOverallScore();
                        campusEvList.add(cEv);
                    }
                }

                // 4. Batches query match
                boolean isBatchQuery = qLower.contains("batch") || qLower.contains("batches") ||
                        qLower.contains("fe") || qLower.contains("se") || qLower.contains("te") || qLower.contains("be") ||
                        qLower.matches(".*\\bd\\d{1,2}[a-z]*\\b.*");
                if (isBatchQuery) {
                    RetrievalEvidence bEv = RetrievalEvidence.builder()
                            .type(EvidenceType.CAMPUS_KNOWLEDGE)
                            .source(EvidenceSource.KNOWLEDGE_BASE)
                            .entityKey("academic_batches")
                            .contentSnippet("Authoritative VESIT academic batch structure:\n" +
                                    "- ECS: D1EC (FE), D6EC (SE), D11EC (TE), D16EC (BE)\n" +
                                    "- AIDS: D1ADA, D1ADB (FE), D6ADA, D6ADB (SE), D11ADA, D11ADB (TE), D16ADA, D16ADB (BE)\n" +
                                    "- CMPN: D2A, D2B, D2C (FE), D7A, D7B, D7C (SE), D12A, D12B, D12C (TE), D17A, D17B, D17C (BE)\n" +
                                    "- AURO: D3 (FE), D8 (SE), D13 (TE), D18 (BE)\n" +
                                    "- EXTC: D4A, D4B (FE), D9A, D9B (SE), D14A, D14B (TE), D19A, D19B (BE)\n" +
                                    "- IT: D5A, D5B, D5C (FE), D10A, D10B, D10C (SE), D15A, D15B, D15C (TE), D20A, D20B, D20C (BE)")
                            .rationale("Authoritative academic batch listing query match")
                            .score(EvidenceScore.builder().relevanceScore(0.99).confidenceScore(0.99).sourceAuthorityScore(0.99).qualityScore(0.99).build())
                            .build();
                    bEv.getScore().calculateOverallScore();
                    campusEvList.add(bEv);
                }

                // 5. Councils query match
                boolean isCouncilQuery = qLower.contains("council") || qLower.contains("councils") ||
                        qLower.contains("veslang") || qLower.contains("veslit") || qLower.contains("sort") ||
                        qLower.contains("sports") || qLower.contains("ieee") || qLower.contains("iste") ||
                        qLower.contains("isa") || qLower.contains("csi");
                if (isCouncilQuery) {
                    RetrievalEvidence cEv = RetrievalEvidence.builder()
                            .type(EvidenceType.CAMPUS_KNOWLEDGE)
                            .source(EvidenceSource.KNOWLEDGE_BASE)
                            .entityKey("campus_councils")
                            .contentSnippet("Authoritative list of VESIT Councils:\n" +
                                    "- VESLANG: VES's Language Council, promoting debate and public speaking.\n" +
                                    "- VESLIT: VES's Literature Council, celebrating writing, poetry, and literature.\n" +
                                    "- SORT: Social Outreach and Reflexive Tribulations (SORT) - Donation drives and social work.\n" +
                                    "- CC: Cultural Council (CC), organizing music, dance, and drama events.\n" +
                                    "- Sports: Sports Council, managing athletic events and tournaments.\n" +
                                    "- IEEE: Technical Council, IEEE Student Branch VESIT facilitating technical growth.\n" +
                                    "- ISTE: Technical Council, Indian Society for Technical Education (ISTE) VESIT Chapter.\n" +
                                    "- ISA: Technical Council, International Society of Automation (ISA) VESIT Chapter.\n" +
                                    "- CSI: Technical Council, Computer Society of India (CSI) VESIT Chapter.")
                            .rationale("Authoritative council listing query match")
                            .score(EvidenceScore.builder().relevanceScore(0.99).confidenceScore(0.99).sourceAuthorityScore(0.99).qualityScore(0.99).build())
                            .build();
                    cEv.getScore().calculateOverallScore();
                    campusEvList.add(cEv);
                }

                // 6. Communities query match
                boolean isCommunityQuery = qLower.contains("communit") || qLower.contains("club") || qLower.contains("ai") ||
                        qLower.contains("gdg") || qLower.contains("cybersecurity") || qLower.contains("web development") ||
                        qLower.contains("photography");
                if (isCommunityQuery) {
                    RetrievalEvidence coEv = RetrievalEvidence.builder()
                            .type(EvidenceType.CAMPUS_KNOWLEDGE)
                            .source(EvidenceSource.KNOWLEDGE_BASE)
                            .entityKey("campus_communities")
                            .contentSnippet("Authoritative list of VESIT Communities:\n" +
                                    "- Google Developer Groups (GDG): Google Developer Groups VESIT (AI and tech club).\n" +
                                    "- AI & ML Club: Artificial Intelligence & Machine Learning Club.\n" +
                                    "- Cybersecurity Club: Cybersecurity and Ethical Hacking Club (IEEE).\n" +
                                    "- Web Development Club: Modern Web Development and Design Club.\n" +
                                    "- Photography Club: Capturing moments and creative expression (CC).")
                            .rationale("Authoritative community listing query match")
                            .score(EvidenceScore.builder().relevanceScore(0.99).confidenceScore(0.99).sourceAuthorityScore(0.99).qualityScore(0.99).build())
                            .build();
                    coEv.getScore().calculateOverallScore();
                    campusEvList.add(coEv);
                }
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
