package com.campusguide.personal.ai.atlas.context.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * Strongly-typed domain context model for Planner.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlannerContext {

    private int activeTasksCount;
    private int overdueTasksCount;
    private int completedTasksCount;

    @Builder.Default
    private List<TaskSummary> topTasks = Collections.emptyList();

    private String summary;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaskSummary {
        private String id;
        private String title;
        private String dueDate;
        private String status;
        private String priority;
    }
}
