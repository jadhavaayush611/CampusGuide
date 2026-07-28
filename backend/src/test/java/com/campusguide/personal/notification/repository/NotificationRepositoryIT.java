package com.campusguide.personal.notification.repository;

import com.campusguide.personal.notification.entity.Notification;
import com.campusguide.personal.notification.enums.NotificationPriority;
import com.campusguide.personal.notification.enums.NotificationType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NotificationRepositoryIT {

    @Autowired
    private NotificationRepository notificationRepository;

    private final String userId = "user-123";
    private final String otherUserId = "user-456";

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();

        // 3 notifications for user-123
        Notification n1 = Notification.builder()
                .userId(userId)
                .title("Low Unread")
                .message("Message 1")
                .type(NotificationType.ACADEMIC)
                .priority(NotificationPriority.LOW)
                .read(false)
                .createdAt(java.time.Instant.now().minus(3, java.time.temporal.ChronoUnit.DAYS))
                .build();

        Notification n2 = Notification.builder()
                .userId(userId)
                .title("High Read")
                .message("Message 2")
                .type(NotificationType.SYSTEM)
                .priority(NotificationPriority.HIGH)
                .read(true)
                .createdAt(java.time.Instant.now().minus(2, java.time.temporal.ChronoUnit.DAYS))
                .readAt(java.time.Instant.now().minus(1, java.time.temporal.ChronoUnit.DAYS))
                .build();

        Notification n3 = Notification.builder()
                .userId(userId)
                .title("Normal Unread")
                .message("Message 3")
                .type(NotificationType.AI)
                .priority(NotificationPriority.NORMAL)
                .read(false)
                .createdAt(java.time.Instant.now().minus(1, java.time.temporal.ChronoUnit.DAYS))
                .build();

        // 1 notification for otherUserId
        Notification n4 = Notification.builder()
                .userId(otherUserId)
                .title("Other User Notification")
                .message("Message 4")
                .type(NotificationType.EVENT)
                .priority(NotificationPriority.NORMAL)
                .read(false)
                .createdAt(java.time.Instant.now())
                .build();

        notificationRepository.save(n1);
        try { Thread.sleep(20); } catch (InterruptedException ignored) {}
        notificationRepository.save(n2);
        try { Thread.sleep(20); } catch (InterruptedException ignored) {}
        notificationRepository.save(n3);
        try { Thread.sleep(20); } catch (InterruptedException ignored) {}
        notificationRepository.save(n4);
    }

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAll();
    }

    @Test
    void testUserRetrieval() {
        Page<Notification> page = notificationRepository.findByUserId(userId, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));
        assertEquals(3, page.getTotalElements());
        // Verify newest first
        assertEquals("Normal Unread", page.getContent().get(0).getTitle());
        assertEquals("High Read", page.getContent().get(1).getTitle());
        assertEquals("Low Unread", page.getContent().get(2).getTitle());
    }

    @Test
    void testUnreadRetrieval() {
        Page<Notification> page = notificationRepository.findByUserIdAndRead(userId, false, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));
        assertEquals(2, page.getTotalElements());
        // Verify newest first
        assertEquals("Normal Unread", page.getContent().get(0).getTitle());
        assertEquals("Low Unread", page.getContent().get(1).getTitle());
    }

    @Test
    void testCountQueries() {
        long totalCount = notificationRepository.countByUserId(userId);
        long unreadCount = notificationRepository.countByUserIdAndRead(userId, false);
        long readCount = notificationRepository.countByUserIdAndRead(userId, true);

        assertEquals(3, totalCount);
        assertEquals(2, unreadCount);
        assertEquals(1, readCount);
    }
}
