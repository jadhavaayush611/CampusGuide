package com.campusguide.personal.notification.controller;

import com.campusguide.personal.notification.dto.CreateScheduledNotificationRequest;
import com.campusguide.personal.notification.dto.UpdateNotificationStatusRequest;
import com.campusguide.personal.notification.dto.UpdateScheduledNotificationRequest;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ScheduledNotificationControllerIT {

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

    private User studentUser;
    private UserDetails studentDetails;
    private UUID studentUserId;
    private ScheduledNotification existingNotif;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        repository.deleteAll();
        userRepository.deleteAll();

        studentUserId = UUID.randomUUID();
        studentUser = User.builder()
                .id(studentUserId.toString())
                .email("student@campusguide.com")
                .password("password")
                .role(Role.STUDENT)
                .firstName("Student")
                .lastName("User")
                .build();
        studentUser = userRepository.save(studentUser);

        studentDetails = org.springframework.security.core.userdetails.User.withUsername(studentUser.getEmail())
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        existingNotif = ScheduledNotification.builder()
                .id(UUID.randomUUID())
                .userId(studentUserId.toString())
                .title("Existing Notification")
                .message("Message content")
                .type(NotificationType.REMINDER)
                .status(NotificationStatus.SCHEDULED)
                .scheduledFor(LocalDateTime.now().plusHours(1))
                .channel(NotificationChannel.IN_APP)
                .priority(NotificationPriority.NORMAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        existingNotif = repository.save(existingNotif);
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testCreateNotification_Success() throws Exception {
        CreateScheduledNotificationRequest request = CreateScheduledNotificationRequest.builder()
                .title("New Notification")
                .message("Reminder for exam")
                .type(NotificationType.ACADEMIC)
                .scheduledFor(LocalDateTime.now().plusDays(1))
                .channel(NotificationChannel.EMAIL)
                .priority(NotificationPriority.HIGH)
                .build();

        mockMvc.perform(post("/api/v1/scheduled-notifications")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Notification"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.userId").value(studentUserId.toString()));
    }

    @Test
    void testGetAllNotifications_Success() throws Exception {
        mockMvc.perform(get("/api/v1/scheduled-notifications")
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Existing Notification"));
    }

    @Test
    void testGetNotificationById_Success() throws Exception {
        mockMvc.perform(get("/api/v1/scheduled-notifications/" + existingNotif.getId())
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingNotif.getId().toString()))
                .andExpect(jsonPath("$.title").value("Existing Notification"));
    }

    @Test
    void testGetPendingNotifications_Success() throws Exception {
        ScheduledNotification pastPending = ScheduledNotification.builder()
                .id(UUID.randomUUID())
                .userId(studentUserId.toString())
                .title("Past Pending")
                .message("Past due message")
                .type(NotificationType.EVENT)
                .status(NotificationStatus.SCHEDULED)
                .scheduledFor(LocalDateTime.now().minusMinutes(5))
                .channel(NotificationChannel.PUSH)
                .priority(NotificationPriority.HIGH)
                .createdAt(LocalDateTime.now().minusHours(1))
                .updatedAt(LocalDateTime.now().minusHours(1))
                .build();
        repository.save(pastPending);

        mockMvc.perform(get("/api/v1/scheduled-notifications/pending")
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Past Pending"));
    }

    @Test
    void testUpdateNotificationStatus_Success() throws Exception {
        UpdateNotificationStatusRequest statusReq = new UpdateNotificationStatusRequest(NotificationStatus.DELIVERED);

        mockMvc.perform(patch("/api/v1/scheduled-notifications/" + existingNotif.getId() + "/status")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"))
                .andExpect(jsonPath("$.deliveredAt").exists());
    }

    @Test
    void testUpdateNotification_Success() throws Exception {
        UpdateScheduledNotificationRequest updateReq = UpdateScheduledNotificationRequest.builder()
                .title("Updated Title")
                .message("Updated message content")
                .type(NotificationType.SYSTEM)
                .scheduledFor(LocalDateTime.now().plusHours(5))
                .channel(NotificationChannel.PUSH)
                .priority(NotificationPriority.HIGH)
                .build();

        mockMvc.perform(put("/api/v1/scheduled-notifications/" + existingNotif.getId())
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.channel").value("PUSH"));
    }

    @Test
    void testDeleteNotification_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/scheduled-notifications/" + existingNotif.getId())
                        .with(user(studentDetails)))
                .andExpect(status().isNoContent());
    }
}
