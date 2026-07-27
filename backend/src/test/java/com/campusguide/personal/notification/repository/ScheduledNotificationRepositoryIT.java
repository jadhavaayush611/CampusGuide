package com.campusguide.personal.notification.repository;

import com.campusguide.personal.notification.entity.ScheduledNotification;
import com.campusguide.personal.notification.enums.NotificationChannel;
import com.campusguide.personal.notification.enums.NotificationPriority;
import com.campusguide.personal.notification.enums.NotificationStatus;
import com.campusguide.personal.notification.enums.NotificationType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ScheduledNotificationRepositoryIT {

    @Autowired
    private ScheduledNotificationRepository repository;

    private UUID userId1;
    private UUID userId2;
    private ScheduledNotification notif1;
    private ScheduledNotification notif2;
    private ScheduledNotification pendingNotif;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        userId1 = UUID.randomUUID();
        userId2 = UUID.randomUUID();

        LocalDateTime now = LocalDateTime.now();

        notif1 = ScheduledNotification.builder()
                .id(UUID.randomUUID())
                .userId(userId1)
                .title("Reminder 1")
                .message("Message 1")
                .type(NotificationType.REMINDER)
                .status(NotificationStatus.SCHEDULED)
                .scheduledFor(now.plusHours(2))
                .channel(NotificationChannel.IN_APP)
                .priority(NotificationPriority.HIGH)
                .createdAt(now)
                .updatedAt(now)
                .build();

        notif2 = ScheduledNotification.builder()
                .id(UUID.randomUUID())
                .userId(userId1)
                .title("Reminder 2")
                .message("Message 2")
                .type(NotificationType.ACADEMIC)
                .status(NotificationStatus.SCHEDULED)
                .scheduledFor(now.plusHours(1))
                .channel(NotificationChannel.EMAIL)
                .priority(NotificationPriority.NORMAL)
                .createdAt(now)
                .updatedAt(now)
                .build();

        pendingNotif = ScheduledNotification.builder()
                .id(UUID.randomUUID())
                .userId(userId1)
                .title("Pending Reminder")
                .message("Past due pending notification")
                .type(NotificationType.EVENT)
                .status(NotificationStatus.SCHEDULED)
                .scheduledFor(now.minusMinutes(10))
                .channel(NotificationChannel.PUSH)
                .priority(NotificationPriority.HIGH)
                .createdAt(now.minusHours(1))
                .updatedAt(now.minusHours(1))
                .build();

        repository.saveAll(List.of(notif1, notif2, pendingNotif));
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    void testSaveAndFindById() {
        Optional<ScheduledNotification> found = repository.findById(notif1.getId());
        assertTrue(found.isPresent());
        assertEquals("Reminder 1", found.get().getTitle());
        assertEquals(userId1, found.get().getUserId());
    }

    @Test
    void testFindByUserIdOrderByScheduledForAsc() {
        List<ScheduledNotification> list = repository.findByUserIdOrderByScheduledForAsc(userId1);
        assertEquals(3, list.size());
        // minus 10m -> pendingNotif, plus 1h -> notif2, plus 2h -> notif1
        assertEquals(pendingNotif.getId(), list.get(0).getId());
        assertEquals(notif2.getId(), list.get(1).getId());
        assertEquals(notif1.getId(), list.get(2).getId());
    }

    @Test
    void testFindByIdAndUserId() {
        Optional<ScheduledNotification> found = repository.findByIdAndUserId(notif1.getId(), userId1);
        assertTrue(found.isPresent());

        Optional<ScheduledNotification> notFoundOtherUser = repository.findByIdAndUserId(notif1.getId(), userId2);
        assertFalse(notFoundOtherUser.isPresent());
    }

    @Test
    void testFindPendingNotifications() {
        LocalDateTime now = LocalDateTime.now();
        List<ScheduledNotification> pendingList = repository
                .findByUserIdAndStatusAndScheduledForLessThanEqualOrderByScheduledForAsc(userId1, NotificationStatus.SCHEDULED, now);

        assertEquals(1, pendingList.size());
        assertEquals(pendingNotif.getId(), pendingList.get(0).getId());
    }

    @Test
    void testDeleteNotification() {
        repository.deleteById(notif1.getId());
        Optional<ScheduledNotification> found = repository.findById(notif1.getId());
        assertFalse(found.isPresent());
    }
}
