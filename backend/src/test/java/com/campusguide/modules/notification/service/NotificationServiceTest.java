package com.campusguide.modules.notification.service;

import com.campusguide.exception.ResourceNotFoundException;
import com.campusguide.modules.notification.dto.request.CreateNotificationRequest;
import com.campusguide.modules.notification.dto.response.NotificationResponse;
import com.campusguide.modules.notification.entity.Notification;
import com.campusguide.modules.notification.enums.NotificationPriority;
import com.campusguide.modules.notification.enums.NotificationType;
import com.campusguide.modules.notification.exception.NotificationNotFoundException;
import com.campusguide.modules.notification.mapper.NotificationMapper;
import com.campusguide.modules.notification.repository.NotificationRepository;
import com.campusguide.modules.notification.service.impl.NotificationServiceImpl;
import com.campusguide.modules.user.entity.User;
import com.campusguide.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private UserDetails userDetails;
    private UserDetails otherUserDetails;

    private User user;
    private User otherUser;

    private Notification notification;
    private NotificationResponse notificationResponse;

    @BeforeEach
    void setUp() {
        userDetails = org.springframework.security.core.userdetails.User.withUsername("test@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        otherUserDetails = org.springframework.security.core.userdetails.User.withUsername("other@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        user = User.builder()
                .id("user-123")
                .email("test@campusguide.com")
                .build();

        otherUser = User.builder()
                .id("user-456")
                .email("other@campusguide.com")
                .build();

        notification = Notification.builder()
                .id("noti-123")
                .userId("user-123")
                .title("Test Notification")
                .message("Test message")
                .type(NotificationType.ACADEMIC)
                .priority(NotificationPriority.NORMAL)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationResponse = NotificationResponse.builder()
                .id("noti-123")
                .title("Test Notification")
                .message("Test message")
                .type(NotificationType.ACADEMIC)
                .priority(NotificationPriority.NORMAL)
                .read(false)
                .createdAt(notification.getCreatedAt())
                .build();
    }

    @Test
    void testCreateNotification_Request() {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .title("Test Notification")
                .message("Test message")
                .type(NotificationType.ACADEMIC)
                .priority(NotificationPriority.NORMAL)
                .build();

        when(notificationMapper.toEntity(request)).thenReturn(notification);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

        NotificationResponse result = notificationService.createNotification("user-123", request);

        assertNotNull(result);
        assertEquals("Test Notification", result.getTitle());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testCreateNotification_Fields() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

        NotificationResponse result = notificationService.createNotification(
                "user-123",
                "Test Notification",
                "Test message",
                NotificationType.ACADEMIC,
                NotificationPriority.NORMAL,
                null
        );

        assertNotNull(result);
        assertEquals("Test Notification", result.getTitle());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testMarkAsRead_Success() {
        when(userRepository.findByEmail(userDetails.getUsername())).thenReturn(Optional.of(user));
        when(notificationRepository.findById("noti-123")).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationMapper.toResponse(any(Notification.class))).thenReturn(notificationResponse);

        NotificationResponse result = notificationService.markAsRead(userDetails, "noti-123");

        assertNotNull(result);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testMarkAsRead_NotFound() {
        when(userRepository.findByEmail(userDetails.getUsername())).thenReturn(Optional.of(user));
        when(notificationRepository.findById("noti-123")).thenReturn(Optional.empty());

        assertThrows(NotificationNotFoundException.class, () -> {
            notificationService.markAsRead(userDetails, "noti-123");
        });
    }

    @Test
    void testMarkAsRead_OwnershipValidationFailed() {
        notification.setUserId("user-456"); // mismatch

        when(userRepository.findByEmail(userDetails.getUsername())).thenReturn(Optional.of(user));
        when(notificationRepository.findById("noti-123")).thenReturn(Optional.of(notification));

        assertThrows(AccessDeniedException.class, () -> {
            notificationService.markAsRead(userDetails, "noti-123");
        });
    }

    @Test
    void testMarkAllAsRead() {
        when(userRepository.findByEmail(userDetails.getUsername())).thenReturn(Optional.of(user));
        when(notificationRepository.findByUserIdAndRead("user-123", false)).thenReturn(List.of(notification));

        notificationService.markAllAsRead(userDetails);

        verify(notificationRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testCountUnreadNotifications() {
        when(userRepository.findByEmail(userDetails.getUsername())).thenReturn(Optional.of(user));
        when(notificationRepository.countByUserIdAndRead("user-123", false)).thenReturn(5L);

        long count = notificationService.countUnreadNotifications(userDetails);

        assertEquals(5L, count);
    }

    @Test
    void testDeleteNotification_Success() {
        when(userRepository.findByEmail(userDetails.getUsername())).thenReturn(Optional.of(user));
        when(notificationRepository.findById("noti-123")).thenReturn(Optional.of(notification));

        notificationService.deleteNotification(userDetails, "noti-123");

        verify(notificationRepository, times(1)).delete(notification);
    }

    @Test
    void testDeleteNotification_OwnershipValidationFailed() {
        notification.setUserId("user-456"); // mismatch

        when(userRepository.findByEmail(userDetails.getUsername())).thenReturn(Optional.of(user));
        when(notificationRepository.findById("noti-123")).thenReturn(Optional.of(notification));

        assertThrows(AccessDeniedException.class, () -> {
            notificationService.deleteNotification(userDetails, "noti-123");
        });
    }
}
