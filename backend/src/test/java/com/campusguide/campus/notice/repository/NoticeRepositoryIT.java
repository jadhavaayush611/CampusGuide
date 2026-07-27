package com.campusguide.campus.notice.repository;

import com.campusguide.campus.notice.entity.Notice;
import com.campusguide.campus.notice.enums.NoticeCategory;
import com.campusguide.campus.notice.enums.NoticePriority;
import com.campusguide.campus.notice.enums.NoticeVisibility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NoticeRepositoryIT {

    @Autowired
    private NoticeRepository noticeRepository;

    private Notice notice1;
    private Notice notice2;
    private UUID councilId;

    @BeforeEach
    void setUp() {
        noticeRepository.deleteAll();
        councilId = UUID.randomUUID();

        notice1 = Notice.builder()
                .id(UUID.randomUUID())
                .title("Exam Schedule Out")
                .slug("exam-schedule-out")
                .content("End-semester examination schedule has been released.")
                .summary("Exam schedule details")
                .category(NoticeCategory.EXAM)
                .priority(NoticePriority.HIGH)
                .visibility(NoticeVisibility.PUBLIC)
                .councilId(councilId)
                .publishedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(10))
                .isPinned(true)
                .isPublished(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        notice2 = Notice.builder()
                .id(UUID.randomUUID())
                .title("Campus Fest Announcement")
                .slug("campus-fest-announcement")
                .content("Annual cultural fest dates announced.")
                .summary("Fest details")
                .category(NoticeCategory.EVENT)
                .priority(NoticePriority.MEDIUM)
                .visibility(NoticeVisibility.PUBLIC)
                .councilId(null)
                .publishedAt(LocalDateTime.now())
                .expiresAt(null)
                .isPinned(false)
                .isPublished(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        noticeRepository.save(notice1);
        noticeRepository.save(notice2);
    }

    @AfterEach
    void tearDown() {
        noticeRepository.deleteAll();
    }

    @Test
    void testFindById() {
        Optional<Notice> found = noticeRepository.findById(notice1.getId());
        assertTrue(found.isPresent());
        assertEquals("Exam Schedule Out", found.get().getTitle());
    }

    @Test
    void testFindBySlug() {
        Optional<Notice> found = noticeRepository.findBySlug("exam-schedule-out");
        assertTrue(found.isPresent());
        assertEquals(notice1.getId(), found.get().getId());
    }

    @Test
    void testExistsBySlug() {
        assertTrue(noticeRepository.existsBySlug("exam-schedule-out"));
        assertFalse(noticeRepository.existsBySlug("non-existent-slug"));
    }

    @Test
    void testExistsBySlugAndIdNot() {
        assertTrue(noticeRepository.existsBySlugAndIdNot("exam-schedule-out", notice2.getId()));
        assertFalse(noticeRepository.existsBySlugAndIdNot("exam-schedule-out", notice1.getId()));
    }

    @Test
    void testFindByIsPublishedTrue() {
        List<Notice> publishedNotices = noticeRepository.findByIsPublishedTrue();
        assertEquals(1, publishedNotices.size());
        assertEquals("Exam Schedule Out", publishedNotices.get(0).getTitle());
    }

    @Test
    void testFindByCouncilId() {
        List<Notice> councilNotices = noticeRepository.findByCouncilId(councilId);
        assertEquals(1, councilNotices.size());
        assertEquals(notice1.getId(), councilNotices.get(0).getId());
    }
}
