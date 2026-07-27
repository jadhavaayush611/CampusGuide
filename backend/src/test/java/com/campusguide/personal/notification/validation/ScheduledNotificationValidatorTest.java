package com.campusguide.personal.notification.validation;

import com.campusguide.campus.event.repository.EventRepository;
import com.campusguide.common.exception.ResourceNotFoundException;
import com.campusguide.personal.achievement.repository.AchievementProgressRepository;
import com.campusguide.personal.calendar.repository.CalendarEntryRepository;
import com.campusguide.personal.notification.dto.CreateScheduledNotificationRequest;
import com.campusguide.personal.notification.entity.ScheduledNotification;
import com.campusguide.personal.notification.enums.NotificationChannel;
import com.campusguide.personal.notification.enums.NotificationPriority;
import com.campusguide.personal.notification.enums.NotificationStatus;
import com.campusguide.personal.notification.enums.NotificationType;
import com.campusguide.personal.notification.exception.ScheduledNotificationValidationException;
import com.campusguide.personal.planner.repository.PlannerTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledNotificationValidatorTest {

    @Mock
    private PlannerTaskRepository plannerTaskRepository;

    @Mock
    private CalendarEntryRepository calendarEntryRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private AchievementProgressRepository achievementProgressRepository;

    @InjectMocks
    private ScheduledNotificationValidator validator;

    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
    }

    @Test
    void testValidateCreate_PastScheduledFor() {
        CreateScheduledNotificationRequest request = CreateScheduledNotificationRequest.builder()
                .title("Title")
                .message("Message")
                .type(NotificationType.REMINDER)
                .scheduledFor(now.minusMinutes(5)) // past
                .channel(NotificationChannel.IN_APP)
                .priority(NotificationPriority.NORMAL)
                .build();

        assertThrows(ScheduledNotificationValidationException.class, () -> validator.validateCreate(request, now));
    }

    @Test
    void testValidateCreate_MultipleReferences() {
        CreateScheduledNotificationRequest request = CreateScheduledNotificationRequest.builder()
                .title("Title")
                .message("Message")
                .type(NotificationType.REMINDER)
                .scheduledFor(now.plusHours(1))
                .linkedPlannerTaskId(UUID.randomUUID())
                .linkedEventId(UUID.randomUUID())
                .channel(NotificationChannel.IN_APP)
                .priority(NotificationPriority.NORMAL)
                .build();

        assertThrows(ScheduledNotificationValidationException.class, () -> validator.validateCreate(request, now));
    }

    @Test
    void testValidateCreate_NonExistentReference() {
        UUID taskId = UUID.randomUUID();
        CreateScheduledNotificationRequest request = CreateScheduledNotificationRequest.builder()
                .title("Title")
                .message("Message")
                .type(NotificationType.REMINDER)
                .scheduledFor(now.plusHours(1))
                .linkedPlannerTaskId(taskId)
                .channel(NotificationChannel.IN_APP)
                .priority(NotificationPriority.NORMAL)
                .build();

        when(plannerTaskRepository.existsById(taskId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> validator.validateCreate(request, now));
    }

    @Test
    void testValidateCreate_ValidSingleReference() {
        UUID taskId = UUID.randomUUID();
        CreateScheduledNotificationRequest request = CreateScheduledNotificationRequest.builder()
                .title("Title")
                .message("Message")
                .type(NotificationType.REMINDER)
                .scheduledFor(now.plusHours(1))
                .linkedPlannerTaskId(taskId)
                .channel(NotificationChannel.IN_APP)
                .priority(NotificationPriority.NORMAL)
                .build();

        when(plannerTaskRepository.existsById(taskId)).thenReturn(true);

        assertDoesNotThrow(() -> validator.validateCreate(request, now));
    }

    @Test
    void testStatusTransition_CancelledToScheduledForbidden() {
        ScheduledNotification entity = ScheduledNotification.builder()
                .status(NotificationStatus.CANCELLED)
                .build();

        assertThrows(ScheduledNotificationValidationException.class,
                () -> validator.validateStatusTransition(entity, NotificationStatus.SCHEDULED));
    }

    @Test
    void testStatusTransition_ScheduledToReadForbidden() {
        ScheduledNotification entity = ScheduledNotification.builder()
                .status(NotificationStatus.SCHEDULED)
                .build();

        assertThrows(ScheduledNotificationValidationException.class,
                () -> validator.validateStatusTransition(entity, NotificationStatus.READ));
    }

    @Test
    void testStatusTransition_DeliveredToReadSuccess() {
        ScheduledNotification entity = ScheduledNotification.builder()
                .status(NotificationStatus.DELIVERED)
                .build();

        assertDoesNotThrow(() -> validator.validateStatusTransition(entity, NotificationStatus.READ));
    }

    @Test
    void testStatusTransition_ScheduledToDeliveredSuccess() {
        ScheduledNotification entity = ScheduledNotification.builder()
                .status(NotificationStatus.SCHEDULED)
                .build();

        assertDoesNotThrow(() -> validator.validateStatusTransition(entity, NotificationStatus.DELIVERED));
    }

    @Test
    void testStatusTransition_ScheduledToCancelledSuccess() {
        ScheduledNotification entity = ScheduledNotification.builder()
                .status(NotificationStatus.SCHEDULED)
                .build();

        assertDoesNotThrow(() -> validator.validateStatusTransition(entity, NotificationStatus.CANCELLED));
    }
}
