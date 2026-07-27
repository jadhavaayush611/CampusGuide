package com.campusguide.personal.calendar.service;

import com.campusguide.common.exception.UnauthorisedException;
import com.campusguide.personal.calendar.dto.CalendarEntryResponse;
import com.campusguide.personal.calendar.dto.CreateCalendarEntryRequest;
import com.campusguide.personal.calendar.dto.UpdateCalendarEntryRequest;
import com.campusguide.personal.calendar.entity.CalendarEntry;
import com.campusguide.personal.calendar.exception.CalendarEntryAccessDeniedException;
import com.campusguide.personal.calendar.exception.CalendarEntryNotFoundException;
import com.campusguide.personal.calendar.mapper.CalendarEntryMapper;
import com.campusguide.personal.calendar.repository.CalendarEntryRepository;
import com.campusguide.personal.calendar.validation.CalendarEntryValidator;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarEntryService {

    private final CalendarEntryRepository calendarEntryRepository;
    private final CalendarEntryMapper calendarEntryMapper;
    private final CalendarEntryValidator calendarEntryValidator;
    private final UserRepository userRepository;

    public CalendarEntryResponse createEntry(UserDetails userDetails, CreateCalendarEntryRequest request) {
        UUID userId = resolveUserId(userDetails);
        calendarEntryValidator.validateCreate(request);

        CalendarEntry entry = calendarEntryMapper.toEntity(request, userId);
        CalendarEntry savedEntry = calendarEntryRepository.save(entry);
        return calendarEntryMapper.toResponse(savedEntry);
    }

    public List<CalendarEntryResponse> getAllEntries(UserDetails userDetails) {
        UUID userId = resolveUserId(userDetails);
        List<CalendarEntry> entries = calendarEntryRepository.findByUserIdOrderByStartTimeAscEndTimeAsc(userId);
        return entries.stream()
                .map(calendarEntryMapper::toResponse)
                .collect(Collectors.toList());
    }

    public CalendarEntryResponse getEntryById(UserDetails userDetails, UUID id) {
        UUID userId = resolveUserId(userDetails);
        CalendarEntry entry = findAndVerifyOwnership(id, userId);
        return calendarEntryMapper.toResponse(entry);
    }

    public List<CalendarEntryResponse> getEntriesInRange(UserDetails userDetails, LocalDateTime from, LocalDateTime to) {
        UUID userId = resolveUserId(userDetails);
        calendarEntryValidator.validateRange(from, to);
        List<CalendarEntry> entries = calendarEntryRepository
                .findByUserIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeAscEndTimeAsc(userId, to, from);
        return entries.stream()
                .map(calendarEntryMapper::toResponse)
                .collect(Collectors.toList());
    }

    public CalendarEntryResponse updateEntry(UserDetails userDetails, UUID id, UpdateCalendarEntryRequest request) {
        UUID userId = resolveUserId(userDetails);
        CalendarEntry entry = findAndVerifyOwnership(id, userId);

        calendarEntryValidator.validateUpdate(request);

        entry.setTitle(request.getTitle());
        entry.setDescription(request.getDescription());
        entry.setType(request.getType());
        entry.setLinkedPlannerTaskId(request.getLinkedPlannerTaskId());
        entry.setLinkedEventId(request.getLinkedEventId());
        entry.setLocation(request.getLocation());
        entry.setStartTime(request.getStartTime());
        entry.setEndTime(request.getEndTime());
        entry.setAllDay(request.getIsAllDay() != null ? request.getIsAllDay() : false);
        entry.setColor(request.getColor());
        entry.setNotes(request.getNotes());
        entry.setUpdatedAt(LocalDateTime.now());

        CalendarEntry savedEntry = calendarEntryRepository.save(entry);
        return calendarEntryMapper.toResponse(savedEntry);
    }

    public void deleteEntry(UserDetails userDetails, UUID id) {
        UUID userId = resolveUserId(userDetails);
        CalendarEntry entry = findAndVerifyOwnership(id, userId);
        calendarEntryRepository.delete(entry);
    }

    public CalendarEntry findAndVerifyOwnership(UUID id, UUID userId) {
        CalendarEntry entry = calendarEntryRepository.findById(id)
                .orElseThrow(() -> new CalendarEntryNotFoundException("Calendar entry not found with id: " + id));

        if (!entry.getUserId().equals(userId)) {
            throw new CalendarEntryAccessDeniedException("User is not authorized to access this calendar entry");
        }

        return entry;
    }

    public UUID resolveUserId(UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseGet(() -> userRepository.findByUsername(userDetails.getUsername())
                        .orElseThrow(() -> new UnauthorisedException("User not found: " + userDetails.getUsername())));

        return parseUserId(user.getId());
    }

    private UUID parseUserId(String idStr) {
        if (idStr == null) {
            throw new UnauthorisedException("User ID is missing");
        }
        try {
            return UUID.fromString(idStr);
        } catch (IllegalArgumentException e) {
            return UUID.nameUUIDFromBytes(idStr.getBytes(StandardCharsets.UTF_8));
        }
    }
}
