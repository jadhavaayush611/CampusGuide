package com.campusguide.modules.event.controller;

import com.campusguide.modules.event.entity.Event;
import com.campusguide.modules.event.repository.EventRepository;
import com.campusguide.modules.user.entity.Role;
import com.campusguide.modules.user.entity.User;
import com.campusguide.modules.user.repository.UserRepository;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class EventRegistrationControllerSecurityIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    private User studentUser;
    private User adminUser;

    private UserDetails studentDetails;
    private UserDetails adminDetails;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        // Save users
        studentUser = User.builder()
                .email("student@campusguide.com")
                .password("password")
                .role(Role.STUDENT)
                .firstName("Student")
                .lastName("User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        studentUser = userRepository.save(studentUser);

        adminUser = User.builder()
                .email("admin@campusguide.com")
                .password("password")
                .role(Role.SUPER_ADMIN)
                .firstName("Admin")
                .lastName("User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        adminUser = userRepository.save(adminUser);

        // Build UserDetails
        studentDetails = org.springframework.security.core.userdetails.User.withUsername("student@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        adminDetails = org.springframework.security.core.userdetails.User.withUsername("admin@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")))
                .build();
    }

    @AfterEach
    void tearDown() {
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    private Event createTestEvent() {
        LocalDateTime now = LocalDateTime.now();
        Event event = Event.builder()
                .title("Hackathon")
                .description("A great hackathon")
                .councilId("council-123")
                .organizerId("organizer-123")
                .location("Hall A")
                .startTime(now.plusDays(2))
                .endTime(now.plusDays(3))
                .registrationDeadline(now.plusDays(1))
                .maxParticipants(100)
                .attendeeCount(0)
                .registeredUserIds(new ArrayList<>())
                .isCancelled(false)
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return eventRepository.save(event);
    }

    private Event createRegisteredEvent(List<String> registeredUserIds) {
        LocalDateTime now = LocalDateTime.now();
        Event event = Event.builder()
                .title("Hackathon")
                .description("A great hackathon")
                .councilId("council-123")
                .organizerId("organizer-123")
                .location("Hall A")
                .startTime(now.plusDays(2))
                .endTime(now.plusDays(3))
                .registrationDeadline(now.plusDays(1))
                .maxParticipants(100)
                .attendeeCount(registeredUserIds.size())
                .registeredUserIds(new ArrayList<>(registeredUserIds))
                .isCancelled(false)
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return eventRepository.save(event);
    }

    // --- REGISTER TESTS ---

    @Test
    void register_Student_ReturnsOk() throws Exception {
        Event event = createTestEvent();

        mockMvc.perform(post("/api/events/" + event.getId() + "/register")
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(event.getId()));

        Event updatedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertTrue(updatedEvent.getRegisteredUserIds().contains(studentUser.getId()));
    }

    @Test
    void register_SuperAdmin_ReturnsOk() throws Exception {
        Event event = createTestEvent();

        mockMvc.perform(post("/api/events/" + event.getId() + "/register")
                        .with(user(adminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(event.getId()));

        Event updatedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertTrue(updatedEvent.getRegisteredUserIds().contains(adminUser.getId()));
    }

    @Test
    void register_NoJwt_ReturnsUnauthorized() throws Exception {
        Event event = createTestEvent();

        mockMvc.perform(post("/api/events/" + event.getId() + "/register"))
                .andExpect(status().isUnauthorized());
    }

    // --- CANCEL REGISTRATION TESTS ---

    @Test
    void cancelRegistration_RegisteredStudent_ReturnsOk() throws Exception {
        Event event = createRegisteredEvent(List.of(studentUser.getId()));

        mockMvc.perform(delete("/api/events/" + event.getId() + "/register")
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(event.getId()));

        Event updatedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertFalse(updatedEvent.getRegisteredUserIds().contains(studentUser.getId()));
    }

    @Test
    void cancelRegistration_SuperAdmin_ReturnsOk() throws Exception {
        Event event = createRegisteredEvent(List.of(adminUser.getId()));

        mockMvc.perform(delete("/api/events/" + event.getId() + "/register")
                        .with(user(adminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(event.getId()));

        Event updatedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertFalse(updatedEvent.getRegisteredUserIds().contains(adminUser.getId()));
    }

    @Test
    void cancelRegistration_NoJwt_ReturnsUnauthorized() throws Exception {
        Event event = createTestEvent();

        mockMvc.perform(delete("/api/events/" + event.getId() + "/register"))
                .andExpect(status().isUnauthorized());
    }

    // --- REGISTRATION STATUS TESTS ---

    @Test
    void registrationStatus_AuthenticatedUser_ReturnsOk() throws Exception {
        Event event = createRegisteredEvent(List.of(studentUser.getId()));

        mockMvc.perform(get("/api/events/" + event.getId() + "/registration-status")
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registered").value(true));

        mockMvc.perform(get("/api/events/" + event.getId() + "/registration-status")
                        .with(user(adminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registered").value(false));
    }

    @Test
    void registrationStatus_NoJwt_ReturnsUnauthorized() throws Exception {
        Event event = createTestEvent();

        mockMvc.perform(get("/api/events/" + event.getId() + "/registration-status"))
                .andExpect(status().isUnauthorized());
    }

    // --- GET REGISTERED USERS TESTS ---

    @Test
    void getRegisteredUsers_AuthenticatedUser_ReturnsOk() throws Exception {
        Event event = createRegisteredEvent(List.of(studentUser.getId(), adminUser.getId()));

        mockMvc.perform(get("/api/events/" + event.getId() + "/registrations")
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value(studentUser.getId()))
                .andExpect(jsonPath("$[1]").value(adminUser.getId()));
    }

    @Test
    void getRegisteredUsers_NoJwt_ReturnsUnauthorized() throws Exception {
        Event event = createTestEvent();

        mockMvc.perform(get("/api/events/" + event.getId() + "/registrations"))
                .andExpect(status().isUnauthorized());
    }
}
