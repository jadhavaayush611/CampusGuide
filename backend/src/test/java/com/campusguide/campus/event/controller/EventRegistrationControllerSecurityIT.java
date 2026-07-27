package com.campusguide.campus.event.controller;

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
    private UserDetails studentDetails;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

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

        studentDetails = org.springframework.security.core.userdetails.User.withUsername("student@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
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
                .id(UUID.randomUUID())
                .title("Hackathon")
                .slug("hackathon-reg")
                .description("A great hackathon")
                .councilId(UUID.randomUUID())
                .venue("Hall A")
                .eventType(EventType.HACKATHON)
                .status(EventStatus.PUBLISHED)
                .startTime(now.plusDays(2))
                .endTime(now.plusDays(3))
                .createdAt(now)
                .updatedAt(now)
                .build();
        return eventRepository.save(event);
    }

    @Test
    void register_Student_ReturnsOk() throws Exception {
        Event event = createTestEvent();

        mockMvc.perform(post("/api/v1/events/" + event.getId() + "/register")
                        .with(user(studentDetails)))
                .andExpect(status().isOk());
    }

    @Test
    void isRegistered_Student_ReturnsOk() throws Exception {
        Event event = createTestEvent();

        mockMvc.perform(get("/api/v1/events/" + event.getId() + "/is-registered")
                        .with(user(studentDetails)))
                .andExpect(status().isOk());
    }
}
