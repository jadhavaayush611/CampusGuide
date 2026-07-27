package com.campusguide.campus.notice.controller;

import com.campusguide.campus.notice.dto.*;
import com.campusguide.campus.notice.entity.Notice;
import com.campusguide.campus.notice.enums.NoticeCategory;
import com.campusguide.campus.notice.enums.NoticePriority;
import com.campusguide.campus.notice.enums.NoticeVisibility;
import com.campusguide.campus.notice.repository.NoticeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class NoticeControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private NoticeRepository noticeRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private Notice testNotice;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        noticeRepository.deleteAll();

        userDetails = User.withUsername("student@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        testNotice = Notice.builder()
                .id(UUID.randomUUID())
                .title("Registration Open")
                .slug("registration-open")
                .content("Course registration for next semester is now open.")
                .summary("Registration notice")
                .category(NoticeCategory.ACADEMIC)
                .priority(NoticePriority.HIGH)
                .visibility(NoticeVisibility.PUBLIC)
                .publishedAt(LocalDateTime.now())
                .isPinned(true)
                .isPublished(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testNotice = noticeRepository.save(testNotice);
    }

    @AfterEach
    void tearDown() {
        noticeRepository.deleteAll();
    }

    @Test
    void createNotice_ReturnsCreated() throws Exception {
        CreateNoticeRequest request = CreateNoticeRequest.builder()
                .title("Library Timing Change")
                .slug("library-timing-change")
                .content("Library will remain open 24x7 during exams.")
                .summary("Library notice")
                .category(NoticeCategory.GENERAL)
                .priority(NoticePriority.MEDIUM)
                .visibility(NoticeVisibility.PUBLIC)
                .isPublished(true)
                .build();

        mockMvc.perform(post("/api/v1/notices")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Library Timing Change"))
                .andExpect(jsonPath("$.slug").value("library-timing-change"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void createNotice_WithInvalidSlug_ReturnsBadRequest() throws Exception {
        CreateNoticeRequest request = CreateNoticeRequest.builder()
                .title("Bad Slug Notice")
                .slug("Bad Slug!")
                .content("Content here")
                .build();

        mockMvc.perform(post("/api/v1/notices")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createNotice_WithDuplicateSlug_ReturnsConflict() throws Exception {
        CreateNoticeRequest request = CreateNoticeRequest.builder()
                .title("Duplicate Slug Notice")
                .slug("registration-open")
                .content("Duplicate slug content")
                .build();

        mockMvc.perform(post("/api/v1/notices")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getAllNotices_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/notices")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Registration Open"))
                .andExpect(jsonPath("$[0].slug").value("registration-open"));
    }

    @Test
    void getNoticeById_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/notices/" + testNotice.getId())
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testNotice.getId().toString()))
                .andExpect(jsonPath("$.title").value("Registration Open"));
    }

    @Test
    void getNoticeById_NonExistent_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/notices/" + UUID.randomUUID())
                        .with(user(userDetails)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getNoticeBySlug_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/notices/slug/registration-open")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Registration Open"));
    }

    @Test
    void getNoticeBySlug_NonExistent_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/notices/slug/non-existent-slug")
                        .with(user(userDetails)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateNotice_ReturnsOk() throws Exception {
        UpdateNoticeRequest request = UpdateNoticeRequest.builder()
                .title("Updated Registration Open")
                .slug("updated-registration-open")
                .content("Updated content for registration.")
                .summary("Updated summary")
                .category(NoticeCategory.ACADEMIC)
                .priority(NoticePriority.URGENT)
                .visibility(NoticeVisibility.PUBLIC)
                .isPinned(true)
                .isPublished(true)
                .build();

        mockMvc.perform(put("/api/v1/notices/" + testNotice.getId())
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Registration Open"))
                .andExpect(jsonPath("$.slug").value("updated-registration-open"));
    }

    @Test
    void publishNotice_ReturnsOk() throws Exception {
        PublishNoticeRequest request = new PublishNoticeRequest(false);

        mockMvc.perform(patch("/api/v1/notices/" + testNotice.getId() + "/publish")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isPublished").value(false));
    }

    @Test
    void pinNotice_ReturnsOk() throws Exception {
        PinNoticeRequest request = new PinNoticeRequest(false);

        mockMvc.perform(patch("/api/v1/notices/" + testNotice.getId() + "/pin")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isPinned").value(false));
    }

    @Test
    void deleteNotice_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/notices/" + testNotice.getId())
                        .with(user(userDetails)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/notices/" + testNotice.getId())
                        .with(user(userDetails)))
                .andExpect(status().isNotFound());
    }
}
