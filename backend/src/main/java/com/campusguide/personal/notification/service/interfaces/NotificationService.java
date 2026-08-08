package com.campusguide.personal.notification.service.interfaces;

import com.campusguide.personal.notification.dto.request.CreateNotificationRequest;
import com.campusguide.personal.notification.dto.response.NotificationResponse;
import com.campusguide.personal.notification.enums.NotificationPriority;
import com.campusguide.personal.notification.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

public interface NotificationService {

    NotificationResponse createNotification(String userId, CreateNotificationRequest request);

    NotificationResponse createNotification(String userId, String title, String message, 
                                            NotificationType type, NotificationPriority priority, 
                                            Map<String, Object> metadata);

    void createNotificationAsync(String userId, String title, String message, 
                                 NotificationType type, NotificationPriority priority, 
                                 Map<String, Object> metadata);

    Page<NotificationResponse> listNotifications(UserDetails userDetails, Pageable pageable);

    Page<NotificationResponse> listUnreadNotifications(UserDetails userDetails, Pageable pageable);

    NotificationResponse markAsRead(UserDetails userDetails, String id);

    void markAllAsRead(UserDetails userDetails);

    long countUnreadNotifications(UserDetails userDetails);

    void deleteNotification(UserDetails userDetails, String id);

    boolean hasUnreadNotificationOfType(String userId, NotificationType type);
}

