package com.campusguide.personal.calendar.controller;

import com.campusguide.personal.calendar.dto.CreateCalendarEntryRequest;
import com.campusguide.personal.calendar.dto.UpdateCalendarEntryRequest;
import com.campusguide.personal.calendar.entity.CalendarEntry;
import com.campusguide.personal.calendar.entity.CalendarEntryType;
import com.campusguide.personal.calendar.repository.CalendarEntryRepository;
import com.campusguide.platform.user.entity.Role;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class CalendarEntryControllerIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CalendarEntryRepository calendarEntryRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private User userEntity;
    private UserDetails userDetails;
    private UUID userId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        calendarEntryRepository.deleteAll();
        userRepository.deleteAll();

        userId = UUID.randomUUID();

        userEntity = User.builder()
                .id(userId.toString())
                .email("student@calendar.com")
                .username("calendarstudent")
                .password("password")
                .role(Role.STUDENT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userEntity = userRepository.save(userEntity);

        userDetails = org.springframework.security.core.userdetails.User.withUsername("student@calendar.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();
    }

    @AfterEach
    void tearDown() {
        calendarEntryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testCreateCalendarEntry_Success() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 8, 15, 10, 0);
        LocalDateTime end = start.plusHours(2);

        CreateCalendarEntryRequest request = CreateCalendarEntryRequest.builder()
                .title("Algorithms Lecture")
                .description("Chapter 4 Algorithms")
                .type(CalendarEntryType.ACADEMIC)
                .location("Hall B")
                .startTime(start)
                .endTime(end)
                .isAllDay(false)
                .color("#0000FF")
                .notes("Bring notebook")
                .build();

        mockMvc.perform(post("/api/v1/calendar")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.title").value("Algorithms Lecture"))
                .andExpect(jsonPath("$.type").value("ACADEMIC"))
                .andExpect(jsonPath("$.location").value("Hall B"))
                .andExpect(jsonPath("$.color").value("#0000FF"));
    }

    @Test
    void testGetAllCalendarEntries_Success() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 8, 15, 10, 0);

        CalendarEntry entry = CalendarEntry.builder()
                .id(UUID.randomUUID())
                .userId(userId.toString())
                .title("Entry 1")
                .type(CalendarEntryType.PERSONAL)
                .startTime(start)
                .endTime(start.plusHours(1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        calendarEntryRepository.save(entry);

        mockMvc.perform(get("/api/v1/calendar")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Entry 1"));
    }

    @Test
    void testGetCalendarEntryById_Success() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 8, 15, 10, 0);

        CalendarEntry entry = CalendarEntry.builder()
                .id(UUID.randomUUID())
                .userId(userId.toString())
                .title("Entry by ID")
                .type(CalendarEntryType.TASK)
                .startTime(start)
                .endTime(start.plusHours(1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        calendarEntryRepository.save(entry);

        mockMvc.perform(get("/api/v1/calendar/" + entry.getId())
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(entry.getId().toString()))
                .andExpect(jsonPath("$.title").value("Entry by ID"));
    }

    @Test
    void testGetCalendarEntriesInRange_Success() throws Exception {
        LocalDateTime base = LocalDateTime.of(2026, 8, 15, 10, 0);

        CalendarEntry entry1 = CalendarEntry.builder()
                .id(UUID.randomUUID())
                .userId(userId.toString())
                .title("Overlapping Entry")
                .type(CalendarEntryType.ACADEMIC)
                .startTime(base)
                .endTime(base.plusHours(2))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        CalendarEntry entry2 = CalendarEntry.builder()
                .id(UUID.randomUUID())
                .userId(userId.toString())
                .title("Out of Range Entry")
                .type(CalendarEntryType.PERSONAL)
                .startTime(base.plusDays(5))
                .endTime(base.plusDays(5).plusHours(2))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        calendarEntryRepository.save(entry1);
        calendarEntryRepository.save(entry2);

        String from = "2026-08-15T09:00:00";
        String to = "2026-08-15T13:00:00";

        mockMvc.perform(get("/api/v1/calendar/range")
                        .param("from", from)
                        .param("to", to)
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Overlapping Entry"));
    }

    @Test
    void testUpdateCalendarEntry_Success() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 8, 15, 10, 0);

        CalendarEntry entry = CalendarEntry.builder()
                .id(UUID.randomUUID())
                .userId(userId.toString())
                .title("Old Title")
                .type(CalendarEntryType.OTHER)
                .startTime(start)
                .endTime(start.plusHours(1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        calendarEntryRepository.save(entry);

        UpdateCalendarEntryRequest updateRequest = UpdateCalendarEntryRequest.builder()
                .title("Updated Title")
                .type(CalendarEntryType.PERSONAL)
                .startTime(start)
                .endTime(start.plusHours(2))
                .location("New Room")
                .build();

        mockMvc.perform(put("/api/v1/calendar/" + entry.getId())
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.location").value("New Room"));
    }

    @Test
    void testDeleteCalendarEntry_Success() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 8, 15, 10, 0);

        CalendarEntry entry = CalendarEntry.builder()
                .id(UUID.randomUUID())
                .userId(userId.toString())
                .title("Entry to Delete")
                .type(CalendarEntryType.OTHER)
                .startTime(start)
                .endTime(start.plusHours(1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        calendarEntryRepository.save(entry);

        mockMvc.perform(delete("/api/v1/calendar/" + entry.getId())
                        .with(user(userDetails)))
                .andExpect(status().isNoContent());

        assertFalse(calendarEntryRepository.findById(entry.getId()).isPresent());
    }
}
