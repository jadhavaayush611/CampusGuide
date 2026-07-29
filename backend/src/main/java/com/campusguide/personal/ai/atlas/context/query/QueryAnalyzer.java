package com.campusguide.personal.ai.atlas.context.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Main query analyzer component that processes raw user queries into structured QueryContext models.
 */
@Component
public class QueryAnalyzer {

    private final QueryNormalizer queryNormalizer;
    private final TemporalExpressionResolver temporalExpressionResolver;
    private final EntityExtractor entityExtractor;
    private final IntentDetector intentDetector;

    public QueryAnalyzer(QueryNormalizer queryNormalizer,
                         TemporalExpressionResolver temporalExpressionResolver,
                         EntityExtractor entityExtractor,
                         IntentDetector intentDetector) {
        this.queryNormalizer = queryNormalizer;
        this.temporalExpressionResolver = temporalExpressionResolver;
        this.entityExtractor = entityExtractor;
        this.intentDetector = intentDetector;
    }

    /**
     * Analyzes an incoming query using current local time as reference.
     */
    public QueryContext analyze(String rawQuery) {
        return analyze(rawQuery, LocalDateTime.now());
    }

    /**
     * Analyzes an incoming query relative to a specific reference timestamp.
     *
     * @param rawQuery user query
     * @param referenceTime reference time for temporal resolution
     * @return fully populated QueryContext
     */
    public QueryContext analyze(String rawQuery, LocalDateTime referenceTime) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return QueryContext.builder()
                    .rawQuery(rawQuery != null ? rawQuery : "")
                    .normalizedQuery("")
                    .intent(QueryIntent.GENERAL_CONVERSATION)
                    .domainClassification(QueryDomain.GENERAL)
                    .confidenceScore(0.50)
                    .build();
        }

        // 1. Normalize Query
        String normalizedQuery = queryNormalizer.normalize(rawQuery);

        // 2. Resolve Temporal Information
        TemporalInformation temporalInfo = temporalExpressionResolver.resolve(normalizedQuery, referenceTime);

        // 3. Extract Entities
        List<ExtractedEntity> entities = entityExtractor.extractEntities(normalizedQuery);
        if (temporalInfo != null && temporalInfo.isResolved()) {
            entities.add(ExtractedEntity.builder()
                    .name(temporalInfo.getRawExpression())
                    .type(EntityType.TEMPORAL_EXPRESSION)
                    .normalizedName(temporalInfo.getResolutionType())
                    .confidence(0.95)
                    .build());
        }

        // 4. Detect Intent & Domain
        IntentDetector.IntentResult intentResult = intentDetector.detectIntent(normalizedQuery, entities);

        // 5. Generate Retrieval Hints
        List<String> retrievalHints = generateRetrievalHints(intentResult.domain(), intentResult.intent(), entities, temporalInfo);

        return QueryContext.builder()
                .rawQuery(rawQuery)
                .normalizedQuery(normalizedQuery)
                .intent(intentResult.intent())
                .domainClassification(intentResult.domain())
                .entities(entities)
                .temporalInformation(temporalInfo)
                .retrievalHints(retrievalHints)
                .confidenceScore(intentResult.confidence())
                .build();
    }

    private List<String> generateRetrievalHints(QueryDomain domain, QueryIntent intent, List<ExtractedEntity> entities, TemporalInformation temporalInfo) {
        List<String> hints = new ArrayList<>();
        hints.add("ALWAYS_USER_PROFILE");

        if (domain == QueryDomain.ACADEMIC) {
            hints.add("FETCH_ACADEMIC_SUMMARY");
            hints.add("FETCH_COURSES");
        } else if (domain == QueryDomain.PLANNER) {
            hints.add("FETCH_ACTIVE_TASKS");
            hints.add("FETCH_OVERDUE_TASKS");
        } else if (domain == QueryDomain.CALENDAR) {
            hints.add("FETCH_TODAY_EVENTS");
            hints.add("FETCH_UPCOMING_EVENTS");
        } else if (domain == QueryDomain.CAMPUS) {
            hints.add("FETCH_CAMPUS_NOTICES");
            hints.add("FETCH_LOCATION_DATA");
        }

        if (temporalInfo != null && temporalInfo.isResolved()) {
            hints.add("TEMPORAL_RANGE_ACTIVE");
        }

        return hints;
    }
}
