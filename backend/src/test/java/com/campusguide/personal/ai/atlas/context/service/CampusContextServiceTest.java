package com.campusguide.personal.ai.atlas.context.service;

import com.campusguide.campus.notice.entity.Notice;
import com.campusguide.campus.notice.repository.NoticeRepository;
import com.campusguide.personal.ai.atlas.context.model.CampusContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampusContextServiceTest {

    @Mock
    private NoticeRepository noticeRepository;

    private CampusContextService campusContextService;

    @BeforeEach
    void setUp() {
        campusContextService = new CampusContextService(noticeRepository);
    }

    @Test
    void testGetCampusContext_WithPublishedNotices() {
        Notice n1 = Notice.builder().id(UUID.randomUUID()).title("Campus Maintenance").isPublished(true).build();
        Notice n2 = Notice.builder().id(UUID.randomUUID()).title("Library Hours").isPublished(true).build();

        when(noticeRepository.findByIsPublishedTrue()).thenReturn(List.of(n1, n2));

        CampusContext context = campusContextService.getCampusContext("user-1", null);

        assertNotNull(context);
        assertEquals("Main Campus", context.getLocation());
        assertEquals(2, context.getActiveNoticesCount());
        assertEquals(2, context.getAnnouncements().size());
        assertEquals("Campus Maintenance", context.getAnnouncements().get(0));
    }
}
