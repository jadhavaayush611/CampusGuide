package com.campusguide.common.attachment.controller;

import com.campusguide.campus.notice.entity.Notice;
import com.campusguide.campus.notice.enums.NoticeCategory;
import com.campusguide.campus.notice.enums.NoticePriority;
import com.campusguide.campus.notice.enums.NoticeVisibility;
import com.campusguide.campus.notice.repository.NoticeRepository;
import com.campusguide.campus.notice.service.NoticeService;
import com.campusguide.common.attachment.entity.Attachment;
import com.campusguide.common.attachment.entity.AttachmentOwnerType;
import com.campusguide.common.attachment.repository.AttachmentRepository;
import com.campusguide.common.storage.StorageService;
import com.campusguide.personal.planner.entity.PlannerTask;
import com.campusguide.personal.planner.entity.TaskPriority;
import com.campusguide.personal.planner.entity.TaskStatus;
import com.campusguide.personal.planner.entity.TaskType;
import com.campusguide.personal.planner.repository.PlannerTaskRepository;
import com.campusguide.personal.planner.service.PlannerTaskService;
import com.campusguide.platform.user.entity.Role;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class AttachmentControllerIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private PlannerTaskRepository plannerTaskRepository;

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StorageService storageService;

    @Autowired
    private PlannerTaskService plannerTaskService;

    @Autowired
    private NoticeService noticeService;

    private User studentA;
    private User studentB;
    private User adminUser;

    private UserDetails studentADetails;
    private UserDetails studentBDetails;
    private UserDetails adminDetails;

    private PlannerTask studentATask;
    private Notice publicNotice;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        attachmentRepository.deleteAll();
        plannerTaskRepository.deleteAll();
        noticeRepository.deleteAll();
        userRepository.deleteAll();

        // Student A
        studentA = User.builder()
                .id(UUID.randomUUID().toString())
                .email("studenta@test.com")
                .username("studenta")
                .password("password")
                .role(Role.STUDENT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        studentA = userRepository.save(studentA);
        studentADetails = org.springframework.security.core.userdetails.User.withUsername("studenta@test.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        // Student B
        studentB = User.builder()
                .id(UUID.randomUUID().toString())
                .email("studentb@test.com")
                .username("studentb")
                .password("password")
                .role(Role.STUDENT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        studentB = userRepository.save(studentB);
        studentBDetails = org.springframework.security.core.userdetails.User.withUsername("studentb@test.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        // Admin User
        adminUser = User.builder()
                .id(UUID.randomUUID().toString())
                .email("admin@test.com")
                .username("adminuser")
                .password("password")
                .role(Role.SUPER_ADMIN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        adminUser = userRepository.save(adminUser);
        adminDetails = org.springframework.security.core.userdetails.User.withUsername("admin@test.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")))
                .build();

        // Student A task
        studentATask = PlannerTask.builder()
                .id(UUID.randomUUID())
                .userId(studentA.getId())
                .title("Student A Math Homework")
                .type(TaskType.ASSIGNMENT)
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.TODO)
                .dueAt(LocalDateTime.now().plusDays(2))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        studentATask = plannerTaskRepository.save(studentATask);

        // Public notice
        publicNotice = Notice.builder()
                .id(UUID.randomUUID())
                .title("Campus Midterm Exam Schedule")
                .slug("campus-midterm-exam-schedule")
                .content("Schedule content details")
                .category(NoticeCategory.EXAM)
                .priority(NoticePriority.HIGH)
                .visibility(NoticeVisibility.PUBLIC)
                .isPublished(true)
                .isPinned(false)
                .publishedAt(LocalDateTime.now().minusDays(1))
                .build();
        publicNotice = noticeRepository.save(publicNotice);
    }

    @AfterEach
    void tearDown() {
        attachmentRepository.findAll().forEach(att -> {
            try {
                storageService.delete(att.getStoredFileName());
            } catch (Exception ignored) {
            }
        });
        attachmentRepository.deleteAll();
        plannerTaskRepository.deleteAll();
        noticeRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testUploadValidFileForPlannerTask() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "homework.pdf",
                "application/pdf",
                "%PDF-1.4 Mock Homework Content".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/v1/attachments/upload")
                        .file(file)
                        .param("ownerType", AttachmentOwnerType.PLANNER_TASK.name())
                        .param("ownerId", studentATask.getId().toString())
                        .with(user(studentADetails)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.originalFileName").value("homework.pdf"))
                .andExpect(jsonPath("$.contentType").value("application/pdf"))
                .andExpect(jsonPath("$.ownerType").value("PLANNER_TASK"))
                .andExpect(jsonPath("$.ownerId").value(studentATask.getId().toString()))
                .andExpect(jsonPath("$.downloadUrl").isNotEmpty());

        List<Attachment> attachments = attachmentRepository.findByOwnerTypeAndOwnerId(
                AttachmentOwnerType.PLANNER_TASK, studentATask.getId());
        assertEquals(1, attachments.size());
        assertTrue(storageService.exists(attachments.get(0).getStoredFileName()));
    }

    @Test
    void testGetAttachmentsForOwner() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.pdf",
                "application/pdf",
                "%PDF-1.4 Sample Notes".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/v1/attachments/upload")
                        .file(file)
                        .param("ownerType", AttachmentOwnerType.PLANNER_TASK.name())
                        .param("ownerId", studentATask.getId().toString())
                        .with(user(studentADetails)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/attachments")
                        .param("ownerType", AttachmentOwnerType.PLANNER_TASK.name())
                        .param("ownerId", studentATask.getId().toString())
                        .with(user(studentADetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].originalFileName").value("notes.pdf"));
    }

    @Test
    void testDownloadAndViewAttachment() throws Exception {
        byte[] fileBytes = "%PDF-1.4 Downloadable Content".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "download_me.pdf",
                "application/pdf",
                fileBytes
        );

        String responseStr = mockMvc.perform(multipart("/api/v1/attachments/upload")
                        .file(file)
                        .param("ownerType", AttachmentOwnerType.PLANNER_TASK.name())
                        .param("ownerId", studentATask.getId().toString())
                        .with(user(studentADetails)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String attachmentId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(responseStr).get("id").asText();

        // Test Download (attachment disposition)
        mockMvc.perform(get("/api/v1/attachments/" + attachmentId + "/download")
                        .with(user(studentADetails)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment; filename=\"download_me.pdf\"")))
                .andExpect(content().bytes(fileBytes));

        // Test View (inline disposition)
        mockMvc.perform(get("/api/v1/attachments/" + attachmentId + "/view")
                        .with(user(studentADetails)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("inline; filename=\"download_me.pdf\"")))
                .andExpect(content().bytes(fileBytes));
    }

    @Test
    void testSingleAttachmentDeletion() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "to_delete.png",
                "image/png",
                new byte[]{(byte) 0x89, 'P', 'N', 'G', 1, 2, 3}
        );

        String responseStr = mockMvc.perform(multipart("/api/v1/attachments/upload")
                        .file(file)
                        .param("ownerType", AttachmentOwnerType.PLANNER_TASK.name())
                        .param("ownerId", studentATask.getId().toString())
                        .with(user(studentADetails)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String attachmentIdStr = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(responseStr).get("id").asText();
        UUID attachmentId = UUID.fromString(attachmentIdStr);

        Attachment att = attachmentRepository.findById(attachmentId).orElseThrow();
        String storedFileName = att.getStoredFileName();
        assertTrue(storageService.exists(storedFileName));

        // Delete attachment
        mockMvc.perform(delete("/api/v1/attachments/" + attachmentId)
                        .with(user(studentADetails)))
                .andExpect(status().isNoContent());

        assertFalse(attachmentRepository.existsById(attachmentId));
        assertFalse(storageService.exists(storedFileName));
    }

    @Test
    void testCascadingDeletionOnTaskRemoval() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "task_spec.pdf",
                "application/pdf",
                "%PDF-1.4 Task Document".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/v1/attachments/upload")
                        .file(file)
                        .param("ownerType", AttachmentOwnerType.PLANNER_TASK.name())
                        .param("ownerId", studentATask.getId().toString())
                        .with(user(studentADetails)))
                .andExpect(status().isCreated());

        Attachment att = attachmentRepository.findByOwnerTypeAndOwnerId(
                AttachmentOwnerType.PLANNER_TASK, studentATask.getId()).get(0);
        String storedFileName = att.getStoredFileName();
        assertTrue(storageService.exists(storedFileName));

        // Delete the planner task
        plannerTaskService.deleteTask(studentADetails, studentATask.getId());

        // Verify attachment metadata and file cascaded
        List<Attachment> remaining = attachmentRepository.findByOwnerTypeAndOwnerId(
                AttachmentOwnerType.PLANNER_TASK, studentATask.getId());
        assertTrue(remaining.isEmpty());
        assertFalse(storageService.exists(storedFileName));
    }

    @Test
    void testCascadingDeletionOnNoticeRemoval() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notice_attachment.pdf",
                "application/pdf",
                "%PDF-1.4 Notice Document".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/v1/attachments/upload")
                        .file(file)
                        .param("ownerType", AttachmentOwnerType.NOTICE.name())
                        .param("ownerId", publicNotice.getId().toString())
                        .with(user(adminDetails)))
                .andExpect(status().isCreated());

        Attachment att = attachmentRepository.findByOwnerTypeAndOwnerId(
                AttachmentOwnerType.NOTICE, publicNotice.getId()).get(0);
        String storedFileName = att.getStoredFileName();
        assertTrue(storageService.exists(storedFileName));

        // Delete the notice
        noticeService.deleteNotice(publicNotice.getId());

        // Verify attachment metadata and file cascaded
        List<Attachment> remaining = attachmentRepository.findByOwnerTypeAndOwnerId(
                AttachmentOwnerType.NOTICE, publicNotice.getId());
        assertTrue(remaining.isEmpty());
        assertFalse(storageService.exists(storedFileName));
    }

    @Test
    void testCrossUserTaskAccessRejection() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "intruder.pdf",
                "application/pdf",
                "%PDF-1.4 Intruder".getBytes(StandardCharsets.UTF_8)
        );

        // Student B tries to upload to Student A's task -> 404 / 403
        mockMvc.perform(multipart("/api/v1/attachments/upload")
                        .file(file)
                        .param("ownerType", AttachmentOwnerType.PLANNER_TASK.name())
                        .param("ownerId", studentATask.getId().toString())
                        .with(user(studentBDetails)))
                .andExpect(status().is4xxClientError());

        // Student A uploads legitimately
        String responseStr = mockMvc.perform(multipart("/api/v1/attachments/upload")
                        .file(file)
                        .param("ownerType", AttachmentOwnerType.PLANNER_TASK.name())
                        .param("ownerId", studentATask.getId().toString())
                        .with(user(studentADetails)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String attachmentId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(responseStr).get("id").asText();

        // Student B tries to download Student A's private task attachment -> 404 / 403
        mockMvc.perform(get("/api/v1/attachments/" + attachmentId + "/download")
                        .with(user(studentBDetails)))
                .andExpect(status().is4xxClientError());

        // Student B tries to delete Student A's attachment -> 404 / 403
        mockMvc.perform(delete("/api/v1/attachments/" + attachmentId)
                        .with(user(studentBDetails)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testNoticeUploadAdminOnly() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "unauthorized_notice_doc.pdf",
                "application/pdf",
                "%PDF-1.4 Notice Attachment".getBytes(StandardCharsets.UTF_8)
        );

        // Student A tries to upload to Notice -> rejected (403 Forbidden)
        mockMvc.perform(multipart("/api/v1/attachments/upload")
                        .file(file)
                        .param("ownerType", AttachmentOwnerType.NOTICE.name())
                        .param("ownerId", publicNotice.getId().toString())
                        .with(user(studentADetails)))
                .andExpect(status().isForbidden());

        // Admin uploads -> succeeds (201 Created)
        mockMvc.perform(multipart("/api/v1/attachments/upload")
                        .file(file)
                        .param("ownerType", AttachmentOwnerType.NOTICE.name())
                        .param("ownerId", publicNotice.getId().toString())
                        .with(user(adminDetails)))
                .andExpect(status().isCreated());
    }

    @Test
    void testDisallowedExecutableFileTypeRejected() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "exploit.exe",
                "application/x-msdownload",
                "MZ executable content".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/v1/attachments/upload")
                        .file(file)
                        .param("ownerType", AttachmentOwnerType.PLANNER_TASK.name())
                        .param("ownerId", studentATask.getId().toString())
                        .with(user(studentADetails)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testPathTraversalAttemptRejected() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "../../../../etc/passwd.pdf",
                "application/pdf",
                "%PDF-1.4 Malicious Traversal".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/v1/attachments/upload")
                        .file(file)
                        .param("ownerType", AttachmentOwnerType.PLANNER_TASK.name())
                        .param("ownerId", studentATask.getId().toString())
                        .with(user(studentADetails)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testOversizeFileRejected() throws Exception {
        // > 20 MB (21 MB dummy byte array)
        byte[] oversized = new byte[21 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "huge_file.pdf",
                "application/pdf",
                oversized
        );

        mockMvc.perform(multipart("/api/v1/attachments/upload")
                        .file(file)
                        .param("ownerType", AttachmentOwnerType.PLANNER_TASK.name())
                        .param("ownerId", studentATask.getId().toString())
                        .with(user(studentADetails)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSpoofedExtensionRejected() throws Exception {
        // Filename says .pdf but MIME type says image/png
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "spoofed.pdf",
                "image/png",
                "fake image content".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/v1/attachments/upload")
                        .file(file)
                        .param("ownerType", AttachmentOwnerType.PLANNER_TASK.name())
                        .param("ownerId", studentATask.getId().toString())
                        .with(user(studentADetails)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUnauthenticatedRequestsRejected() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "%PDF-1.4 Test".getBytes(StandardCharsets.UTF_8)
        );

        // Upload unauthenticated -> 401
        mockMvc.perform(multipart("/api/v1/attachments/upload")
                        .file(file)
                        .param("ownerType", AttachmentOwnerType.PLANNER_TASK.name())
                        .param("ownerId", studentATask.getId().toString()))
                .andExpect(status().isUnauthorized());

        // Get unauthenticated -> 401
        mockMvc.perform(get("/api/v1/attachments")
                        .param("ownerType", AttachmentOwnerType.PLANNER_TASK.name())
                        .param("ownerId", studentATask.getId().toString()))
                .andExpect(status().isUnauthorized());

        // Delete unauthenticated -> 401
        mockMvc.perform(delete("/api/v1/attachments/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }
}
