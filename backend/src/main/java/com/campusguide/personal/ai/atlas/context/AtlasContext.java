package com.campusguide.personal.ai.atlas.context;

import com.campusguide.personal.ai.atlas.context.metrics.ContextMetrics;
import com.campusguide.personal.ai.atlas.context.model.AcademicContext;
import com.campusguide.personal.ai.atlas.context.model.CalendarContext;
import com.campusguide.personal.ai.atlas.context.model.CampusContext;
import com.campusguide.personal.ai.atlas.context.model.PlannerContext;
import com.campusguide.personal.ai.atlas.context.model.UserContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Strongly-typed aggregate domain context model for Atlas AI.
 * Accumulates domain context models (UserContext, PlannerContext, CalendarContext, AcademicContext, CampusContext)
 * and diagnostic metrics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtlasContext {

    private String conversationId;
    private String userId;

    private UserContext userContext;
    private PlannerContext plannerContext;
    private CalendarContext calendarContext;
    private AcademicContext academicContext;
    private CampusContext campusContext;

    @Builder.Default
    private ContextMetrics metrics = new ContextMetrics();

    @Builder.Default
    private Map<String, Object> contributions = new ConcurrentHashMap<>();

    @Builder.Default
    private Map<String, Object> placeholders = new ConcurrentHashMap<>();

    @Builder.Default
    private Map<String, Object> metadata = new ConcurrentHashMap<>();

    public AtlasContext(String conversationId, String userId) {
        this.conversationId = conversationId;
        this.userId = userId;
        this.metrics = new ContextMetrics();
        this.contributions = new ConcurrentHashMap<>();
        this.placeholders = new ConcurrentHashMap<>();
        this.metadata = new ConcurrentHashMap<>();
    }

    public void addContribution(String contributorName, Object data) {
        if (contributorName != null && data != null) {
            this.contributions.put(contributorName, data);
        }
    }

    public Object getContribution(String contributorName) {
        if (contributorName == null) return null;
        if ("userProfile".equals(contributorName) && userContext != null) return userContext;
        if ("planner".equals(contributorName) && plannerContext != null) return plannerContext;
        if ("calendar".equals(contributorName) && calendarContext != null) return calendarContext;
        if ("academic".equals(contributorName) && academicContext != null) return academicContext;
        if ("campus".equals(contributorName) && campusContext != null) return campusContext;
        return this.contributions.get(contributorName);
    }

    public void putPlaceholder(String key, Object value) {
        if (key != null && value != null) {
            this.placeholders.put(key, value);
        }
    }

    public void putPlaceholders(Map<String, Object> map) {
        if (map != null) {
            map.forEach(this::putPlaceholder);
        }
    }

    /**
     * Returns a consolidated map of all placeholders combining explicit placeholders and domain context fields.
     */
    public Map<String, Object> getMergedPlaceholders() {
        Map<String, Object> merged = new ConcurrentHashMap<>(this.placeholders);

        if (userContext != null) {
            if (userContext.getName() != null && !merged.containsKey("student_name")) {
                merged.put("student_name", userContext.getName());
            }
            if (userContext.getSummary() != null && !merged.containsKey("user_profile_summary")) {
                merged.put("user_profile_summary", userContext.getSummary());
            }
        }
        if (academicContext != null) {
            if (academicContext.getDepartment() != null && !merged.containsKey("department")) {
                merged.put("department", academicContext.getDepartment());
            }
            if (academicContext.getSummary() != null && !merged.containsKey("academic_summary")) {
                merged.put("academic_summary", academicContext.getSummary());
            }
        }
        if (plannerContext != null && plannerContext.getSummary() != null && !merged.containsKey("planner_summary")) {
            merged.put("planner_summary", plannerContext.getSummary());
        }
        if (calendarContext != null && calendarContext.getSummary() != null && !merged.containsKey("calendar_summary")) {
            merged.put("calendar_summary", calendarContext.getSummary());
        }
        if (campusContext != null && campusContext.getSummary() != null && !merged.containsKey("campus_summary")) {
            merged.put("campus_summary", campusContext.getSummary());
        }

        this.contributions.forEach((key, val) -> {
            if (!merged.containsKey(key)) {
                merged.put(key, val);
            }
        });
        return Collections.unmodifiableMap(merged);
    }
}
