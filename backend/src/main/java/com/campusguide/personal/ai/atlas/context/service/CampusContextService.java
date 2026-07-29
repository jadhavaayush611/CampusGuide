package com.campusguide.personal.ai.atlas.context.service;

import com.campusguide.campus.notice.entity.Notice;
import com.campusguide.campus.notice.repository.NoticeRepository;
import com.campusguide.personal.ai.atlas.context.model.CampusContext;
import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsible for querying, summarizing, and normalizing Campus domain context.
 */
@Service
@Slf4j
public class CampusContextService {

    private static final int MAX_ANNOUNCEMENTS = 5;
    private final NoticeRepository noticeRepository;

    public CampusContextService(@Autowired(required = false) NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    /**
     * Queries, filters, and normalizes campus context with deterministic ordering and bounded limits.
     *
     * @param userId target user ID
     * @param request chat request
     * @return normalized CampusContext
     */
    public CampusContext getCampusContext(String userId, AtlasChatRequest request) {
        String location = "Main Campus";
        int activeEventsCount = 0;
        int activeNoticesCount = 0;
        List<String> announcements = new ArrayList<>();

        if (noticeRepository != null) {
            try {
                List<Notice> notices = noticeRepository.findByIsPublishedTrue();
                if (notices != null && !notices.isEmpty()) {
                    activeNoticesCount = notices.size();
                    // Deterministic sorting: Title asc
                    announcements = notices.stream()
                            .map(Notice::getTitle)
                            .filter(StringUtils::hasText)
                            .distinct()
                            .sorted(Comparator.naturalOrder())
                            .limit(MAX_ANNOUNCEMENTS)
                            .collect(Collectors.toList());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch published notices: {}", e.getMessage());
            }
        }

        String summary = String.format("Campus context summary: %s region (%d active notice(s)).", location, activeNoticesCount);

        return CampusContext.builder()
                .location(location)
                .activeEventsCount(activeEventsCount)
                .activeNoticesCount(activeNoticesCount)
                .announcements(announcements)
                .summary(summary)
                .build();
    }
}
