package com.campusguide.campus.event.controller;

import com.campusguide.campus.council.entity.Council;
import com.campusguide.campus.council.repository.CouncilRepository;
import com.campusguide.campus.event.entity.Event;
import com.campusguide.campus.event.entity.EventStatus;
import com.campusguide.campus.event.entity.EventType;
import com.campusguide.campus.event.repository.EventRepository;
import com.campusguide.platform.user.entity.Role;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    private UserRepository userRepository;

    @Autowired
    private CouncilRepository councilRepository;

    private User adminUser;
    private User studentUser;

    private UserDetails adminDetails;
    private UserDetails studentDetails;

    private UUID councilId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        eventRepository.deleteAll();
        userRepository.deleteAll();
        councilRepository.deleteAll();

        councilId = UUID.randomUUID();
        Council council = Council.builder()
                .id(councilId)
                .name("Security Council")
                .slug("security-council")
                .description("Security Council")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        councilRepository.save(council);

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
        councilRepository.deleteAll();
    }

    @Test
    void createEvent_SuperAdmin_ReturnsCreated() throws Exception {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        String requestJson = "{"
                + "\"title\":\"Security Hackathon\","
                + "\"slug\":\"security-hackathon\","
                + "\"description\":\"Security competition\","
                + "\"councilId\":\"" + councilId + "\","
                + "\"venue\":\"Auditorium A\","
                + "\"eventType\":\"HACKATHON\","
                + "\"startTime\":\"" + now.plusDays(2) + "\","
                + "\"endTime\":\"" + now.plusDays(3) + "\""
                + "}";

        mockMvc.perform(post("/api/v1/events")
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Security Hackathon"));
    }

    @Test
    void getEvents_Authenticated_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/events")
                        .with(user(studentDetails)))
                .andExpect(status().isOk());
    }
}
