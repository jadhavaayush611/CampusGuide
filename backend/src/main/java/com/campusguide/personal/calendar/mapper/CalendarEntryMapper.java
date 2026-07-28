package com.campusguide.personal.calendar.mapper;

import com.campusguide.personal.calendar.dto.CreateCalendarEntryRequest;
import com.campusguide.personal.calendar.dto.CalendarEntryResponse;
import com.campusguide.personal.calendar.entity.CalendarEntry;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Component
public class CalendarEntryMapper {

    public CalendarEntry toEntity(CreateCalendarEntryRequest request, String userId) {
        if (request == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        boolean isAllDay = request.getIsAllDay() != null ? request.getIsAllDay() : false;
        return CalendarEntry.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .linkedPlannerTaskId(request.getLinkedPlannerTaskId())
                .linkedEventId(request.getLinkedEventId())
                .location(request.getLocation())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isAllDay(isAllDay)
                .color(request.getColor())
                .notes(request.getNotes())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public CalendarEntryResponse toResponse(CalendarEntry entry) {
        if (entry == null) {
            return null;
        }
        LocalDateTime createdAt = entry.getCreatedAt() != null
                ? LocalDateTime.ofInstant(entry.getCreatedAt(), ZoneId.systemDefault())
                : null;
        LocalDateTime updatedAt = entry.getUpdatedAt() != null
                ? LocalDateTime.ofInstant(entry.getUpdatedAt(), ZoneId.systemDefault())
                : null;

        return CalendarEntryResponse.builder()
                .id(entry.getId())
                .userId(entry.getUserId())
                .title(entry.getTitle())
                .description(entry.getDescription())
                .type(entry.getType())
                .linkedPlannerTaskId(entry.getLinkedPlannerTaskId())
                .linkedEventId(entry.getLinkedEventId())
                .location(entry.getLocation())
                .startTime(entry.getStartTime())
                .endTime(entry.getEndTime())
                .isAllDay(entry.isAllDay())
                .color(entry.getColor())
                .notes(entry.getNotes())
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
