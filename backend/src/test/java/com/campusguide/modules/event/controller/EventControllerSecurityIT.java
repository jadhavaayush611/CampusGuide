package com.campusguide.modules.event.controller;

import com.campusguide.modules.council.entity.Council;
import com.campusguide.modules.council.repository.CouncilRepository;
import com.campusguide.modules.event.entity.Event;
import com.campusguide.modules.event.repository.EventRepository;
import com.campusguide.modules.user.entity.Role;
import com.campusguide.modules.user.entity.User;
import com.campusguide.modules.user.repository.UserRepository;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class EventControllerSecurityIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CouncilRepository councilRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User organizerUser;
    private User otherUser;
    private User adminUser;

    private UserDetails organizerDetails;
    private UserDetails otherDetails;
    private UserDetails adminDetails;

    private Council testCouncil;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        // 1. Save Users in Repository
        organizerUser = User.builder()
                .email("organizer@campusguide.com")
                .password("password")
                .role(Role.STUDENT)
                .firstName("Organizer")
                .lastName("User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        organizerUser = userRepository.save(organizerUser);

        otherUser = User.builder()
                .email("other@campusguide.com")
                .password("password")
                .role(Role.STUDENT)
                .firstName("Other")
                .lastName("User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        otherUser = userRepository.save(otherUser);

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

        // 2. Build UserDetails for MockMvc authentication helper
        organizerDetails = org.springframework.security.core.userdetails.User.withUsername("organizer@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        otherDetails = org.springframework.security.core.userdetails.User.withUsername("other@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        adminDetails = org.springframework.security.core.userdetails.User.withUsername("admin@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")))
                .build();

        // 3. Save a Test Council
        testCouncil = Council.builder()
                .name("Technical Council")
                .description("Test Technical Council")
                .category("TECHNICAL")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testCouncil = councilRepository.save(testCouncil);
    }

    @AfterEach
    void tearDown() {
        eventRepository.deleteAll();
        councilRepository.deleteAll();
        userRepository.deleteAll();
    }

    // --- CREATE EVENT TESTS ---

    @Test
    void createEvent_AuthenticatedOrganizer_ReturnsCreated() throws Exception {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        String requestJson = "{"
                + "\"title\":\"Hackathon\","
                + "\"description\":\"A great hackathon\","
                + "\"councilId\":\"" + testCouncil.getId() + "\","
                + "\"location\":\"Hall A\","
                + "\"startTime\":\"" + now.plusDays(2) + "\","
                + "\"endTime\":\"" + now.plusDays(3) + "\","
                + "\"registrationDeadline\":\"" + now.plusDays(1) + "\","
                + "\"maxParticipants\":100,"
                + "\"imageUrl\":\"http://example.com/image.png\""
                + "}";

        mockMvc.perform(post("/api/events")
                        .with(user(organizerDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Hackathon"))
                .andExpect(jsonPath("$.organizerId").value(organizerUser.getId()));
    }

    @Test
    void createEvent_SuperAdmin_ReturnsCreated() throws Exception {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        String requestJson = "{"
                + "\"title\":\"Hackathon\","
                + "\"description\":\"A great hackathon\","
                + "\"councilId\":\"" + testCouncil.getId() + "\","
                + "\"location\":\"Hall A\","
                + "\"startTime\":\"" + now.plusDays(2) + "\","
                + "\"endTime\":\"" + now.plusDays(3) + "\","
                + "\"registrationDeadline\":\"" + now.plusDays(1) + "\","
                + "\"maxParticipants\":100,"
                + "\"imageUrl\":\"http://example.com/image.png\""
                + "}";

        mockMvc.perform(post("/api/events")
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Hackathon"))
                .andExpect(jsonPath("$.organizerId").value(adminUser.getId()));
    }

    @Test
    void createEvent_NoJwt_ReturnsUnauthorized() throws Exception {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        String requestJson = "{"
                + "\"title\":\"Hackathon\","
                + "\"description\":\"A great hackathon\","
                + "\"councilId\":\"" + testCouncil.getId() + "\","
                + "\"location\":\"Hall A\","
                + "\"startTime\":\"" + now.plusDays(2) + "\","
                + "\"endTime\":\"" + now.plusDays(3) + "\","
                + "\"registrationDeadline\":\"" + now.plusDays(1) + "\","
                + "\"maxParticipants\":100,"
                + "\"imageUrl\":\"http://example.com/image.png\""
                + "}";

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnauthorized());
    }

    // --- UPDATE EVENT TESTS ---

    @Test
    void updateEvent_Organizer_ReturnsOk() throws Exception {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        Event event = Event.builder()
                .title("Original Hackathon")
                .description("Original description")
                .councilId(testCouncil.getId())
                .organizerId(organizerUser.getId())
                .location("Hall A")
                .startTime(now.plusDays(2))
                .endTime(now.plusDays(3))
                .registrationDeadline(now.plusDays(1))
                .maxParticipants(100)
                .attendeeCount(0)
                .isCancelled(false)
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
        event = eventRepository.save(event);

        String requestJson = "{\"title\":\"Updated Hackathon\"}";

        mockMvc.perform(put("/api/events/" + event.getId())
                        .with(user(organizerDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Hackathon"));
    }

    @Test
    void updateEvent_SuperAdmin_ReturnsOk() throws Exception {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        Event event = Event.builder()
                .title("Original Hackathon")
                .description("Original description")
                .councilId(testCouncil.getId())
                .organizerId(organizerUser.getId())
                .location("Hall A")
                .startTime(now.plusDays(2))
                .endTime(now.plusDays(3))
                .registrationDeadline(now.plusDays(1))
                .maxParticipants(100)
                .attendeeCount(0)
                .isCancelled(false)
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
        event = eventRepository.save(event);

        String requestJson = "{\"title\":\"Updated Hackathon\"}";

        mockMvc.perform(put("/api/events/" + event.getId())
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Hackathon"));
    }

    @Test
    void updateEvent_OtherStudent_ReturnsForbidden() throws Exception {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        Event event = Event.builder()
                .title("Original Hackathon")
                .description("Original description")
                .councilId(testCouncil.getId())
                .organizerId(organizerUser.getId())
                .location("Hall A")
                .startTime(now.plusDays(2))
                .endTime(now.plusDays(3))
                .registrationDeadline(now.plusDays(1))
                .maxParticipants(100)
                .attendeeCount(0)
                .isCancelled(false)
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
        event = eventRepository.save(event);

        String requestJson = "{\"title\":\"Updated Hackathon\"}";

        mockMvc.perform(put("/api/events/" + event.getId())
                        .with(user(otherDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("You are not authorized to update this event"));
    }

    @Test
    void updateEvent_NoJwt_ReturnsUnauthorized() throws Exception {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        Event event = Event.builder()
                .title("Original Hackathon")
                .description("Original description")
                .councilId(testCouncil.getId())
                .organizerId(organizerUser.getId())
                .location("Hall A")
                .startTime(now.plusDays(2))
                .endTime(now.plusDays(3))
                .registrationDeadline(now.plusDays(1))
                .maxParticipants(100)
                .attendeeCount(0)
                .isCancelled(false)
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
        event = eventRepository.save(event);

        String requestJson = "{\"title\":\"Updated Hackathon\"}";

        mockMvc.perform(put("/api/events/" + event.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnauthorized());
    }

    // --- DELETE EVENT TESTS ---

    @Test
    void deleteEvent_Organizer_ReturnsNoContent() throws Exception {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        Event event = Event.builder()
                .title("Original Hackathon")
                .description("Original description")
                .councilId(testCouncil.getId())
                .organizerId(organizerUser.getId())
                .location("Hall A")
                .startTime(now.plusDays(2))
                .endTime(now.plusDays(3))
                .registrationDeadline(now.plusDays(1))
                .maxParticipants(100)
                .attendeeCount(0)
                .isCancelled(false)
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
        event = eventRepository.save(event);

        mockMvc.perform(delete("/api/events/" + event.getId())
                        .with(user(organizerDetails)))
                .andExpect(status().isNoContent());

        Event deletedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertTrue(deletedEvent.getIsDeleted());
    }

    @Test
    void deleteEvent_SuperAdmin_ReturnsNoContent() throws Exception {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        Event event = Event.builder()
                .title("Original Hackathon")
                .description("Original description")
                .councilId(testCouncil.getId())
                .organizerId(organizerUser.getId())
                .location("Hall A")
                .startTime(now.plusDays(2))
                .endTime(now.plusDays(3))
                .registrationDeadline(now.plusDays(1))
                .maxParticipants(100)
                .attendeeCount(0)
                .isCancelled(false)
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
        event = eventRepository.save(event);

        mockMvc.perform(delete("/api/events/" + event.getId())
                        .with(user(adminDetails)))
                .andExpect(status().isNoContent());

        Event deletedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertTrue(deletedEvent.getIsDeleted());
    }

    @Test
    void deleteEvent_OtherStudent_ReturnsForbidden() throws Exception {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        Event event = Event.builder()
                .title("Original Hackathon")
                .description("Original description")
                .councilId(testCouncil.getId())
                .organizerId(organizerUser.getId())
                .location("Hall A")
                .startTime(now.plusDays(2))
                .endTime(now.plusDays(3))
                .registrationDeadline(now.plusDays(1))
                .maxParticipants(100)
                .attendeeCount(0)
                .isCancelled(false)
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
        event = eventRepository.save(event);

        mockMvc.perform(delete("/api/events/" + event.getId())
                        .with(user(otherDetails)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("You are not authorized to delete this event"));

        Event notDeletedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertFalse(notDeletedEvent.getIsDeleted());
    }

    @Test
    void deleteEvent_NoJwt_ReturnsUnauthorized() throws Exception {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        Event event = Event.builder()
                .title("Original Hackathon")
                .description("Original description")
                .councilId(testCouncil.getId())
                .organizerId(organizerUser.getId())
                .location("Hall A")
                .startTime(now.plusDays(2))
                .endTime(now.plusDays(3))
                .registrationDeadline(now.plusDays(1))
                .maxParticipants(100)
                .attendeeCount(0)
                .isCancelled(false)
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
        event = eventRepository.save(event);

        mockMvc.perform(delete("/api/events/" + event.getId()))
                .andExpect(status().isUnauthorized());

        Event notDeletedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertFalse(notDeletedEvent.getIsDeleted());
    }

    // --- GET EVENT TESTS ---

    @Test
    void getEvent_Authenticated_ReturnsOk() throws Exception {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        Event event = Event.builder()
                .title("Original Hackathon")
                .description("Original description")
                .councilId(testCouncil.getId())
                .organizerId(organizerUser.getId())
                .location("Hall A")
                .startTime(now.plusDays(2))
                .endTime(now.plusDays(3))
                .registrationDeadline(now.plusDays(1))
                .maxParticipants(100)
                .attendeeCount(0)
                .isCancelled(false)
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
        event = eventRepository.save(event);

        mockMvc.perform(get("/api/events/" + event.getId())
                        .with(user(otherDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Original Hackathon"));
    }

    @Test
    void getEvent_NoJwt_ReturnsUnauthorized() throws Exception {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        Event event = Event.builder()
                .title("Original Hackathon")
                .description("Original description")
                .councilId(testCouncil.getId())
                .organizerId(organizerUser.getId())
                .location("Hall A")
                .startTime(now.plusDays(2))
                .endTime(now.plusDays(3))
                .registrationDeadline(now.plusDays(1))
                .maxParticipants(100)
                .attendeeCount(0)
                .isCancelled(false)
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
        event = eventRepository.save(event);

        mockMvc.perform(get("/api/events/" + event.getId()))
                .andExpect(status().isUnauthorized());
    }

    // --- GET ALL EVENTS TESTS ---

    @Test
    void getAllEvents_Authenticated_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/events")
                        .with(user(otherDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAllEvents_NoJwt_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isUnauthorized());
    }

    // --- GET UPCOMING EVENTS TESTS ---

    @Test
    void getUpcomingEvents_Authenticated_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/events/upcoming")
                        .with(user(otherDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getUpcomingEvents_NoJwt_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/events/upcoming"))
                .andExpect(status().isUnauthorized());
    }

    // --- GET EVENTS BY COUNCIL TESTS ---

    @Test
    void getEventsByCouncil_Authenticated_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/events/council/" + testCouncil.getId())
                        .with(user(otherDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getEventsByCouncil_NoJwt_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/events/council/" + testCouncil.getId()))
                .andExpect(status().isUnauthorized());
    }
}
