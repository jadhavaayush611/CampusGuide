package com.campusguide.personal.calendar.repository;

import com.campusguide.personal.calendar.entity.CalendarEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CalendarEntryRepository extends MongoRepository<CalendarEntry, UUID> {

    List<CalendarEntry> findByUserIdOrderByStartTimeAscEndTimeAsc(UUID userId);

    Optional<CalendarEntry> findByIdAndUserId(UUID id, UUID userId);

    List<CalendarEntry> findByUserIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeAscEndTimeAsc(
            UUID userId, LocalDateTime startTimeBefore, LocalDateTime endTimeAfter);

    boolean existsByIdAndUserId(UUID id, UUID userId);
}
