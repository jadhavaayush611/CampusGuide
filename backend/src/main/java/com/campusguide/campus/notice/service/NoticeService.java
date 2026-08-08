package com.campusguide.campus.notice.service;

import com.campusguide.campus.council.repository.CouncilRepository;
import com.campusguide.campus.notice.dto.*;
import com.campusguide.campus.notice.entity.Notice;
import com.campusguide.campus.notice.enums.NoticePriority;
import com.campusguide.campus.notice.exception.DuplicateNoticeSlugException;
import com.campusguide.campus.notice.exception.NoticeNotFoundException;
import com.campusguide.campus.notice.exception.NoticeValidationException;
import com.campusguide.campus.notice.mapper.NoticeMapper;
import com.campusguide.campus.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final CouncilRepository councilRepository;
    private final NoticeMapper noticeMapper;

    public static final Comparator<Notice> NOTICE_COMPARATOR = Comparator
            .comparing((Notice n) -> Boolean.TRUE.equals(n.getIsPinned()), Comparator.reverseOrder())
            .thenComparing(Notice::getPriority, NoticePriority.byWeightDesc())
            .thenComparing(n -> n.getPublishedAt() != null ? n.getPublishedAt() : (n.getCreatedAt() != null ? java.time.LocalDateTime.ofInstant(n.getCreatedAt(), java.time.ZoneId.systemDefault()) : null), Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(n -> n.getCreatedAt() != null ? java.time.LocalDateTime.ofInstant(n.getCreatedAt(), java.time.ZoneId.systemDefault()) : null, Comparator.nullsLast(Comparator.reverseOrder()));

    public NoticeResponse createNotice(CreateNoticeRequest request) {
        if (noticeRepository.existsBySlug(request.getSlug())) {
            throw new DuplicateNoticeSlugException("Notice with slug '" + request.getSlug() + "' already exists");
        }

        validateCouncilExists(request.getCouncilId());

        Notice notice = noticeMapper.toEntity(request);
        validateDates(notice.getPublishedAt(), notice.getExpiresAt());

        Notice saved = noticeRepository.save(notice);
        return noticeMapper.toResponse(saved);
    }

    public List<NoticeResponse> getAllNotices(Boolean includeUnpublished) {
        LocalDateTime now = LocalDateTime.now();
        List<Notice> notices;

        boolean allowUnpublished = Boolean.TRUE.equals(includeUnpublished) && isCurrentUserAdmin();
        if (allowUnpublished) {
            notices = new java.util.ArrayList<>(noticeRepository.findAll());
        } else {
            notices = new java.util.ArrayList<>(noticeRepository.findActiveNotices(now));
        }

        notices.sort(NOTICE_COMPARATOR);
        return noticeMapper.toResponseList(notices);
    }

    private boolean isCurrentUserAdmin() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream().anyMatch(a ->
                "ROLE_SUPER_ADMIN".equals(a.getAuthority()) || "ROLE_COUNCIL_ADMIN".equals(a.getAuthority()));
    }

    public NoticeResponse getNoticeById(UUID id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new NoticeNotFoundException("Notice not found with ID: " + id));
        return noticeMapper.toResponse(notice);
    }

    public NoticeResponse getNoticeBySlug(String slug) {
        Notice notice = noticeRepository.findBySlug(slug)
                .orElseThrow(() -> new NoticeNotFoundException("Notice not found with slug: " + slug));
        return noticeMapper.toResponse(notice);
    }

    public NoticeResponse updateNotice(UUID id, UpdateNoticeRequest request) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new NoticeNotFoundException("Notice not found with ID: " + id));

        if (noticeRepository.existsBySlugAndIdNot(request.getSlug(), id)) {
            throw new DuplicateNoticeSlugException("Notice with slug '" + request.getSlug() + "' already exists");
        }

        validateCouncilExists(request.getCouncilId());

        noticeMapper.updateEntityFromRequest(notice, request);
        validateDates(notice.getPublishedAt(), notice.getExpiresAt());

        Notice updated = noticeRepository.save(notice);
        return noticeMapper.toResponse(updated);
    }

    public NoticeResponse publishNotice(UUID id, PublishNoticeRequest request) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new NoticeNotFoundException("Notice not found with ID: " + id));

        boolean targetPublished = request == null || request.getIsPublished() == null || Boolean.TRUE.equals(request.getIsPublished());
        notice.setIsPublished(targetPublished);

        if (targetPublished && notice.getPublishedAt() == null) {
            notice.setPublishedAt(LocalDateTime.now());
        }
        notice.setUpdatedAt(Instant.now());

        validateDates(notice.getPublishedAt(), notice.getExpiresAt());

        Notice updated = noticeRepository.save(notice);
        return noticeMapper.toResponse(updated);
    }

    public NoticeResponse pinNotice(UUID id, PinNoticeRequest request) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new NoticeNotFoundException("Notice not found with ID: " + id));

        boolean targetPinned = request == null || request.getIsPinned() == null || Boolean.TRUE.equals(request.getIsPinned());
        notice.setIsPinned(targetPinned);
        notice.setUpdatedAt(Instant.now());

        Notice updated = noticeRepository.save(notice);
        return noticeMapper.toResponse(updated);
    }

    public void deleteNotice(UUID id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new NoticeNotFoundException("Notice not found with ID: " + id));
        noticeRepository.delete(notice);
    }

    private void validateCouncilExists(UUID councilId) {
        if (councilId != null && !councilRepository.existsById(councilId)) {
            throw new NoticeValidationException("Referenced council does not exist with ID: " + councilId);
        }
    }

    private void validateDates(LocalDateTime publishedAt, LocalDateTime expiresAt) {
        if (publishedAt != null && expiresAt != null && !expiresAt.isAfter(publishedAt)) {
            throw new NoticeValidationException("Expiration date must be after publication date");
        }
    }
}
