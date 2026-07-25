package com.campusguide.personal.notification.controller;

import com.campusguide.personal.notification.entity.Notification;
import com.campusguide.personal.notification.enums.NotificationPriority;
import com.campusguide.personal.notification.enums.NotificationType;
import com.campusguide.personal.notification.repository.NotificationRepository;
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
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class NotificationControllerSecurityIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private User studentUser;
    private User otherUser;

    private UserDetails studentDetails;
    private UserDetails otherDetails;

    private Notification studentNotification;
    private Notification otherNotification;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        notificationRepository.deleteAll();
        userRepository.deleteAll();

        studentUser = User.builder()
                .email("student@campusguide.com")
                .password("password")
                .role(Role.STUDENT)
                .firstName("Student")
                .lastName("User")
                .build();
        studentUser = userRepository.save(studentUser);

        otherUser = User.builder()
                .email("other@campusguide.com")
                .password("password")
                .role(Role.STUDENT)
                .firstName("Other")
                .lastName("User")
                .build();
        otherUser = userRepository.save(otherUser);

        studentDetails = org.springframework.security.core.userdetails.User.withUsername(studentUser.getEmail())
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        otherDetails = org.springframework.security.core.userdetails.User.withUsername(otherUser.getEmail())
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        studentNotification = Notification.builder()
                .userId(studentUser.getId())
                .title("Student Notification")
                .message("Message 1")
                .type(NotificationType.ACADEMIC)
                .priority(NotificationPriority.NORMAL)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
        studentNotification = notificationRepository.save(studentNotification);

        otherNotification = Notification.builder()
                .userId(otherUser.getId())
                .title("Other Notification")
                .message("Message 2")
                .type(NotificationType.SYSTEM)
                .priority(NotificationPriority.HIGH)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
        otherNotification = notificationRepository.save(otherNotification);
    }

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testListNotifications_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testListNotifications_Authenticated() throws Exception {
        mockMvc.perform(get("/api/notifications")
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Student Notification"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void testListUnreadNotifications_Authenticated() throws Exception {
        mockMvc.perform(get("/api/notifications/unread")
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Student Notification"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void testCountUnreadNotifications_Authenticated() throws Exception {
        mockMvc.perform(get("/api/notifications/unread/count")
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void testMarkAsRead_Authenticated_Success() throws Exception {
        mockMvc.perform(patch("/api/notifications/" + studentNotification.getId() + "/read")
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    void testMarkAsRead_Authenticated_Forbidden() throws Exception {
        mockMvc.perform(patch("/api/notifications/" + otherNotification.getId() + "/read")
                        .with(user(studentDetails)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testMarkAllAsRead_Authenticated() throws Exception {
        mockMvc.perform(patch("/api/notifications/read-all")
                        .with(user(studentDetails)))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteNotification_Authenticated_Success() throws Exception {
        mockMvc.perform(delete("/api/notifications/" + studentNotification.getId())
                        .with(user(studentDetails)))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteNotification_Authenticated_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/notifications/" + otherNotification.getId())
                        .with(user(studentDetails)))
                .andExpect(status().isForbidden());
    }
}
