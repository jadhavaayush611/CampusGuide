package com.campusguide.personal.notification.validation;

import com.campusguide.campus.event.repository.EventRepository;
import com.campusguide.common.exception.ResourceNotFoundException;
import com.campusguide.personal.achievement.repository.AchievementProgressRepository;
import com.campusguide.personal.calendar.repository.CalendarEntryRepository;
import com.campusguide.personal.notification.dto.CreateScheduledNotificationRequest;
import com.campusguide.personal.notification.dto.UpdateScheduledNotificationRequest;
import com.campusguide.personal.notification.entity.ScheduledNotification;
import com.campusguide.personal.notification.enums.NotificationStatus;
import com.campusguide.personal.notification.exception.ScheduledNotificationValidationException;
import com.campusguide.personal.planner.repository.PlannerTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class ScheduledNotificationValidator {

    private final PlannerTaskRepository plannerTaskRepository;
    private final CalendarEntryRepository calendarEntryRepository;
    private final EventRepository eventRepository;
    private final AchievementProgressRepository achievementProgressRepository;

    public void validateCreate(CreateScheduledNotificationRequest request, LocalDateTime now) {
        if (request.getScheduledFor() == null || !request.getScheduledFor().isAfter(now)) {
            throw new ScheduledNotificationValidationException("scheduledFor must be in the future");
        }
        validateSingleReference(
                request.getLinkedPlannerTaskId(),
                request.getLinkedCalendarEntryId(),
                request.getLinkedEventId(),
                request.getLinkedAchievementId()
        );
        validateReferencedEntitiesExist(
                request.getLinkedPlannerTaskId(),
                request.getLinkedCalendarEntryId(),
                request.getLinkedEventId(),
                request.getLinkedAchievementId()
        );
    }

    public void validateUpdate(ScheduledNotification existing, UpdateScheduledNotificationRequest request, LocalDateTime now) {
        if (request.getScheduledFor() == null || !request.getScheduledFor().isAfter(now)) {
            throw new ScheduledNotificationValidationException("scheduledFor must be in the future");
        }
        validateSingleReference(
                request.getLinkedPlannerTaskId(),
                request.getLinkedCalendarEntryId(),
                request.getLinkedEventId(),
                request.getLinkedAchievementId()
        );
        validateReferencedEntitiesExist(
                request.getLinkedPlannerTaskId(),
                request.getLinkedCalendarEntryId(),
                request.getLinkedEventId(),
                request.getLinkedAchievementId()
        );
    }

    public void validateSingleReference(UUID linkedPlannerTaskId, UUID linkedCalendarEntryId, UUID linkedEventId, UUID linkedAchievementId) {
        long count = Stream.of(linkedPlannerTaskId, linkedCalendarEntryId, linkedEventId, linkedAchievementId)
                .filter(Objects::nonNull)
                .count();
        if (count > 1) {
            throw new ScheduledNotificationValidationException("Notification may reference at most one aggregate");
        }
    }

    public void validateReferencedEntitiesExist(UUID linkedPlannerTaskId, UUID linkedCalendarEntryId, UUID linkedEventId, UUID linkedAchievementId) {
        if (linkedPlannerTaskId != null && !plannerTaskRepository.existsById(linkedPlannerTaskId)) {
            throw new ResourceNotFoundException("Planner task not found with id: " + linkedPlannerTaskId);
        }
        if (linkedCalendarEntryId != null && !calendarEntryRepository.existsById(linkedCalendarEntryId)) {
            throw new ResourceNotFoundException("Calendar entry not found with id: " + linkedCalendarEntryId);
        }
        if (linkedEventId != null && !eventRepository.existsById(linkedEventId)) {
            throw new ResourceNotFoundException("Event not found with id: " + linkedEventId);
        }
        if (linkedAchievementId != null && !achievementProgressRepository.existsById(linkedAchievementId)) {
            throw new ResourceNotFoundException("Achievement progress not found with id: " + linkedAchievementId);
        }
    }

    public void validateStatusTransition(ScheduledNotification existing, NotificationStatus newStatus) {
        NotificationStatus current = existing.getStatus();
        if (current == newStatus) {
            return;
        }

        if (current == NotificationStatus.CANCELLED && newStatus == NotificationStatus.SCHEDULED) {
            throw new ScheduledNotificationValidationException("CANCELLED notification cannot transition back to SCHEDULED");
        }

        if (newStatus == NotificationStatus.READ && current != NotificationStatus.DELIVERED) {
            throw new ScheduledNotificationValidationException("READ status requires notification to be DELIVERED first");
        }

        if (current == NotificationStatus.CANCELLED && (newStatus == NotificationStatus.DELIVERED || newStatus == NotificationStatus.READ)) {
            throw new ScheduledNotificationValidationException("CANCELLED notification cannot transition to " + newStatus);
        }

        if (current == NotificationStatus.READ && (newStatus == NotificationStatus.SCHEDULED || newStatus == NotificationStatus.DELIVERED)) {
            throw new ScheduledNotificationValidationException("READ notification cannot transition back to " + newStatus);
        }
    }
}
