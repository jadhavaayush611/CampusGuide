package com.campusguide.campus.notice.controller;

import com.campusguide.campus.notice.dto.CreateNoticeRequest;
import com.campusguide.campus.notice.dto.UpdateNoticeRequest;
import com.campusguide.campus.notice.entity.Notice;
import com.campusguide.campus.notice.enums.NoticeCategory;
import com.campusguide.campus.notice.enums.NoticePriority;
import com.campusguide.campus.notice.enums.NoticeVisibility;
import com.campusguide.campus.notice.repository.NoticeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class NoticeControllerSecurityIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private NoticeRepository noticeRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private UserDetails studentUser;
    private UserDetails councilAdminUser;
    private Notice testNotice;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        noticeRepository.deleteAll();

        studentUser = User.withUsername("student@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        councilAdminUser = User.withUsername("council@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_COUNCIL_ADMIN")))
                .build();

        testNotice = Notice.builder()
                .id(UUID.randomUUID())
                .title("Draft Notice")
                .slug("draft-notice")
                .content("Draft content")
                .summary("Draft summary")
                .category(NoticeCategory.GENERAL)
                .priority(NoticePriority.LOW)
                .visibility(NoticeVisibility.PUBLIC)
                .isPublished(false)
                .isPinned(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testNotice = noticeRepository.save(testNotice);
    }

    @Test
    void student_CannotCreateNotice_ReturnsForbidden() throws Exception {
        CreateNoticeRequest request = CreateNoticeRequest.builder()
                .title("Unauthorized Notice")
                .slug("unauthorized-notice")
                .content("Content")
                .build();

        mockMvc.perform(post("/api/v1/notices")
                        .with(user(studentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void student_CannotUpdateNotice_ReturnsForbidden() throws Exception {
        UpdateNoticeRequest request = UpdateNoticeRequest.builder()
                .title("Attempted Update")
                .slug("attempted-update")
                .content("Content")
                .build();

        mockMvc.perform(put("/api/v1/notices/" + testNotice.getId())
                        .with(user(studentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void student_CannotPublishNotice_ReturnsForbidden() throws Exception {
        mockMvc.perform(patch("/api/v1/notices/" + testNotice.getId() + "/publish")
                        .with(user(studentUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void student_CannotPinNotice_ReturnsForbidden() throws Exception {
        mockMvc.perform(patch("/api/v1/notices/" + testNotice.getId() + "/pin")
                        .with(user(studentUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void student_CannotDeleteNotice_ReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/notices/" + testNotice.getId())
                        .with(user(studentUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymous_CannotAccessNotices_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/notices"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/notices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void councilAdmin_CanCreateNotice_ReturnsCreated() throws Exception {
        CreateNoticeRequest request = CreateNoticeRequest.builder()
                .title("Council Notice")
                .slug("council-notice")
                .content("Council Content")
                .summary("Summary")
                .category(NoticeCategory.GENERAL)
                .priority(NoticePriority.MEDIUM)
                .visibility(NoticeVisibility.PUBLIC)
                .isPublished(true)
                .build();

        mockMvc.perform(post("/api/v1/notices")
                        .with(user(councilAdminUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
