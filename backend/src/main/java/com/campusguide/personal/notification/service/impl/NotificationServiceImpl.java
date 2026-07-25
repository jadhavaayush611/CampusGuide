package com.campusguide.personal.notification.service.impl;

import com.campusguide.common.exception.ResourceNotFoundException;
import com.campusguide.common.exception.UnauthorisedException;
import com.campusguide.personal.notification.dto.request.CreateNotificationRequest;
import com.campusguide.personal.notification.dto.response.NotificationResponse;
import com.campusguide.personal.notification.entity.Notification;
import com.campusguide.personal.notification.enums.NotificationPriority;
import com.campusguide.personal.notification.enums.NotificationType;
import com.campusguide.personal.notification.exception.NotificationNotFoundException;
import com.campusguide.personal.notification.mapper.NotificationMapper;
import com.campusguide.personal.notification.repository.NotificationRepository;
import com.campusguide.personal.notification.service.interfaces.NotificationService;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public NotificationResponse createNotification(String userId, CreateNotificationRequest request) {
        log.info("Creating notification of type {} for user ID: {}", request.getType(), userId);
        Notification notification = notificationMapper.toEntity(request);
        notification.setUserId(userId);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        Notification saved = notificationRepository.save(notification);
        return notificationMapper.toResponse(saved);
    }

    @Override
    public NotificationResponse createNotification(String userId, String title, String message, 
                                                    NotificationType type, NotificationPriority priority, 
                                                    Map<String, Object> metadata) {
        log.info("Creating notification of type {} for user ID: {}", type, userId);
        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .priority(priority)
                .metadata(metadata != null ? new HashMap<>(metadata) : new HashMap<>())
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
        Notification saved = notificationRepository.save(notification);
        return notificationMapper.toResponse(saved);
    }

    @Override
    public Page<NotificationResponse> listNotifications(UserDetails userDetails, Pageable pageable) {
        User user = getUser(userDetails);
        log.info("Listing notifications for user ID: {}", user.getId());
        return notificationRepository.findByUserId(user.getId(), pageable)
                .map(notificationMapper::toResponse);
    }

    @Override
    public Page<NotificationResponse> listUnreadNotifications(UserDetails userDetails, Pageable pageable) {
        User user = getUser(userDetails);
        log.info("Listing unread notifications for user ID: {}", user.getId());
        return notificationRepository.findByUserIdAndRead(user.getId(), false, pageable)
                .map(notificationMapper::toResponse);
    }

    @Override
    public NotificationResponse markAsRead(UserDetails userDetails, String id) {
        User user = getUser(userDetails);
        log.info("Marking notification ID: {} as read for user ID: {}", id, user.getId());
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with ID: " + id));

        // Ownership validation
        if (!notification.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to mark this notification as read");
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
        }
        return notificationMapper.toResponse(notification);
    }

    @Override
    public void markAllAsRead(UserDetails userDetails) {
        User user = getUser(userDetails);
        log.info("Marking all notifications as read for user ID: {}", user.getId());
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndRead(user.getId(), false);
        LocalDateTime now = LocalDateTime.now();
        unreadNotifications.forEach(n -> {
            n.setRead(true);
            n.setReadAt(now);
        });
        notificationRepository.saveAll(unreadNotifications);
    }

    @Override
    public long countUnreadNotifications(UserDetails userDetails) {
        User user = getUser(userDetails);
        log.info("Counting unread notifications for user ID: {}", user.getId());
        return notificationRepository.countByUserIdAndRead(user.getId(), false);
    }

    @Override
    public void deleteNotification(UserDetails userDetails, String id) {
        User user = getUser(userDetails);
        log.info("Deleting notification ID: {} for user ID: {}", id, user.getId());
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with ID: " + id));

        // Ownership validation
        if (!notification.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to delete this notification");
        }

        notificationRepository.delete(notification);
    }

    @Override
    public boolean hasUnreadNotificationOfType(String userId, NotificationType type) {
        return notificationRepository.existsByUserIdAndTypeAndReadFalse(userId, type);
    }

    private User getUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));
    }
}
