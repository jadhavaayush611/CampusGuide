package com.campusguide.personal.notification.service;

import com.campusguide.common.exception.UnauthorisedException;
import com.campusguide.personal.notification.dto.CreateScheduledNotificationRequest;
import com.campusguide.personal.notification.dto.ScheduledNotificationResponse;
import com.campusguide.personal.notification.dto.UpdateNotificationStatusRequest;
import com.campusguide.personal.notification.dto.UpdateScheduledNotificationRequest;
import com.campusguide.personal.notification.entity.ScheduledNotification;
import com.campusguide.personal.notification.enums.NotificationStatus;
import com.campusguide.personal.notification.exception.ScheduledNotificationAccessDeniedException;
import com.campusguide.personal.notification.exception.ScheduledNotificationNotFoundException;
import com.campusguide.personal.notification.mapper.ScheduledNotificationMapper;
import com.campusguide.personal.notification.repository.ScheduledNotificationRepository;
import com.campusguide.personal.notification.validation.ScheduledNotificationValidator;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduledNotificationService {

    private final ScheduledNotificationRepository repository;
    private final ScheduledNotificationMapper mapper;
    private final ScheduledNotificationValidator validator;
    private final UserRepository userRepository;

    public ScheduledNotificationResponse createNotification(UserDetails userDetails, CreateScheduledNotificationRequest request) {
        UUID userId = resolveUserId(userDetails);
        LocalDateTime now = LocalDateTime.now();

        validator.validateCreate(request, now);

        ScheduledNotification entity = mapper.toEntity(request, userId);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        ScheduledNotification saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    public List<ScheduledNotificationResponse> getAllNotifications(UserDetails userDetails) {
        UUID userId = resolveUserId(userDetails);
        List<ScheduledNotification> list = repository.findByUserIdOrderByScheduledForAsc(userId);
        return list.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public ScheduledNotificationResponse getNotificationById(UserDetails userDetails, UUID id) {
        UUID userId = resolveUserId(userDetails);
        ScheduledNotification entity = findAndVerifyOwnership(id, userId);
        return mapper.toResponse(entity);
    }

    public List<ScheduledNotificationResponse> getPendingNotifications(UserDetails userDetails) {
        UUID userId = resolveUserId(userDetails);
        LocalDateTime now = LocalDateTime.now();
        List<ScheduledNotification> pending = repository
                .findByUserIdAndStatusAndScheduledForLessThanEqualOrderByScheduledForAsc(userId, NotificationStatus.SCHEDULED, now);
        return pending.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public ScheduledNotificationResponse updateNotificationStatus(UserDetails userDetails, UUID id, UpdateNotificationStatusRequest request) {
        UUID userId = resolveUserId(userDetails);
        ScheduledNotification entity = findAndVerifyOwnership(id, userId);

        LocalDateTime now = LocalDateTime.now();
        validator.validateStatusTransition(entity, request.getStatus());

        applyStatusTransition(entity, request.getStatus(), now);
        entity.setUpdatedAt(now);

        ScheduledNotification saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    public ScheduledNotificationResponse updateNotification(UserDetails userDetails, UUID id, UpdateScheduledNotificationRequest request) {
        UUID userId = resolveUserId(userDetails);
        ScheduledNotification entity = findAndVerifyOwnership(id, userId);

        LocalDateTime now = LocalDateTime.now();
        validator.validateUpdate(entity, request, now);

        entity.setTitle(request.getTitle());
        entity.setMessage(request.getMessage());
        entity.setType(request.getType());
        entity.setScheduledFor(request.getScheduledFor());
        entity.setLinkedPlannerTaskId(request.getLinkedPlannerTaskId());
        entity.setLinkedCalendarEntryId(request.getLinkedCalendarEntryId());
        entity.setLinkedEventId(request.getLinkedEventId());
        entity.setLinkedAchievementId(request.getLinkedAchievementId());
        entity.setChannel(request.getChannel());
        entity.setPriority(request.getPriority());
        entity.setMetadata(request.getMetadata());
        entity.setUpdatedAt(now);

        ScheduledNotification saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    public void deleteNotification(UserDetails userDetails, UUID id) {
        UUID userId = resolveUserId(userDetails);
        ScheduledNotification entity = findAndVerifyOwnership(id, userId);
        repository.delete(entity);
    }

    public ScheduledNotification findAndVerifyOwnership(UUID id, UUID userId) {
        ScheduledNotification entity = repository.findById(id)
                .orElseThrow(() -> new ScheduledNotificationNotFoundException("Scheduled notification not found with id: " + id));

        if (!entity.getUserId().equals(userId)) {
            throw new ScheduledNotificationAccessDeniedException("User is not authorized to access this notification");
        }

        return entity;
    }

    private void applyStatusTransition(ScheduledNotification entity, NotificationStatus newStatus, LocalDateTime now) {
        entity.setStatus(newStatus);
        if (newStatus == NotificationStatus.DELIVERED) {
            if (entity.getDeliveredAt() == null) {
                entity.setDeliveredAt(now);
            }
        } else if (newStatus == NotificationStatus.READ) {
            if (entity.getReadAt() == null) {
                entity.setReadAt(now);
            }
        }
    }

    public UUID resolveUserId(UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseGet(() -> userRepository.findByUsername(userDetails.getUsername())
                        .orElseThrow(() -> new UnauthorisedException("User not found: " + userDetails.getUsername())));

        return parseUserId(user.getId());
    }

    private UUID parseUserId(String idStr) {
        if (idStr == null) {
            throw new UnauthorisedException("User ID is missing");
        }
        try {
            return UUID.fromString(idStr);
        } catch (IllegalArgumentException e) {
            return UUID.nameUUIDFromBytes(idStr.getBytes(StandardCharsets.UTF_8));
        }
    }
}
