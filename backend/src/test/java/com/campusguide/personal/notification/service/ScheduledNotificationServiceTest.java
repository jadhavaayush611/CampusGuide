package com.campusguide.personal.notification.service;

import com.campusguide.common.exception.UnauthorisedException;
import com.campusguide.personal.notification.dto.CreateScheduledNotificationRequest;
import com.campusguide.personal.notification.dto.ScheduledNotificationResponse;
import com.campusguide.personal.notification.dto.UpdateNotificationStatusRequest;
import com.campusguide.personal.notification.dto.UpdateScheduledNotificationRequest;
import com.campusguide.personal.notification.entity.ScheduledNotification;
import com.campusguide.personal.notification.enums.NotificationChannel;
import com.campusguide.personal.notification.enums.NotificationPriority;
import com.campusguide.personal.notification.enums.NotificationStatus;
import com.campusguide.personal.notification.enums.NotificationType;
import com.campusguide.personal.notification.exception.ScheduledNotificationAccessDeniedException;
import com.campusguide.personal.notification.exception.ScheduledNotificationNotFoundException;
import com.campusguide.personal.notification.mapper.ScheduledNotificationMapper;
import com.campusguide.personal.notification.repository.ScheduledNotificationRepository;
import com.campusguide.personal.notification.validation.ScheduledNotificationValidator;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.campusguide.platform.user.service.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class ScheduledNotificationServiceTest {

    @Mock
    private ScheduledNotificationRepository repository;

    @Mock
    private ScheduledNotificationMapper mapper;

    @Mock
    private ScheduledNotificationValidator validator;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ScheduledNotificationService service;

    private UserDetails userDetails;
    private User user;
    private UUID userId;
    private UUID notificationId;
    private ScheduledNotification notification;
    private ScheduledNotificationResponse responseDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        notificationId = UUID.randomUUID();

        userDetails = org.springframework.security.core.userdetails.User.withUsername("test@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        user = User.builder()
                .id(userId.toString())
                .email("test@campusguide.com")
                .username("testuser")
                .build();

        lenient().when(currentUserService.getCurrentUserId(any())).thenReturn(userId.toString());

        notification = ScheduledNotification.builder()
                .id(notificationId)
                .userId(userId.toString())
                .title("Test Title")
                .message("Test Message")
                .type(NotificationType.REMINDER)
                .status(NotificationStatus.SCHEDULED)
                .scheduledFor(LocalDateTime.now().plusHours(1))
                .channel(NotificationChannel.IN_APP)
                .priority(NotificationPriority.NORMAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        responseDto = ScheduledNotificationResponse.builder()
                .id(notificationId)
                .userId(userId.toString())
                .title("Test Title")
                .message("Test Message")
                .type(NotificationType.REMINDER)
                .status(NotificationStatus.SCHEDULED)
                .scheduledFor(notification.getScheduledFor())
                .channel(NotificationChannel.IN_APP)
                .priority(NotificationPriority.NORMAL)
                .createdAt(notification.getCreatedAt() != null ? java.time.LocalDateTime.ofInstant(notification.getCreatedAt(), java.time.ZoneId.systemDefault()) : null)
                .updatedAt(notification.getUpdatedAt() != null ? java.time.LocalDateTime.ofInstant(notification.getUpdatedAt(), java.time.ZoneId.systemDefault()) : null)
                .build();
    }

    @Test
    void testCreateNotification_Success() {
        CreateScheduledNotificationRequest request = CreateScheduledNotificationRequest.builder()
                .title("Test Title")
                .message("Test Message")
                .type(NotificationType.REMINDER)
                .scheduledFor(LocalDateTime.now().plusHours(1))
                .channel(NotificationChannel.IN_APP)
                .priority(NotificationPriority.NORMAL)
                .build();


        when(mapper.toEntity(eq(request), eq(userId.toString()))).thenReturn(notification);
        when(repository.save(any(ScheduledNotification.class))).thenReturn(notification);
        when(mapper.toResponse(notification)).thenReturn(responseDto);

        ScheduledNotificationResponse result = service.createNotification(userDetails, request);

        assertNotNull(result);
        assertEquals(notificationId, result.getId());
        verify(validator).validateCreate(eq(request), any(LocalDateTime.class));
        verify(repository).save(any(ScheduledNotification.class));
    }

    @Test
    void testGetAllNotifications_Success() {

        when(repository.findByUserIdOrderByScheduledForAsc(userId.toString())).thenReturn(List.of(notification));
        when(mapper.toResponse(notification)).thenReturn(responseDto);

        List<ScheduledNotificationResponse> results = service.getAllNotifications(userDetails);

        assertEquals(1, results.size());
        assertEquals("Test Title", results.get(0).getTitle());
    }

    @Test
    void testGetNotificationById_Success() {

        when(repository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(mapper.toResponse(notification)).thenReturn(responseDto);

        ScheduledNotificationResponse result = service.getNotificationById(userDetails, notificationId);

        assertNotNull(result);
        assertEquals("Test Title", result.getTitle());
    }

    @Test
    void testGetNotificationById_NotFound() {

        when(repository.findById(notificationId)).thenReturn(Optional.empty());

        assertThrows(ScheduledNotificationNotFoundException.class, () -> service.getNotificationById(userDetails, notificationId));
    }

    @Test
    void testGetNotificationById_AccessDenied() {
        notification.setUserId(UUID.randomUUID().toString()); // different user

        when(repository.findById(notificationId)).thenReturn(Optional.of(notification));

        assertThrows(ScheduledNotificationAccessDeniedException.class, () -> service.getNotificationById(userDetails, notificationId));
    }

    @Test
    void testGetPendingNotifications_Success() {

        when(repository.findByUserIdAndStatusAndScheduledForLessThanEqualOrderByScheduledForAsc(eq(userId.toString()), eq(NotificationStatus.SCHEDULED), any(LocalDateTime.class)))
                .thenReturn(List.of(notification));
        when(mapper.toResponse(notification)).thenReturn(responseDto);

        List<ScheduledNotificationResponse> results = service.getPendingNotifications(userDetails);

        assertEquals(1, results.size());
        verify(repository).findByUserIdAndStatusAndScheduledForLessThanEqualOrderByScheduledForAsc(eq(userId.toString()), eq(NotificationStatus.SCHEDULED), any(LocalDateTime.class));
    }

    @Test
    void testUpdateNotificationStatus_TransitionToDelivered() {
        UpdateNotificationStatusRequest statusReq = new UpdateNotificationStatusRequest(NotificationStatus.DELIVERED);


        when(repository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(repository.save(any(ScheduledNotification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(ScheduledNotification.class))).thenReturn(responseDto);

        service.updateNotificationStatus(userDetails, notificationId, statusReq);

        assertEquals(NotificationStatus.DELIVERED, notification.getStatus());
        assertNotNull(notification.getDeliveredAt());
        verify(validator).validateStatusTransition(any(), eq(NotificationStatus.DELIVERED));
    }

    @Test
    void testUpdateNotificationStatus_TransitionToRead() {
        notification.setStatus(NotificationStatus.DELIVERED);
        notification.setDeliveredAt(LocalDateTime.now());
        UpdateNotificationStatusRequest statusReq = new UpdateNotificationStatusRequest(NotificationStatus.READ);


        when(repository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(repository.save(any(ScheduledNotification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(ScheduledNotification.class))).thenReturn(responseDto);

        service.updateNotificationStatus(userDetails, notificationId, statusReq);

        assertEquals(NotificationStatus.READ, notification.getStatus());
        assertNotNull(notification.getReadAt());
        verify(validator).validateStatusTransition(any(), eq(NotificationStatus.READ));
    }

    @Test
    void testUpdateNotification_Success() {
        UpdateScheduledNotificationRequest updateReq = UpdateScheduledNotificationRequest.builder()
                .title("Updated Title")
                .message("Updated Message")
                .type(NotificationType.ACADEMIC)
                .scheduledFor(LocalDateTime.now().plusHours(3))
                .channel(NotificationChannel.EMAIL)
                .priority(NotificationPriority.HIGH)
                .build();


        when(repository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(repository.save(any(ScheduledNotification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(ScheduledNotification.class))).thenReturn(responseDto);

        service.updateNotification(userDetails, notificationId, updateReq);

        assertEquals("Updated Title", notification.getTitle());
        assertEquals("Updated Message", notification.getMessage());
        assertEquals(NotificationType.ACADEMIC, notification.getType());
        assertEquals(NotificationChannel.EMAIL, notification.getChannel());
        assertEquals(NotificationPriority.HIGH, notification.getPriority());
        verify(validator).validateUpdate(eq(notification), eq(updateReq), any(LocalDateTime.class));
    }

    @Test
    void testDeleteNotification_Success() {

        when(repository.findById(notificationId)).thenReturn(Optional.of(notification));

        service.deleteNotification(userDetails, notificationId);

        verify(repository).delete(notification);
    }

    @Test
    void testResolveUserId_Unauthenticated() {
        when(currentUserService.getCurrentUserId(null)).thenThrow(new UnauthorisedException("User is not authenticated"));
        assertThrows(UnauthorisedException.class, () -> service.resolveUserId(null));
    }
}
