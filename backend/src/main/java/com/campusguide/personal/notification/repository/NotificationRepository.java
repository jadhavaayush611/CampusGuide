package com.campusguide.personal.notification.repository;

import com.campusguide.personal.notification.entity.Notification;
import com.campusguide.personal.notification.enums.NotificationType;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    Page<Notification> findByUserId(String userId, Pageable pageable);

    Page<Notification> findByUserIdAndRead(String userId, boolean read, Pageable pageable);

    List<Notification> findByUserIdAndRead(String userId, boolean read);

    boolean existsByUserIdAndTypeAndReadFalse(String userId, NotificationType type);

    long countByUserIdAndRead(String userId, boolean read);


    long countByUserId(String userId);

    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);

    Optional<Notification> findByIdAndUserId(String id, String userId);
}
