package com.campusguide.campus.event.controller;

import com.campusguide.campus.council.entity.Council;
import com.campusguide.campus.council.repository.CouncilRepository;
import com.campusguide.campus.event.entity.Event;
import com.campusguide.campus.event.entity.EventStatus;
import com.campusguide.campus.event.entity.EventType;
import com.campusguide.campus.event.repository.EventRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
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
class EventControllerIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CouncilRepository councilRepository;

    private UserDetails adminDetails;
    private UserDetails studentDetails;
    private Council council;
    private UUID councilId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        eventRepository.deleteAll();
        councilRepository.deleteAll();

        councilId = UUID.randomUUID();
        council = Council.builder()
                .id(councilId)
                .name("Technical Council")
                .slug("technical-council")
                .description("Handles technical clubs")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        councilRepository.save(council);

        adminDetails = User.withUsername("admin@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")))
                .build();

        studentDetails = User.withUsername("student@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();
    }

    @AfterEach
    void tearDown() {
        eventRepository.deleteAll();
        councilRepository.deleteAll();
    }

    @Test
    void createEvent_ReturnsCreated() throws Exception {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        String requestJson = "{"
                + "\"title\":\"CodeFest 2026\","
                + "\"slug\":\"codefest-2026\","
                + "\"description\":\"Annual coding event\","
                + "\"summary\":\"Codefest summary\","
                + "\"councilId\":\"" + councilId + "\","
                + "\"venue\":\"Auditorium\","
                + "\"eventType\":\"HACKATHON\","
                + "\"status\":\"PUBLISHED\","
                + "\"registrationRequired\":false,"
                + "\"startTime\":\"" + now.plusDays(2) + "\","
                + "\"endTime\":\"" + now.plusDays(3) + "\""
                + "}";

        mockMvc.perform(post("/api/v1/events")
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("CodeFest 2026"))
                .andExpect(jsonPath("$.slug").value("codefest-2026"));
    }

    @Test
    void getPublicEvents_ReturnsOk() throws Exception {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        Event event = Event.builder()
                .id(UUID.randomUUID())
                .title("Public Seminar")
                .slug("public-seminar")
                .description("Open to all")
                .councilId(councilId)
                .venue("Hall 1")
                .eventType(EventType.SEMINAR)
                .status(EventStatus.PUBLISHED)
                .startTime(now.plusDays(1))
                .endTime(now.plusDays(2))
                .createdAt(now)
                .updatedAt(now)
                .build();
        eventRepository.save(event);

        mockMvc.perform(get("/api/v1/events")
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Public Seminar"));
    }

    @Test
    void getEventById_ReturnsOk() throws Exception {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        UUID id = UUID.randomUUID();
        Event event = Event.builder()
                .id(id)
                .title("Specific Event")
                .slug("specific-event")
                .description("Specific description")
                .councilId(councilId)
                .venue("Hall 2")
                .eventType(EventType.WORKSHOP)
                .status(EventStatus.DRAFT)
                .startTime(now.plusDays(1))
                .endTime(now.plusDays(2))
                .createdAt(now)
                .updatedAt(now)
                .build();
        eventRepository.save(event);

        mockMvc.perform(get("/api/v1/events/" + id)
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value("Specific Event"));
    }

    @Test
    void getEventBySlug_ReturnsOk() throws Exception {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        Event event = Event.builder()
                .id(UUID.randomUUID())
                .title("Slug Event")
                .slug("slug-event")
                .description("Slug description")
                .councilId(councilId)
                .venue("Hall 3")
                .eventType(EventType.CULTURAL)
                .status(EventStatus.PUBLISHED)
                .startTime(now.plusDays(1))
                .endTime(now.plusDays(2))
                .createdAt(now)
                .updatedAt(now)
                .build();
        eventRepository.save(event);

        mockMvc.perform(get("/api/v1/events/slug/slug-event")
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("slug-event"));
    }

    @Test
    void getEventsByCouncil_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/events/council/" + councilId)
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void updateEvent_ReturnsOk() throws Exception {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        UUID id = UUID.randomUUID();
        Event event = Event.builder()
                .id(id)
                .title("Old Title")
                .slug("old-title")
                .description("Old description")
                .councilId(councilId)
                .venue("Hall A")
                .eventType(EventType.SEMINAR)
                .status(EventStatus.PUBLISHED)
                .startTime(now.plusDays(1))
                .endTime(now.plusDays(2))
                .createdAt(now)
                .updatedAt(now)
                .build();
        eventRepository.save(event);

        String requestJson = "{"
                + "\"title\":\"New Title\","
                + "\"slug\":\"new-title\","
                + "\"description\":\"Updated description\","
                + "\"venue\":\"Hall B\","
                + "\"eventType\":\"SEMINAR\","
                + "\"startTime\":\"" + now.plusDays(1) + "\","
                + "\"endTime\":\"" + now.plusDays(2) + "\""
                + "}";

        mockMvc.perform(put("/api/v1/events/" + id)
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.slug").value("new-title"));
    }

    @Test
    void updateEventStatus_ReturnsOk() throws Exception {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        UUID id = UUID.randomUUID();
        Event event = Event.builder()
                .id(id)
                .title("Status Event")
                .slug("status-event")
                .description("Description")
                .councilId(councilId)
                .venue("Hall A")
                .eventType(EventType.SEMINAR)
                .status(EventStatus.DRAFT)
                .startTime(now.plusDays(1))
                .endTime(now.plusDays(2))
                .createdAt(now)
                .updatedAt(now)
                .build();
        eventRepository.save(event);

        String requestJson = "{\"status\":\"PUBLISHED\"}";

        mockMvc.perform(patch("/api/v1/events/" + id + "/status")
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    void deleteEvent_ReturnsNoContent() throws Exception {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        UUID id = UUID.randomUUID();
        Event event = Event.builder()
                .id(id)
                .title("Event to delete")
                .slug("event-to-delete")
                .description("Description")
                .councilId(councilId)
                .venue("Hall A")
                .eventType(EventType.SEMINAR)
                .status(EventStatus.DRAFT)
                .startTime(now.plusDays(1))
                .endTime(now.plusDays(2))
                .createdAt(now)
                .updatedAt(now)
                .build();
        eventRepository.save(event);

        mockMvc.perform(delete("/api/v1/events/" + id)
                        .with(user(adminDetails)))
                .andExpect(status().isNoContent());

        assertFalse(eventRepository.existsById(id));
    }
}
