package com.campusguide.campus.event.repository;

import com.campusguide.campus.event.entity.Event;
import com.campusguide.campus.event.entity.EventStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends MongoRepository<Event, UUID> {

    Optional<Event> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    List<Event> findByCouncilId(UUID councilId);

    List<Event> findByStatusAndEndTimeGreaterThanEqualOrderByStartTimeAsc(EventStatus status, LocalDateTime now);

    List<Event> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrVenueContainingIgnoreCaseOrderByStartTimeAsc(
            String titleQuery, String descQuery, String venueQuery);

    long countByStatus(EventStatus status);

    long countByStatusAndEndTimeGreaterThanEqual(EventStatus status, LocalDateTime now);

    boolean existsByCouncilId(UUID councilId);
}
