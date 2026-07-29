package com.campusguide.personal.ai.atlas.context.service;

import com.campusguide.personal.ai.atlas.context.model.PlannerContext;
import com.campusguide.personal.ai.atlas.context.model.PlannerContext.TaskSummary;
import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import com.campusguide.personal.planner.entity.PlannerTask;
import com.campusguide.personal.planner.entity.TaskStatus;
import com.campusguide.personal.planner.repository.PlannerTaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsible for querying, summarizing, and normalizing Planner domain context.
 */
@Service
@Slf4j
public class PlannerContextService {

    private static final int MAX_TOP_TASKS = 5;
    private final PlannerTaskRepository plannerTaskRepository;

    public PlannerContextService(@Autowired(required = false) PlannerTaskRepository plannerTaskRepository) {
        this.plannerTaskRepository = plannerTaskRepository;
    }

    /**
     * Queries, filters, and normalizes planner context with deterministic ordering and bounded limits.
     *
     * @param userId target user ID
     * @param request chat request
     * @return normalized PlannerContext
     */
    public PlannerContext getPlannerContext(String userId, AtlasChatRequest request) {
        int activeTasksCount = 0;
        int overdueTasksCount = 0;
        int completedTasksCount = 0;
        List<TaskSummary> topTasks = new ArrayList<>();

        if (plannerTaskRepository != null && StringUtils.hasText(userId)) {
            try {
                List<PlannerTask> userTasks = plannerTaskRepository.findByUserId(userId);
                if (userTasks != null && !userTasks.isEmpty()) {
                    LocalDateTime now = LocalDateTime.now();

                    for (PlannerTask task : userTasks) {
                        if (task.getStatus() == TaskStatus.COMPLETED) {
                            completedTasksCount++;
                        } else {
                            activeTasksCount++;
                            if (task.getDueAt() != null && task.getDueAt().isBefore(now)) {
                                overdueTasksCount++;
                            }
                        }
                    }

                    // Enforce deterministic ordering: Pending/In-progress first, then due date asc, title asc
                    topTasks = userTasks.stream()
                            .filter(t -> t.getStatus() != TaskStatus.COMPLETED)
                            .sorted(Comparator
                                    .comparing((PlannerTask t) -> t.getDueAt() != null ? t.getDueAt() : LocalDateTime.MAX)
                                    .thenComparing(t -> t.getTitle() != null ? t.getTitle() : "")
                                    .thenComparing(t -> t.getId() != null ? t.getId().toString() : ""))
                            .limit(MAX_TOP_TASKS)
                            .map(this::toTaskSummary)
                            .collect(Collectors.toList());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch planner tasks for userId [{}]: {}", userId, e.getMessage());
            }
        }

        String summary;
        if (activeTasksCount == 0) {
            summary = "Planner context summary: Clear task list, no pending tasks.";
        } else if (overdueTasksCount > 0) {
            summary = String.format("Planner context summary: %d active task(s) (%d overdue).", activeTasksCount, overdueTasksCount);
        } else {
            summary = String.format("Planner context summary: %d active task(s) upcoming.", activeTasksCount);
        }

        return PlannerContext.builder()
                .activeTasksCount(activeTasksCount)
                .overdueTasksCount(overdueTasksCount)
                .completedTasksCount(completedTasksCount)
                .topTasks(topTasks)
                .summary(summary)
                .build();
    }

    private TaskSummary toTaskSummary(PlannerTask task) {
        return TaskSummary.builder()
                .id(task.getId() != null ? task.getId().toString() : null)
                .title(task.getTitle())
                .dueDate(task.getDueAt() != null ? task.getDueAt().toString() : null)
                .status(task.getStatus() != null ? task.getStatus().name() : null)
                .priority(task.getPriority() != null ? task.getPriority().name() : null)
                .build();
    }
}
