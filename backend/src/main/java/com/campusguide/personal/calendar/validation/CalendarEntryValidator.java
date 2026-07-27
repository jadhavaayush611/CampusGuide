package com.campusguide.personal.calendar.validation;

import com.campusguide.campus.event.repository.EventRepository;
import com.campusguide.common.exception.ResourceNotFoundException;
import com.campusguide.personal.calendar.dto.CreateCalendarEntryRequest;
import com.campusguide.personal.calendar.dto.UpdateCalendarEntryRequest;
import com.campusguide.personal.calendar.exception.CalendarEntryValidationException;
import com.campusguide.personal.planner.repository.PlannerTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CalendarEntryValidator {

    private final PlannerTaskRepository plannerTaskRepository;
    private final EventRepository eventRepository;

    public void validateCreate(CreateCalendarEntryRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new CalendarEntryValidationException("Title is mandatory");
        }
        if (request.getType() == null) {
            throw new CalendarEntryValidationException("Calendar entry type is mandatory");
        }

        validateTimeRange(request.getStartTime(), request.getEndTime());
        validateSingleReference(request.getLinkedPlannerTaskId(), request.getLinkedEventId());
        validateLinkedPlannerTask(request.getLinkedPlannerTaskId());
        validateLinkedEvent(request.getLinkedEventId());
    }

    public void validateUpdate(UpdateCalendarEntryRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new CalendarEntryValidationException("Title is mandatory");
        }
        if (request.getType() == null) {
            throw new CalendarEntryValidationException("Calendar entry type is mandatory");
        }

        validateTimeRange(request.getStartTime(), request.getEndTime());
        validateSingleReference(request.getLinkedPlannerTaskId(), request.getLinkedEventId());
        validateLinkedPlannerTask(request.getLinkedPlannerTaskId());
        validateLinkedEvent(request.getLinkedEventId());
    }

    public void validateRange(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            throw new CalendarEntryValidationException("from and to query parameters are required");
        }
        if (!from.isBefore(to)) {
            throw new CalendarEntryValidationException("from parameter must be before to parameter");
        }
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new CalendarEntryValidationException("startTime and endTime are mandatory");
        }
        if (!startTime.isBefore(endTime)) {
            throw new CalendarEntryValidationException("startTime must be before endTime");
        }
    }

    private void validateSingleReference(UUID linkedPlannerTaskId, UUID linkedEventId) {
        if (linkedPlannerTaskId != null && linkedEventId != null) {
            throw new CalendarEntryValidationException("Entry may reference either PlannerTask or Event, but never both simultaneously");
        }
    }

    private void validateLinkedPlannerTask(UUID linkedPlannerTaskId) {
        if (linkedPlannerTaskId != null) {
            if (!plannerTaskRepository.existsById(linkedPlannerTaskId)) {
                throw new ResourceNotFoundException("Planner task not found with id: " + linkedPlannerTaskId);
            }
        }
    }

    private void validateLinkedEvent(UUID linkedEventId) {
        if (linkedEventId != null) {
            if (!eventRepository.existsById(linkedEventId)) {
                throw new ResourceNotFoundException("Event not found with id: " + linkedEventId);
            }
        }
    }
}
