package com.campusguide.personal.planner.repository;

import com.campusguide.personal.planner.entity.PlannerTask;
import com.campusguide.personal.planner.entity.TaskPriority;
import com.campusguide.personal.planner.entity.TaskStatus;
import com.campusguide.personal.planner.entity.TaskType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PlannerTaskRepositoryIT {

    @Autowired
    private PlannerTaskRepository plannerTaskRepository;

    private UUID userId1;
    private UUID userId2;
    private PlannerTask task1;
    private PlannerTask task2;

    @BeforeEach
    void setUp() {
        plannerTaskRepository.deleteAll();

        userId1 = UUID.randomUUID();
        userId2 = UUID.randomUUID();

        LocalDateTime now = LocalDateTime.now();

        task1 = PlannerTask.builder()
                .id(UUID.randomUUID())
                .userId(userId1)
                .title("Study Math")
                .description("Prepare for midterm")
                .type(TaskType.STUDY)
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.TODO)
                .dueAt(now.plusDays(2))
                .createdAt(now)
                .updatedAt(now)
                .build();

        task2 = PlannerTask.builder()
                .id(UUID.randomUUID())
                .userId(userId1)
                .title("Buy groceries")
                .description("Milk, Eggs")
                .type(TaskType.PERSONAL)
                .priority(TaskPriority.LOW)
                .status(TaskStatus.COMPLETED)
                .dueAt(now.plusDays(1))
                .completedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        plannerTaskRepository.save(task1);
        plannerTaskRepository.save(task2);
    }

    @AfterEach
    void tearDown() {
        plannerTaskRepository.deleteAll();
    }

    @Test
    void testSaveAndFindById() {
        Optional<PlannerTask> found = plannerTaskRepository.findById(task1.getId());
        assertTrue(found.isPresent());
        assertEquals("Study Math", found.get().getTitle());
        assertEquals(userId1, found.get().getUserId());
    }

    @Test
    void testFindByUserIdOrderByDueAtAsc() {
        List<PlannerTask> tasks = plannerTaskRepository.findByUserIdOrderByDueAtAsc(userId1);
        assertEquals(2, tasks.size());
        // task2 dueAt is now + 1 day, task1 dueAt is now + 2 days -> task2 should come first
        assertEquals(task2.getId(), tasks.get(0).getId());
        assertEquals(task1.getId(), tasks.get(1).getId());
    }

    @Test
    void testFindByUserIdAndStatus() {
        List<PlannerTask> todoTasks = plannerTaskRepository.findByUserIdAndStatus(userId1, TaskStatus.TODO);
        assertEquals(1, todoTasks.size());
        assertEquals(task1.getId(), todoTasks.get(0).getId());

        List<PlannerTask> completedTasks = plannerTaskRepository.findByUserIdAndStatus(userId1, TaskStatus.COMPLETED);
        assertEquals(1, completedTasks.size());
        assertEquals(task2.getId(), completedTasks.get(0).getId());
    }

    @Test
    void testFindByIdAndUserId() {
        Optional<PlannerTask> found = plannerTaskRepository.findByIdAndUserId(task1.getId(), userId1);
        assertTrue(found.isPresent());

        Optional<PlannerTask> notFoundOtherUser = plannerTaskRepository.findByIdAndUserId(task1.getId(), userId2);
        assertFalse(notFoundOtherUser.isPresent());
    }

    @Test
    void testDeleteTask() {
        plannerTaskRepository.deleteById(task1.getId());
        Optional<PlannerTask> found = plannerTaskRepository.findById(task1.getId());
        assertFalse(found.isPresent());
    }
}
