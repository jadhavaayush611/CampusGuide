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

    List<CalendarEntry> findByUserIdOrderByStartTimeAscEndTimeAsc(String userId);

    Optional<CalendarEntry> findByIdAndUserId(UUID id, String userId);

    List<CalendarEntry> findByUserIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeAscEndTimeAsc(
            String userId, LocalDateTime startTimeBefore, LocalDateTime endTimeAfter);

    boolean existsByIdAndUserId(UUID id, String userId);
}
