package com.campusguide.campus.notice.service;

import com.campusguide.campus.council.repository.CouncilRepository;
import com.campusguide.campus.notice.dto.*;
import com.campusguide.campus.notice.entity.Notice;
import com.campusguide.campus.notice.enums.NoticeCategory;
import com.campusguide.campus.notice.enums.NoticePriority;
import com.campusguide.campus.notice.enums.NoticeVisibility;
import com.campusguide.campus.notice.exception.DuplicateNoticeSlugException;
import com.campusguide.campus.notice.exception.NoticeNotFoundException;
import com.campusguide.campus.notice.exception.NoticeValidationException;
import com.campusguide.campus.notice.mapper.NoticeMapper;
import com.campusguide.campus.notice.repository.NoticeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @Mock
    private NoticeRepository noticeRepository;

    @Mock
    private CouncilRepository councilRepository;

    @Spy
    private NoticeMapper noticeMapper;

    @InjectMocks
    private NoticeService noticeService;

    private UUID noticeId;
    private UUID councilId;
    private Notice notice;
    private CreateNoticeRequest createRequest;
    private UpdateNoticeRequest updateRequest;

    @BeforeEach
    void setUp() {
        noticeId = UUID.randomUUID();
        councilId = UUID.randomUUID();

        notice = Notice.builder()
                .id(noticeId)
                .title("Midterm Exam Notice")
                .slug("midterm-exam-notice")
                .content("Midterm exams will commence next week.")
                .summary("Exam schedule")
                .category(NoticeCategory.EXAM)
                .priority(NoticePriority.HIGH)
                .visibility(NoticeVisibility.PUBLIC)
                .councilId(councilId)
                .publishedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .isPinned(false)
                .isPublished(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = CreateNoticeRequest.builder()
                .title("Midterm Exam Notice")
                .slug("midterm-exam-notice")
                .content("Midterm exams will commence next week.")
                .summary("Exam schedule")
                .category(NoticeCategory.EXAM)
                .priority(NoticePriority.HIGH)
                .visibility(NoticeVisibility.PUBLIC)
                .councilId(councilId)
                .publishedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .isPinned(false)
                .isPublished(true)
                .build();

        updateRequest = UpdateNoticeRequest.builder()
                .title("Updated Midterm Notice")
                .slug("updated-midterm-notice")
                .content("Updated content")
                .summary("Updated summary")
                .category(NoticeCategory.ACADEMIC)
                .priority(NoticePriority.URGENT)
                .visibility(NoticeVisibility.STUDENTS)
                .councilId(null)
                .publishedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(10))
                .isPinned(true)
                .isPublished(true)
                .build();
    }

    @Test
    void createNotice_Success_GlobalNotice() {
        createRequest.setCouncilId(null);
        when(noticeRepository.existsBySlug(createRequest.getSlug())).thenReturn(false);
        when(noticeRepository.save(any(Notice.class))).thenReturn(notice);

        NoticeResponse response = noticeService.createNotice(createRequest);

        assertNotNull(response);
        assertEquals("Midterm Exam Notice", response.getTitle());
        verify(councilRepository, never()).existsById(any());
        verify(noticeRepository).save(any(Notice.class));
    }

    @Test
    void createNotice_Success_CouncilNotice() {
        when(noticeRepository.existsBySlug(createRequest.getSlug())).thenReturn(false);
        when(councilRepository.existsById(councilId)).thenReturn(true);
        when(noticeRepository.save(any(Notice.class))).thenReturn(notice);

        NoticeResponse response = noticeService.createNotice(createRequest);

        assertNotNull(response);
        assertEquals(councilId, response.getCouncilId());
        verify(councilRepository).existsById(councilId);
    }

    @Test
    void createNotice_ThrowsDuplicateNoticeSlugException_WhenSlugExists() {
        when(noticeRepository.existsBySlug(createRequest.getSlug())).thenReturn(true);

        assertThrows(DuplicateNoticeSlugException.class, () -> noticeService.createNotice(createRequest));
        verify(noticeRepository, never()).save(any(Notice.class));
    }

    @Test
    void createNotice_ThrowsNoticeValidationException_WhenCouncilDoesNotExist() {
        when(noticeRepository.existsBySlug(createRequest.getSlug())).thenReturn(false);
        when(councilRepository.existsById(councilId)).thenReturn(false);

        assertThrows(NoticeValidationException.class, () -> noticeService.createNotice(createRequest));
        verify(noticeRepository, never()).save(any(Notice.class));
    }

    @Test
    void createNotice_ThrowsNoticeValidationException_WhenExpirationBeforePublication() {
        LocalDateTime now = LocalDateTime.now();
        createRequest.setCouncilId(null);
        createRequest.setPublishedAt(now);
        createRequest.setExpiresAt(now.minusDays(1));
        when(noticeRepository.existsBySlug(createRequest.getSlug())).thenReturn(false);

        assertThrows(NoticeValidationException.class, () -> noticeService.createNotice(createRequest));
        verify(noticeRepository, never()).save(any(Notice.class));
    }

    @Test
    void getAllNotices_PublicListing_FiltersAndSortsCorrectly() {
        LocalDateTime now = LocalDateTime.now();

        Notice normalHigh = Notice.builder()
                .id(UUID.randomUUID())
                .title("Normal High Priority")
                .slug("normal-high")
                .priority(NoticePriority.HIGH)
                .isPinned(false)
                .isPublished(true)
                .publishedAt(now.minusHours(2))
                .createdAt(now.minusHours(2))
                .build();

        Notice pinnedLow = Notice.builder()
                .id(UUID.randomUUID())
                .title("Pinned Low Priority")
                .slug("pinned-low")
                .priority(NoticePriority.LOW)
                .isPinned(true)
                .isPublished(true)
                .publishedAt(now.minusHours(5))
                .createdAt(now.minusHours(5))
                .build();

        Notice pinnedUrgent = Notice.builder()
                .id(UUID.randomUUID())
                .title("Pinned Urgent Priority")
                .slug("pinned-urgent")
                .priority(NoticePriority.URGENT)
                .isPinned(true)
                .isPublished(true)
                .publishedAt(now.minusHours(1))
                .createdAt(now.minusHours(1))
                .build();

        Notice expiredNotice = Notice.builder()
                .id(UUID.randomUUID())
                .title("Expired Notice")
                .slug("expired")
                .isPinned(false)
                .isPublished(true)
                .publishedAt(now.minusDays(10))
                .expiresAt(now.minusDays(1))
                .build();

        when(noticeRepository.findByIsPublishedTrue())
                .thenReturn(List.of(normalHigh, pinnedLow, pinnedUrgent, expiredNotice));

        List<NoticeResponse> responses = noticeService.getAllNotices(false);

        assertEquals(3, responses.size());
        // Pinned urgent first, pinned low second, normal high third
        assertEquals("Pinned Urgent Priority", responses.get(0).getTitle());
        assertEquals("Pinned Low Priority", responses.get(1).getTitle());
        assertEquals("Normal High Priority", responses.get(2).getTitle());
    }

    @Test
    void getNoticeById_Success() {
        when(noticeRepository.findById(noticeId)).thenReturn(Optional.of(notice));

        NoticeResponse response = noticeService.getNoticeById(noticeId);

        assertNotNull(response);
        assertEquals(noticeId, response.getId());
    }

    @Test
    void getNoticeById_ThrowsNoticeNotFoundException_WhenNotFound() {
        when(noticeRepository.findById(noticeId)).thenReturn(Optional.empty());

        assertThrows(NoticeNotFoundException.class, () -> noticeService.getNoticeById(noticeId));
    }

    @Test
    void getNoticeBySlug_Success() {
        when(noticeRepository.findBySlug("midterm-exam-notice")).thenReturn(Optional.of(notice));

        NoticeResponse response = noticeService.getNoticeBySlug("midterm-exam-notice");

        assertNotNull(response);
        assertEquals("midterm-exam-notice", response.getSlug());
    }

    @Test
    void updateNotice_Success() {
        when(noticeRepository.findById(noticeId)).thenReturn(Optional.of(notice));
        when(noticeRepository.existsBySlugAndIdNot(updateRequest.getSlug(), noticeId)).thenReturn(false);
        when(noticeRepository.save(any(Notice.class))).thenReturn(notice);

        NoticeResponse response = noticeService.updateNotice(noticeId, updateRequest);

        assertNotNull(response);
        verify(noticeRepository).save(any(Notice.class));
    }

    @Test
    void publishNotice_TogglesPublishState() {
        when(noticeRepository.findById(noticeId)).thenReturn(Optional.of(notice));
        when(noticeRepository.save(any(Notice.class))).thenReturn(notice);

        PublishNoticeRequest publishReq = new PublishNoticeRequest(false);
        NoticeResponse response = noticeService.publishNotice(noticeId, publishReq);

        assertNotNull(response);
        assertFalse(response.getIsPublished());
    }

    @Test
    void pinNotice_TogglesPinState() {
        when(noticeRepository.findById(noticeId)).thenReturn(Optional.of(notice));
        when(noticeRepository.save(any(Notice.class))).thenReturn(notice);

        PinNoticeRequest pinReq = new PinNoticeRequest(true);
        NoticeResponse response = noticeService.pinNotice(noticeId, pinReq);

        assertNotNull(response);
        assertTrue(response.getIsPinned());
    }

    @Test
    void deleteNotice_Success() {
        when(noticeRepository.findById(noticeId)).thenReturn(Optional.of(notice));

        noticeService.deleteNotice(noticeId);

        verify(noticeRepository).delete(notice);
    }
}
