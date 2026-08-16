package com.campusguide.personal.ai.atlas.context;

import com.campusguide.personal.ai.atlas.context.metrics.ContextMetrics;
import com.campusguide.personal.ai.atlas.context.query.EntityExtractor;
import com.campusguide.personal.ai.atlas.context.query.ExtractedEntity;
import com.campusguide.personal.ai.atlas.context.query.IntentDetector;
import com.campusguide.personal.ai.atlas.context.query.QueryAnalyzer;
import com.campusguide.personal.ai.atlas.context.query.QueryContext;
import com.campusguide.personal.ai.atlas.context.query.QueryNormalizer;
import com.campusguide.personal.ai.atlas.context.query.TemporalExpressionResolver;
import com.campusguide.personal.ai.atlas.context.ranking.ContextRankingService;
import com.campusguide.personal.ai.atlas.context.ranking.ContextScore;
import com.campusguide.personal.ai.atlas.context.ranking.RelevanceScorer;
import com.campusguide.personal.ai.atlas.context.retrieval.AcademicRetrievalStrategy;
import com.campusguide.personal.ai.atlas.context.retrieval.CalendarRetrievalStrategy;
import com.campusguide.personal.ai.atlas.context.retrieval.CampusRetrievalStrategy;
import com.campusguide.personal.ai.atlas.context.retrieval.ContextRetriever;
import com.campusguide.personal.ai.atlas.context.retrieval.PlannerRetrievalStrategy;
import com.campusguide.personal.ai.atlas.context.retrieval.RetrievalContext;
import com.campusguide.personal.ai.atlas.context.retrieval.RetrievalPolicy;
import com.campusguide.personal.ai.atlas.context.retrieval.RetrievalStrategy;
import com.campusguide.personal.ai.atlas.context.retrieval.UserRetrievalStrategy;
import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import com.campusguide.personal.ai.atlas.util.AtlasUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ContextEngine manages semantic query analysis, selective strategy-based retrieval,
 * context ranking, and metric aggregation for Atlas.
 */
@Component
@Getter
@Slf4j
public class ContextEngine {

    private final List<ContextContributor> contributors;
    private final QueryAnalyzer queryAnalyzer;
    private final ContextRetriever contextRetriever;
    private final ContextRankingService contextRankingService;
    private final com.campusguide.personal.ai.atlas.context.intelligence.ContextIntelligenceEngine intelligenceEngine;

    @Autowired
    public ContextEngine(List<ContextContributor> contributors,
                         QueryAnalyzer queryAnalyzer,
                         ContextRetriever contextRetriever,
                         ContextRankingService contextRankingService,
                         @Autowired(required = false) com.campusguide.personal.ai.atlas.context.intelligence.ContextIntelligenceEngine intelligenceEngine) {
        this.contributors = contributors != null ? contributors : List.of();
        this.queryAnalyzer = queryAnalyzer;
        this.contextRetriever = contextRetriever;
        this.contextRankingService = contextRankingService;
        this.intelligenceEngine = intelligenceEngine != null ? intelligenceEngine : new com.campusguide.personal.ai.atlas.context.intelligence.ContextIntelligenceEngine();
    }

    public ContextEngine(List<ContextContributor> contributors,
                         QueryAnalyzer queryAnalyzer,
                         ContextRetriever contextRetriever,
                         ContextRankingService contextRankingService) {
        this(contributors, queryAnalyzer, contextRetriever, contextRankingService, null);
    }

    /**
     * Backward-compatible constructor for testing and manual instantiation.
     */
    public ContextEngine(List<ContextContributor> contributors) {
        this.contributors = contributors != null ? contributors : List.of();

        QueryNormalizer normalizer = new QueryNormalizer();
        TemporalExpressionResolver temporalResolver = new TemporalExpressionResolver();
        EntityExtractor extractor = new EntityExtractor();
        IntentDetector intentDetector = new IntentDetector();

        this.queryAnalyzer = new QueryAnalyzer(normalizer, temporalResolver, extractor, intentDetector);

        List<RetrievalStrategy> strategyList = createDefaultStrategies(this.contributors);
        RetrievalPolicy policy = new RetrievalPolicy();
        this.contextRetriever = new ContextRetriever(strategyList, policy);
        this.contextRankingService = new ContextRankingService(new RelevanceScorer());
        this.intelligenceEngine = new com.campusguide.personal.ai.atlas.context.intelligence.ContextIntelligenceEngine();
    }

    private static List<RetrievalStrategy> createDefaultStrategies(List<ContextContributor> contributors) {
        List<RetrievalStrategy> strategies = new ArrayList<>();
        if (contributors == null) return strategies;

        for (ContextContributor contributor : contributors) {
            String name = contributor.getName() != null ? contributor.getName() : contributor.getClass().getSimpleName();

            if (contributor instanceof com.campusguide.personal.ai.atlas.context.contributor.UserProfileContributor uc) {
                strategies.add(new UserRetrievalStrategy(uc));
            } else if (contributor instanceof com.campusguide.personal.ai.atlas.context.contributor.AcademicContributor ac) {
                strategies.add(new AcademicRetrievalStrategy(ac));
            } else if (contributor instanceof com.campusguide.personal.ai.atlas.context.contributor.PlannerContributor pc) {
                strategies.add(new PlannerRetrievalStrategy(pc));
            } else if (contributor instanceof com.campusguide.personal.ai.atlas.context.contributor.CalendarContributor cc) {
                strategies.add(new CalendarRetrievalStrategy(cc));
            } else if (contributor instanceof com.campusguide.personal.ai.atlas.context.contributor.CampusContributor cam) {
                strategies.add(new CampusRetrievalStrategy(cam));
            } else {
                strategies.add(new RetrievalStrategy() {
                    @Override
                    public String getStrategyName() {
                        String name = contributor.getName();
                        return name != null ? name : contributor.getClass().getSimpleName();
                    }

                    @Override
                    public boolean supports(QueryContext queryContext, RetrievalPolicy policy) {
                        return true;
                    }

                    @Override
                    public double calculateRelevance(QueryContext queryContext) {
                        return 0.50;
                    }

                    @Override
                    public void retrieve(RetrievalContext retrievalContext, AtlasContext atlasContext) {
                        long s = System.currentTimeMillis();
                        String cName = contributor.getName() != null ? contributor.getName() : contributor.getClass().getSimpleName();
                        try {
                            contributor.contribute(retrievalContext.getRequest(), atlasContext);
                        } catch (Exception e) {
                            if (atlasContext.getMetrics() != null) {
                                atlasContext.getMetrics().getContributorFailures().put(cName, e.getMessage());
                            }
                        } finally {
                            long elapsed = System.currentTimeMillis() - s;
                            if (atlasContext.getMetrics() != null) {
                                atlasContext.getMetrics().getExecutionTimeMs().put(cName, elapsed);
                            }
                        }
                    }
                });
            }
        }
        return strategies;
    }

    /**
     * Constructs a populated AtlasContext using semantic query analysis, selective strategy retrieval,
     * deterministic ranking, and Context Intelligence processing.
     *
     * @param request incoming chat request
     * @return fully enriched AtlasContext with diagnostic metrics
     */
    public AtlasContext buildContext(AtlasChatRequest request) {
        return buildContext(request, null);
    }

    public AtlasContext buildContext(AtlasChatRequest request, String userId) {
        String conversationId = request != null ? request.getConversationId() : null;
        AtlasContext context = new AtlasContext(conversationId, userId);
        ContextMetrics metrics = context.getMetrics();
        if (metrics == null) {
            metrics = new ContextMetrics();
            context.setMetrics(metrics);
        }

        if (request == null) {
            return context;
        }

        if (request.getContextPlaceholders() != null) {
            context.putPlaceholders(request.getContextPlaceholders());
        }

        long totalStart = System.currentTimeMillis();

        // 1. Query Analysis
        String userQuery = request.getPrompt();
        QueryContext queryContext = queryAnalyzer.analyze(userQuery);

        // Record query diagnostics
        metrics.setDetectedIntent(queryContext.getIntent() != null ? queryContext.getIntent().name() : "UNKNOWN");
        metrics.setNormalizedQuery(queryContext.getNormalizedQuery());
        metrics.setRetrievalConfidence(queryContext.getConfidenceScore());

        List<String> entityNames = new ArrayList<>();
        if (queryContext.getEntities() != null) {
            for (ExtractedEntity entity : queryContext.getEntities()) {
                entityNames.add(entity.getNormalizedName() != null ? entity.getNormalizedName() : entity.getName());
            }
        }
        metrics.setExtractedEntities(entityNames);

        // 2. Intelligent Context Retrieval
        RetrievalContext retrievalContext = contextRetriever.retrieveContext(request, queryContext, context);

        metrics.setExecutedStrategies(retrievalContext.getExecutedStrategies());
        metrics.setSkippedStrategies(retrievalContext.getSkippedStrategies());
        metrics.setRetrievalLatencyMs(retrievalContext.getTotalLatencyMs());

        // 3. Context Ranking
        List<ContextScore> rankedScores = contextRankingService.rankContexts(context, queryContext);
        Map<String, Double> relevanceScoreMap = new LinkedHashMap<>();
        for (ContextScore score : rankedScores) {
            relevanceScoreMap.put(score.getContributorName(), score.getTotalScore());
        }
        metrics.setRelevanceScores(relevanceScoreMap);

        // 4. Context Intelligence Layer Processing (Evidence, Fusion, Conflict Resolution, Prioritization, Optimization)
        intelligenceEngine.process(request, queryContext, retrievalContext, context);

        long totalElapsed = System.currentTimeMillis() - totalStart;
        metrics.setTotalExecutionTimeMs(totalElapsed);

        // Calculate estimated context size & token count
        String contextRepresentation = context.getMergedPlaceholders().toString();
        int sizeBytes = contextRepresentation.getBytes(StandardCharsets.UTF_8).length;
        metrics.setEstimatedContextSizeBytes(sizeBytes);
        metrics.setEstimatedTokenCount(AtlasUtils.estimateTokens(contextRepresentation));

        return context;
    }
}
