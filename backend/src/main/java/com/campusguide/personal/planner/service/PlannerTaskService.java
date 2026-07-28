package com.campusguide.personal.planner.service;

import com.campusguide.common.exception.UnauthorisedException;
import com.campusguide.personal.planner.dto.CreatePlannerTaskRequest;
import com.campusguide.personal.planner.dto.PlannerTaskResponse;
import com.campusguide.personal.planner.dto.UpdatePlannerTaskRequest;
import com.campusguide.personal.planner.dto.UpdateTaskStatusRequest;
import com.campusguide.personal.planner.entity.PlannerTask;
import com.campusguide.personal.planner.entity.TaskStatus;
import com.campusguide.personal.planner.exception.PlannerTaskAccessDeniedException;
import com.campusguide.personal.planner.exception.PlannerTaskNotFoundException;
import com.campusguide.personal.planner.mapper.PlannerTaskMapper;
import com.campusguide.personal.planner.repository.PlannerTaskRepository;
import com.campusguide.personal.planner.validation.PlannerTaskValidator;
import com.campusguide.platform.user.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlannerTaskService {

    private final PlannerTaskRepository plannerTaskRepository;
    private final PlannerTaskMapper plannerTaskMapper;
    private final PlannerTaskValidator plannerTaskValidator;
    private final CurrentUserService currentUserService;

    public PlannerTaskResponse createTask(UserDetails userDetails, CreatePlannerTaskRequest request) {
        String userId = resolveUserId(userDetails);
        Instant now = Instant.now();
        plannerTaskValidator.validateCreate(request, now);

        PlannerTask task = plannerTaskMapper.toEntity(request, userId);

        PlannerTask savedTask = plannerTaskRepository.save(task);
        return plannerTaskMapper.toResponse(savedTask);
    }

    public List<PlannerTaskResponse> getAllTasks(UserDetails userDetails) {
        String userId = resolveUserId(userDetails);
        List<PlannerTask> tasks = plannerTaskRepository.findByUserIdOrderByDueAtAsc(userId);
        return tasks.stream()
                .map(plannerTaskMapper::toResponse)
                .collect(Collectors.toList());
    }

    public PlannerTaskResponse getTaskById(UserDetails userDetails, UUID id) {
        String userId = resolveUserId(userDetails);
        PlannerTask task = findAndVerifyOwnership(id, userId);
        return plannerTaskMapper.toResponse(task);
    }

    public PlannerTaskResponse updateTask(UserDetails userDetails, UUID id, UpdatePlannerTaskRequest request) {
        String userId = resolveUserId(userDetails);
        PlannerTask task = findAndVerifyOwnership(id, userId);

        plannerTaskValidator.validateUpdate(task, request);

        LocalDateTime now = LocalDateTime.now();

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setType(request.getType());
        task.setPriority(request.getPriority());
        task.setLinkedEventId(request.getLinkedEventId());
        task.setDueAt(request.getDueAt());
        task.setReminderAt(request.getReminderAt());
        task.setNotes(request.getNotes());

        if (request.getStatus() != null && request.getStatus() != task.getStatus()) {
            applyStatusTransition(task, request.getStatus(), now);
        }

        PlannerTask savedTask = plannerTaskRepository.save(task);
        return plannerTaskMapper.toResponse(savedTask);
    }

    public PlannerTaskResponse updateTaskStatus(UserDetails userDetails, UUID id, UpdateTaskStatusRequest request) {
        String userId = resolveUserId(userDetails);
        PlannerTask task = findAndVerifyOwnership(id, userId);

        plannerTaskValidator.validateStatusChange(task, request.getStatus());

        LocalDateTime now = LocalDateTime.now();
        applyStatusTransition(task, request.getStatus(), now);

        PlannerTask savedTask = plannerTaskRepository.save(task);
        return plannerTaskMapper.toResponse(savedTask);
    }

    public void deleteTask(UserDetails userDetails, UUID id) {
        String userId = resolveUserId(userDetails);
        PlannerTask task = findAndVerifyOwnership(id, userId);
        plannerTaskRepository.delete(task);
    }

    public PlannerTask findAndVerifyOwnership(UUID id, String userId) {
        PlannerTask task = plannerTaskRepository.findById(id)
                .orElseThrow(() -> new PlannerTaskNotFoundException("Planner task not found with id: " + id));

        if (!task.getUserId().equals(userId)) {
            throw new PlannerTaskAccessDeniedException("User is not authorized to access this planner task");
        }

        return task;
    }

    private void applyStatusTransition(PlannerTask task, TaskStatus newStatus, LocalDateTime now) {
        task.setStatus(newStatus);
        if (newStatus == TaskStatus.COMPLETED) {
            if (task.getCompletedAt() == null) {
                task.setCompletedAt(now);
            }
        } else {
            task.setCompletedAt(null);
        }

        if (newStatus == TaskStatus.CANCELLED) {
            task.setReminderAt(null);
        }
    }

    public String resolveUserId(UserDetails userDetails) {
        return currentUserService.getCurrentUserId(userDetails);
    }
}
