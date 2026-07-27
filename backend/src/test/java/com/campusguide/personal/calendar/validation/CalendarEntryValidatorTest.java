package com.campusguide.personal.calendar.validation;

import com.campusguide.campus.event.repository.EventRepository;
import com.campusguide.common.exception.ResourceNotFoundException;
import com.campusguide.personal.calendar.dto.CreateCalendarEntryRequest;
import com.campusguide.personal.calendar.dto.UpdateCalendarEntryRequest;
import com.campusguide.personal.calendar.entity.CalendarEntryType;
import com.campusguide.personal.calendar.exception.CalendarEntryValidationException;
import com.campusguide.personal.planner.repository.PlannerTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalendarEntryValidatorTest {

    @Mock
    private PlannerTaskRepository plannerTaskRepository;

    @Mock
    private EventRepository eventRepository;

    private CalendarEntryValidator validator;

    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        validator = new CalendarEntryValidator(plannerTaskRepository, eventRepository);
        start = LocalDateTime.now().plusDays(1);
        end = start.plusHours(2);
    }

    @Test
    void validateCreate_Success() {
        CreateCalendarEntryRequest request = CreateCalendarEntryRequest.builder()
                .title("Valid Title")
                .type(CalendarEntryType.PERSONAL)
                .startTime(start)
                .endTime(end)
                .build();

        assertDoesNotThrow(() -> validator.validateCreate(request));
    }

    @Test
    void validateCreate_BlankTitle_ThrowsException() {
        CreateCalendarEntryRequest request = CreateCalendarEntryRequest.builder()
                .title("  ")
                .type(CalendarEntryType.PERSONAL)
                .startTime(start)
                .endTime(end)
                .build();

        assertThrows(CalendarEntryValidationException.class, () -> validator.validateCreate(request));
    }

    @Test
    void validateCreate_NullType_ThrowsException() {
        CreateCalendarEntryRequest request = CreateCalendarEntryRequest.builder()
                .title("Title")
                .type(null)
                .startTime(start)
                .endTime(end)
                .build();

        assertThrows(CalendarEntryValidationException.class, () -> validator.validateCreate(request));
    }

    @Test
    void validateCreate_StartTimeAfterEndTime_ThrowsException() {
        CreateCalendarEntryRequest request = CreateCalendarEntryRequest.builder()
                .title("Title")
                .type(CalendarEntryType.PERSONAL)
                .startTime(end)
                .endTime(start)
                .build();

        assertThrows(CalendarEntryValidationException.class, () -> validator.validateCreate(request));
    }

    @Test
    void validateCreate_BothReferencesPresent_ThrowsException() {
        UUID taskId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        CreateCalendarEntryRequest request = CreateCalendarEntryRequest.builder()
                .title("Title")
                .type(CalendarEntryType.PERSONAL)
                .startTime(start)
                .endTime(end)
                .linkedPlannerTaskId(taskId)
                .linkedEventId(eventId)
                .build();

        CalendarEntryValidationException ex = assertThrows(CalendarEntryValidationException.class,
                () -> validator.validateCreate(request));
        assertTrue(ex.getMessage().contains("never both simultaneously"));
    }

    @Test
    void validateCreate_NonExistentPlannerTask_ThrowsResourceNotFoundException() {
        UUID taskId = UUID.randomUUID();
        when(plannerTaskRepository.existsById(taskId)).thenReturn(false);

        CreateCalendarEntryRequest request = CreateCalendarEntryRequest.builder()
                .title("Title")
                .type(CalendarEntryType.TASK)
                .startTime(start)
                .endTime(end)
                .linkedPlannerTaskId(taskId)
                .build();

        assertThrows(ResourceNotFoundException.class, () -> validator.validateCreate(request));
    }

    @Test
    void validateCreate_NonExistentEvent_ThrowsResourceNotFoundException() {
        UUID eventId = UUID.randomUUID();
        when(eventRepository.existsById(eventId)).thenReturn(false);

        CreateCalendarEntryRequest request = CreateCalendarEntryRequest.builder()
                .title("Title")
                .type(CalendarEntryType.EVENT)
                .startTime(start)
                .endTime(end)
                .linkedEventId(eventId)
                .build();

        assertThrows(ResourceNotFoundException.class, () -> validator.validateCreate(request));
    }

    @Test
    void validateUpdate_Success() {
        UpdateCalendarEntryRequest request = UpdateCalendarEntryRequest.builder()
                .title("Updated Title")
                .type(CalendarEntryType.ACADEMIC)
                .startTime(start)
                .endTime(end)
                .build();

        assertDoesNotThrow(() -> validator.validateUpdate(request));
    }

    @Test
    void validateRange_Success() {
        assertDoesNotThrow(() -> validator.validateRange(start, end));
    }

    @Test
    void validateRange_InvalidOrder_ThrowsException() {
        assertThrows(CalendarEntryValidationException.class, () -> validator.validateRange(end, start));
    }
}
