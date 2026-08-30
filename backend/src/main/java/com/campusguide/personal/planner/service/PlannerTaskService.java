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
import com.campusguide.common.attachment.entity.AttachmentOwnerType;
import com.campusguide.common.attachment.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PlannerTaskService {

    private final PlannerTaskRepository plannerTaskRepository;
    private final PlannerTaskMapper plannerTaskMapper;
    private final PlannerTaskValidator plannerTaskValidator;
    private final CurrentUserService currentUserService;
    private final AttachmentService attachmentService;

    public PlannerTaskService(PlannerTaskRepository plannerTaskRepository,
                              PlannerTaskMapper plannerTaskMapper,
                              PlannerTaskValidator plannerTaskValidator,
                              CurrentUserService currentUserService) {
        this(plannerTaskRepository, plannerTaskMapper, plannerTaskValidator, currentUserService, null);
    }

    @Autowired
    public PlannerTaskService(PlannerTaskRepository plannerTaskRepository,
                              PlannerTaskMapper plannerTaskMapper,
                              PlannerTaskValidator plannerTaskValidator,
                              CurrentUserService currentUserService,
                              @Autowired(required = false) AttachmentService attachmentService) {
        this.plannerTaskRepository = plannerTaskRepository;
        this.plannerTaskMapper = plannerTaskMapper;
        this.plannerTaskValidator = plannerTaskValidator;
        this.currentUserService = currentUserService;
        this.attachmentService = attachmentService;
    }

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
                .map(t -> {
                    PlannerTaskResponse res = plannerTaskMapper.toResponse(t);
                    if (attachmentService != null) {
                        res.setAttachments(attachmentService.getAttachmentsForOwner(userDetails, AttachmentOwnerType.PLANNER_TASK, t.getId()));
                    }
                    return res;
                })
                .collect(Collectors.toList());
    }

    public PlannerTaskResponse getTaskById(UserDetails userDetails, UUID id) {
        String userId = resolveUserId(userDetails);
        PlannerTask task = findAndVerifyOwnership(id, userId);
        PlannerTaskResponse response = plannerTaskMapper.toResponse(task);
        if (attachmentService != null) {
            response.setAttachments(attachmentService.getAttachmentsForOwner(userDetails, AttachmentOwnerType.PLANNER_TASK, id));
        }
        return response;
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
        if (attachmentService != null) {
            attachmentService.deleteByOwner(AttachmentOwnerType.PLANNER_TASK, id);
        }
        plannerTaskRepository.delete(task);
    }

    public PlannerTask findAndVerifyOwnership(UUID id, String userId) {
        return plannerTaskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new PlannerTaskNotFoundException("Planner task not found"));
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
