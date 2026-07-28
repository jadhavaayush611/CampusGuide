package com.campusguide.personal.calendar.service;

import com.campusguide.campus.event.repository.EventRepository;
import com.campusguide.personal.calendar.dto.CalendarEntryResponse;
import com.campusguide.personal.calendar.dto.CreateCalendarEntryRequest;
import com.campusguide.personal.calendar.dto.UpdateCalendarEntryRequest;
import com.campusguide.personal.calendar.entity.CalendarEntry;
import com.campusguide.personal.calendar.entity.CalendarEntryType;
import com.campusguide.personal.calendar.exception.CalendarEntryAccessDeniedException;
import com.campusguide.personal.calendar.exception.CalendarEntryNotFoundException;
import com.campusguide.personal.calendar.mapper.CalendarEntryMapper;
import com.campusguide.personal.calendar.repository.CalendarEntryRepository;
import com.campusguide.personal.calendar.validation.CalendarEntryValidator;
import com.campusguide.personal.planner.repository.PlannerTaskRepository;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
import com.campusguide.platform.user.service.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarEntryServiceTest {

    @Mock
    private CalendarEntryRepository calendarEntryRepository;

    @Spy
    private CalendarEntryMapper calendarEntryMapper = new CalendarEntryMapper();

    @Mock
    private PlannerTaskRepository plannerTaskRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CurrentUserService currentUserService;

    private CalendarEntryValidator calendarEntryValidator;
    private CalendarEntryService calendarEntryService;

    private User testUser;
    private UserDetails userDetails;
    private UUID userId;
    private UUID entryId;
    private CalendarEntry existingEntry;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        calendarEntryValidator = new CalendarEntryValidator(plannerTaskRepository, eventRepository);
        calendarEntryService = new CalendarEntryService(calendarEntryRepository, calendarEntryMapper, calendarEntryValidator, currentUserService);

        userId = UUID.randomUUID();
        entryId = UUID.randomUUID();
        start = LocalDateTime.now().plusDays(1);
        end = start.plusHours(2);

        testUser = User.builder()
                .id(userId.toString())
                .email("student@calendar.com")
                .username("calendaruser")
                .build();

        userDetails = org.springframework.security.core.userdetails.User.withUsername("student@calendar.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        lenient().when(currentUserService.getCurrentUserId(any())).thenReturn(userId.toString());

        existingEntry = CalendarEntry.builder()
                .id(entryId)
                .userId(userId.toString())
                .title("Sample Entry")
                .description("Sample Description")
                .type(CalendarEntryType.PERSONAL)
                .startTime(start)
                .endTime(end)
                .isAllDay(false)
                .createdAt(LocalDateTime.now().minusHours(1))
                .updatedAt(LocalDateTime.now().minusHours(1))
                .build();
    }

    @Test
    void createEntry_Success() {

        when(calendarEntryRepository.save(any(CalendarEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateCalendarEntryRequest request = CreateCalendarEntryRequest.builder()
                .title("New Entry")
                .type(CalendarEntryType.PERSONAL)
                .startTime(start)
                .endTime(end)
                .build();

        CalendarEntryResponse response = calendarEntryService.createEntry(userDetails, request);

        assertNotNull(response);
        assertEquals("New Entry", response.getTitle());
        assertEquals(userId.toString(), response.getUserId());
        verify(calendarEntryRepository, times(1)).save(any(CalendarEntry.class));
    }

    @Test
    void getAllEntries_Success() {

        when(calendarEntryRepository.findByUserIdOrderByStartTimeAscEndTimeAsc(userId.toString()))
                .thenReturn(List.of(existingEntry));

        List<CalendarEntryResponse> responses = calendarEntryService.getAllEntries(userDetails);

        assertEquals(1, responses.size());
        assertEquals("Sample Entry", responses.get(0).getTitle());
    }

    @Test
    void getEntryById_Success() {

        when(calendarEntryRepository.findById(entryId)).thenReturn(Optional.of(existingEntry));

        CalendarEntryResponse response = calendarEntryService.getEntryById(userDetails, entryId);

        assertNotNull(response);
        assertEquals(entryId, response.getId());
    }

    @Test
    void getEntryById_NotFound_ThrowsException() {

        when(calendarEntryRepository.findById(entryId)).thenReturn(Optional.empty());

        assertThrows(CalendarEntryNotFoundException.class, () -> calendarEntryService.getEntryById(userDetails, entryId));
    }

    @Test
    void getEntryById_AccessDenied_ThrowsException() {

        UUID otherUserId = UUID.randomUUID();
        existingEntry.setUserId(otherUserId.toString());
        when(calendarEntryRepository.findById(entryId)).thenReturn(Optional.of(existingEntry));

        assertThrows(CalendarEntryAccessDeniedException.class, () -> calendarEntryService.getEntryById(userDetails, entryId));
    }

    @Test
    void getEntriesInRange_Success() {

        LocalDateTime rangeStart = start.minusHours(1);
        LocalDateTime rangeEnd = end.plusHours(1);
        when(calendarEntryRepository.findByUserIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeAscEndTimeAsc(userId.toString(), rangeEnd, rangeStart))
                .thenReturn(List.of(existingEntry));

        List<CalendarEntryResponse> responses = calendarEntryService.getEntriesInRange(userDetails, rangeStart, rangeEnd);

        assertEquals(1, responses.size());
        assertEquals("Sample Entry", responses.get(0).getTitle());
    }

    @Test
    void updateEntry_Success() {

        when(calendarEntryRepository.findById(entryId)).thenReturn(Optional.of(existingEntry));
        when(calendarEntryRepository.save(any(CalendarEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateCalendarEntryRequest request = UpdateCalendarEntryRequest.builder()
                .title("Updated Entry")
                .type(CalendarEntryType.ACADEMIC)
                .startTime(start)
                .endTime(end)
                .build();

        CalendarEntryResponse response = calendarEntryService.updateEntry(userDetails, entryId, request);

        assertNotNull(response);
        assertEquals("Updated Entry", response.getTitle());
        assertEquals(CalendarEntryType.ACADEMIC, response.getType());
    }

    @Test
    void deleteEntry_Success() {

        when(calendarEntryRepository.findById(entryId)).thenReturn(Optional.of(existingEntry));

        assertDoesNotThrow(() -> calendarEntryService.deleteEntry(userDetails, entryId));

        verify(calendarEntryRepository, times(1)).delete(existingEntry);
    }
}
