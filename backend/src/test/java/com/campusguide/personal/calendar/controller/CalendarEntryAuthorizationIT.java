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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class CalendarEntryAuthorizationIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CalendarEntryRepository calendarEntryRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private User user1;
    private User user2;
    private UserDetails userDetails1;
    private UserDetails userDetails2;
    private CalendarEntry user2Entry;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        calendarEntryRepository.deleteAll();
        userRepository.deleteAll();

        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();

        user1 = User.builder()
                .id(user1Id.toString())
                .email("user1@calendar.com")
                .username("user1calendar")
                .password("password")
                .role(Role.STUDENT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        user1 = userRepository.save(user1);

        user2 = User.builder()
                .id(user2Id.toString())
                .email("user2@calendar.com")
                .username("user2calendar")
                .password("password")
                .role(Role.STUDENT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        user2 = userRepository.save(user2);

        userDetails1 = org.springframework.security.core.userdetails.User.withUsername("user1@calendar.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        userDetails2 = org.springframework.security.core.userdetails.User.withUsername("user2@calendar.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);

        user2Entry = CalendarEntry.builder()
                .id(UUID.randomUUID())
                .userId(user2.getId())
                .title("User 2 Private Entry")
                .type(CalendarEntryType.PERSONAL)
                .startTime(start)
                .endTime(end)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        user2Entry = calendarEntryRepository.save(user2Entry);
    }

    @AfterEach
    void tearDown() {
        calendarEntryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void unauthenticatedRequest_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/calendar"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/calendar/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/calendar/range?from=2026-08-01T00:00:00&to=2026-08-02T00:00:00"))
                .andExpect(status().isUnauthorized());

        CreateCalendarEntryRequest createRequest = CreateCalendarEntryRequest.builder()
                .title("Unauth Entry")
                .type(CalendarEntryType.PERSONAL)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .build();

        mockMvc.perform(post("/api/v1/calendar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessingOtherUserEntry_Returns403() throws Exception {
        // User 1 tries to access User 2's entry
        mockMvc.perform(get("/api/v1/calendar/" + user2Entry.getId())
                        .with(user(userDetails1)))
                .andExpect(status().isForbidden());

        UpdateCalendarEntryRequest updateRequest = UpdateCalendarEntryRequest.builder()
                .title("Hacked Title")
                .type(CalendarEntryType.PERSONAL)
                .startTime(user2Entry.getStartTime())
                .endTime(user2Entry.getEndTime())
                .build();

        mockMvc.perform(put("/api/v1/calendar/" + user2Entry.getId())
                        .with(user(userDetails1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/v1/calendar/" + user2Entry.getId())
                        .with(user(userDetails1)))
                .andExpect(status().isForbidden());
    }
}
