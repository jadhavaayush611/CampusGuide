package com.campusguide.campus.notice.mapper;

import com.campusguide.campus.notice.dto.CreateNoticeRequest;
import com.campusguide.campus.notice.dto.NoticeResponse;
import com.campusguide.campus.notice.dto.UpdateNoticeRequest;
import com.campusguide.campus.notice.entity.Notice;
import com.campusguide.campus.notice.enums.NoticeCategory;
import com.campusguide.campus.notice.enums.NoticePriority;
import com.campusguide.campus.notice.enums.NoticeVisibility;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class NoticeMapper {

    public Notice toEntity(CreateNoticeRequest request) {
        if (request == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        boolean published = Boolean.TRUE.equals(request.getIsPublished());
        LocalDateTime publishedAt = request.getPublishedAt();
        if (published && publishedAt == null) {
            publishedAt = now;
        }

        return Notice.builder()
                .id(UUID.randomUUID())
                .title(trim(request.getTitle()))
                .slug(trim(request.getSlug()))
                .content(trim(request.getContent()))
                .summary(trim(request.getSummary()))
                .category(request.getCategory() != null ? request.getCategory() : NoticeCategory.GENERAL)
                .priority(request.getPriority() != null ? request.getPriority() : NoticePriority.MEDIUM)
                .visibility(request.getVisibility() != null ? request.getVisibility() : NoticeVisibility.PUBLIC)
                .councilId(request.getCouncilId())
                .publishedAt(publishedAt)
                .expiresAt(request.getExpiresAt())
                .isPinned(request.getIsPinned() != null ? request.getIsPinned() : false)
                .isPublished(published)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void updateEntityFromRequest(Notice notice, UpdateNoticeRequest request) {
        if (notice == null || request == null) {
            return;
        }

        notice.setTitle(trim(request.getTitle()));
        notice.setSlug(trim(request.getSlug()));
        notice.setContent(trim(request.getContent()));
        notice.setSummary(trim(request.getSummary()));
        if (request.getCategory() != null) {
            notice.setCategory(request.getCategory());
        }
        if (request.getPriority() != null) {
            notice.setPriority(request.getPriority());
        }
        if (request.getVisibility() != null) {
            notice.setVisibility(request.getVisibility());
        }
        notice.setCouncilId(request.getCouncilId());

        if (request.getIsPublished() != null) {
            boolean wasPublished = Boolean.TRUE.equals(notice.getIsPublished());
            boolean nowPublished = Boolean.TRUE.equals(request.getIsPublished());
            notice.setIsPublished(nowPublished);
            if (nowPublished && !wasPublished && request.getPublishedAt() == null) {
                notice.setPublishedAt(LocalDateTime.now());
            } else if (request.getPublishedAt() != null) {
                notice.setPublishedAt(request.getPublishedAt());
            }
        } else if (request.getPublishedAt() != null) {
            notice.setPublishedAt(request.getPublishedAt());
        }

        if (request.getExpiresAt() != null) {
            notice.setExpiresAt(request.getExpiresAt());
        }
        if (request.getIsPinned() != null) {
            notice.setIsPinned(request.getIsPinned());
        }
        notice.setUpdatedAt(LocalDateTime.now());
    }

    public NoticeResponse toResponse(Notice notice) {
        if (notice == null) {
            return null;
        }

        return NoticeResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .slug(notice.getSlug())
                .content(notice.getContent())
                .summary(notice.getSummary())
                .category(notice.getCategory())
                .priority(notice.getPriority())
                .visibility(notice.getVisibility())
                .councilId(notice.getCouncilId())
                .publishedAt(notice.getPublishedAt())
                .expiresAt(notice.getExpiresAt())
                .isPinned(notice.getIsPinned())
                .isPublished(notice.getIsPublished())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build();
    }

    public List<NoticeResponse> toResponseList(List<Notice> notices) {
        if (notices == null) {
            return Collections.emptyList();
        }

        return notices.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private String trim(String value) {
        return value != null ? value.trim() : null;
    }
}
