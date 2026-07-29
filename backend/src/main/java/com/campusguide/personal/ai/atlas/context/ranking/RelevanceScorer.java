package com.campusguide.personal.ai.atlas.context.ranking;

import com.campusguide.personal.ai.atlas.context.model.AcademicContext;
import com.campusguide.personal.ai.atlas.context.model.CalendarContext;
import com.campusguide.personal.ai.atlas.context.model.CampusContext;
import com.campusguide.personal.ai.atlas.context.model.PlannerContext;
import com.campusguide.personal.ai.atlas.context.model.UserContext;
import com.campusguide.personal.ai.atlas.context.query.EntityType;
import com.campusguide.personal.ai.atlas.context.query.ExtractedEntity;
import com.campusguide.personal.ai.atlas.context.query.QueryContext;
import com.campusguide.personal.ai.atlas.context.query.QueryDomain;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Computes individual ranking dimension scores (intent relevance, entity overlap,
 * freshness, source priority, confidence, completeness) for domain context items.
 */
@Component
public class RelevanceScorer {

    public ContextScore scoreContext(String contributorName, Object contextObj, QueryContext queryContext) {
        if (contributorName == null) {
            contributorName = "unknown";
        }

        double intentRelevance = calculateIntentRelevance(contributorName, queryContext);
        double entityOverlap = calculateEntityOverlap(contributorName, queryContext);
        double freshness = calculateFreshness(contextObj);
        double sourcePriority = calculateSourcePriority(contributorName);
        double confidence = calculateConfidence(queryContext);
        double completeness = calculateCompleteness(contextObj);

        double totalScore = (0.25 * intentRelevance)
                + (0.20 * entityOverlap)
                + (0.15 * freshness)
                + (0.15 * sourcePriority)
                + (0.15 * confidence)
                + (0.10 * completeness);

        return ContextScore.builder()
                .contributorName(contributorName)
                .totalScore(Math.min(1.0, Math.max(0.0, totalScore)))
                .intentRelevance(intentRelevance)
                .entityOverlap(entityOverlap)
                .freshness(freshness)
                .sourcePriority(sourcePriority)
                .confidence(confidence)
                .completeness(completeness)
                .build();
    }

    public double calculateIntentRelevance(String name, QueryContext qc) {
        if (qc == null || qc.getDomainClassification() == null) return 0.50;
        QueryDomain domain = qc.getDomainClassification();

        if ("user".equalsIgnoreCase(name) || "userProfile".equalsIgnoreCase(name)) {
            return (domain == QueryDomain.USER) ? 1.0 : 0.80;
        }
        if ("academic".equalsIgnoreCase(name)) {
            return (domain == QueryDomain.ACADEMIC) ? 1.0 : 0.30;
        }
        if ("planner".equalsIgnoreCase(name)) {
            return (domain == QueryDomain.PLANNER) ? 1.0 : 0.30;
        }
        if ("calendar".equalsIgnoreCase(name)) {
            return (domain == QueryDomain.CALENDAR) ? 1.0 : 0.30;
        }
        if ("campus".equalsIgnoreCase(name)) {
            return (domain == QueryDomain.CAMPUS) ? 1.0 : 0.30;
        }

        return 0.50;
    }

    public double calculateEntityOverlap(String name, QueryContext qc) {
        if (qc == null || qc.getEntities() == null || qc.getEntities().isEmpty()) {
            return 0.50;
        }

        List<ExtractedEntity> entities = qc.getEntities();
        for (ExtractedEntity entity : entities) {
            if (entity.getType() == EntityType.CAMPUS_LOCATION && ("campus".equalsIgnoreCase(name))) return 0.95;
            if (entity.getType() == EntityType.ACADEMIC_CONCEPT && ("academic".equalsIgnoreCase(name))) return 0.95;
            if (entity.getType() == EntityType.PLANNER_ITEM && ("planner".equalsIgnoreCase(name))) return 0.95;
            if (entity.getType() == EntityType.CALENDAR_EVENT && ("calendar".equalsIgnoreCase(name))) return 0.95;
            if (entity.getType() == EntityType.TEMPORAL_EXPRESSION && ("calendar".equalsIgnoreCase(name) || "planner".equalsIgnoreCase(name))) return 0.85;
        }

        return 0.40;
    }

    public double calculateFreshness(Object contextObj) {
        // High freshness baseline for domain models present in request
        return contextObj != null ? 0.90 : 0.0;
    }

    public double calculateSourcePriority(String name) {
        if ("user".equalsIgnoreCase(name) || "userProfile".equalsIgnoreCase(name)) return 1.0;
        if ("academic".equalsIgnoreCase(name)) return 0.90;
        if ("planner".equalsIgnoreCase(name)) return 0.85;
        if ("calendar".equalsIgnoreCase(name)) return 0.80;
        if ("campus".equalsIgnoreCase(name)) return 0.75;
        return 0.50;
    }

    public double calculateConfidence(QueryContext qc) {
        return qc != null ? qc.getConfidenceScore() : 0.50;
    }

    public double calculateCompleteness(Object contextObj) {
        if (contextObj == null) return 0.0;
        if (contextObj instanceof UserContext uc) {
            return (uc.getName() != null ? 0.5 : 0.0) + (uc.getSummary() != null ? 0.5 : 0.0);
        }
        if (contextObj instanceof AcademicContext ac) {
            return (ac.getDepartment() != null ? 0.5 : 0.0) + (ac.getSummary() != null ? 0.5 : 0.0);
        }
        if (contextObj instanceof PlannerContext pc) {
            return pc.getSummary() != null ? 1.0 : 0.5;
        }
        if (contextObj instanceof CalendarContext cc) {
            return cc.getSummary() != null ? 1.0 : 0.5;
        }
        if (contextObj instanceof CampusContext cam) {
            return cam.getSummary() != null ? 1.0 : 0.5;
        }
        return 0.50;
    }
}
