package com.campusguide.personal.notification.repository;

import com.campusguide.personal.notification.entity.ScheduledNotification;
import com.campusguide.personal.notification.enums.NotificationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScheduledNotificationRepository extends MongoRepository<ScheduledNotification, UUID> {

    List<ScheduledNotification> findByUserIdOrderByScheduledForAsc(String userId);

    Optional<ScheduledNotification> findByIdAndUserId(UUID id, String userId);

    List<ScheduledNotification> findByUserIdAndStatusAndScheduledForLessThanEqualOrderByScheduledForAsc(
            String userId, NotificationStatus status, LocalDateTime now);

    List<ScheduledNotification> findByStatusAndScheduledForLessThanEqualOrderByScheduledForAsc(
            NotificationStatus status, LocalDateTime now);
}
