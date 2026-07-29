package com.campusguide.personal.ai.atlas.context.service;

import com.campusguide.personal.ai.atlas.context.model.PlannerContext;
import com.campusguide.personal.planner.entity.PlannerTask;
import com.campusguide.personal.planner.entity.TaskPriority;
import com.campusguide.personal.planner.entity.TaskStatus;
import com.campusguide.personal.planner.repository.PlannerTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlannerContextServiceTest {

    @Mock
    private PlannerTaskRepository plannerTaskRepository;

    private PlannerContextService plannerContextService;

    @BeforeEach
    void setUp() {
        plannerContextService = new PlannerContextService(plannerTaskRepository);
    }

    @Test
    void testGetPlannerContext_BoundedAndSortedTasks() {
        String userId = "user-1";
        LocalDateTime now = LocalDateTime.now();

        PlannerTask t1 = PlannerTask.builder().id(UUID.randomUUID()).userId(userId).title("Task A").status(TaskStatus.TODO).dueAt(now.plusDays(2)).priority(TaskPriority.HIGH).build();
        PlannerTask t2 = PlannerTask.builder().id(UUID.randomUUID()).userId(userId).title("Task B").status(TaskStatus.IN_PROGRESS).dueAt(now.minusDays(1)).priority(TaskPriority.HIGH).build();
        PlannerTask t3 = PlannerTask.builder().id(UUID.randomUUID()).userId(userId).title("Task C").status(TaskStatus.COMPLETED).dueAt(now.minusDays(2)).priority(TaskPriority.MEDIUM).build();

        when(plannerTaskRepository.findByUserId(userId)).thenReturn(List.of(t1, t2, t3));

        PlannerContext context = plannerContextService.getPlannerContext(userId, null);

        assertNotNull(context);
        assertEquals(2, context.getActiveTasksCount());
        assertEquals(1, context.getOverdueTasksCount());
        assertEquals(1, context.getCompletedTasksCount());

        // Bounded to active tasks, sorted by due date asc (t2 overdue comes before t1)
        assertEquals(2, context.getTopTasks().size());
        assertEquals("Task B", context.getTopTasks().get(0).getTitle());
        assertEquals("Task A", context.getTopTasks().get(1).getTitle());
    }
}
