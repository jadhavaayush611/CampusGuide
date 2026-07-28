package com.campusguide.personal.notification.controller;

import com.campusguide.personal.notification.dto.UpdateNotificationStatusRequest;
import com.campusguide.personal.notification.entity.ScheduledNotification;
import com.campusguide.personal.notification.enums.NotificationChannel;
import com.campusguide.personal.notification.enums.NotificationPriority;
import com.campusguide.personal.notification.enums.NotificationStatus;
import com.campusguide.personal.notification.enums.NotificationType;
import com.campusguide.personal.notification.repository.ScheduledNotificationRepository;
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
class ScheduledNotificationSecurityIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ScheduledNotificationRepository repository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = com.fasterxml.jackson.databind.json.JsonMapper.builder()
            .findAndAddModules()
            .build();

    private User student1;
    private User student2;

    private UserDetails student1Details;
    private UserDetails student2Details;

    private ScheduledNotification student1Notif;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        repository.deleteAll();
        userRepository.deleteAll();

        UUID u1Id = UUID.randomUUID();
        student1 = User.builder()
                .id(u1Id.toString())
                .email("student1@campusguide.com")
                .password("password")
                .role(Role.STUDENT)
                .firstName("Student")
                .lastName("One")
                .build();
        student1 = userRepository.save(student1);

        UUID u2Id = UUID.randomUUID();
        student2 = User.builder()
                .id(u2Id.toString())
                .email("student2@campusguide.com")
                .password("password")
                .role(Role.STUDENT)
                .firstName("Student")
                .lastName("Two")
                .build();
        student2 = userRepository.save(student2);

        student1Details = org.springframework.security.core.userdetails.User.withUsername(student1.getEmail())
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        student2Details = org.springframework.security.core.userdetails.User.withUsername(student2.getEmail())
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        student1Notif = ScheduledNotification.builder()
                .id(UUID.randomUUID())
                .userId(u1Id.toString())
                .title("Student 1 Private Notif")
                .message("Message")
                .type(NotificationType.REMINDER)
                .status(NotificationStatus.SCHEDULED)
                .scheduledFor(LocalDateTime.now().plusHours(2))
                .channel(NotificationChannel.IN_APP)
                .priority(NotificationPriority.NORMAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        student1Notif = repository.save(student1Notif);
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testUnauthenticatedAccess_ForbiddenOrUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/scheduled-notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAccessOtherUserNotification_Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/scheduled-notifications/" + student1Notif.getId())
                        .with(user(student2Details)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateStatusOtherUserNotification_Forbidden() throws Exception {
        UpdateNotificationStatusRequest statusReq = new UpdateNotificationStatusRequest(NotificationStatus.DELIVERED);

        mockMvc.perform(patch("/api/v1/scheduled-notifications/" + student1Notif.getId() + "/status")
                        .with(user(student2Details))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteOtherUserNotification_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/scheduled-notifications/" + student1Notif.getId())
                        .with(user(student2Details)))
                .andExpect(status().isForbidden());
    }
}
