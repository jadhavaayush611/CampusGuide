package com.campusguide.campus.event.repository;

import com.campusguide.campus.event.entity.Event;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends MongoRepository<Event, String> {

    List<Event> findByCouncilIdAndIsDeletedFalse(String councilId);

    List<Event> findByIsDeletedFalseAndIsCancelledFalseAndStartTimeGreaterThanEqualOrderByStartTimeAsc(LocalDateTime now);

    List<Event> findByIsDeletedFalseOrderByStartTimeAsc();

    List<Event> findByIsDeletedFalseAndStartTimeBeforeOrderByStartTimeDesc(LocalDateTime now);

    List<Event> findByOrganizerIdAndIsDeletedFalseOrderByStartTimeAsc(String organizerId);

    List<Event> findByIsDeletedFalseAndTitleContainingIgnoreCaseOrIsDeletedFalseAndDescriptionContainingIgnoreCaseOrIsDeletedFalseAndLocationContainingIgnoreCaseOrderByStartTimeAsc(
            String title, String description, String location);

    List<Event> findByIsDeletedFalseAndStartTimeBetweenOrderByStartTimeAsc(LocalDateTime startDate, LocalDateTime endDate);

    long countByIsDeletedFalse();

    long countByIsDeletedFalseAndIsCancelledFalseAndStartTimeGreaterThanEqual(LocalDateTime now);
}

