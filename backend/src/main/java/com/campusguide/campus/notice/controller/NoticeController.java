package com.campusguide.campus.notice.controller;

import com.campusguide.campus.notice.dto.*;
import com.campusguide.campus.notice.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NoticeResponse> createNotice(@Valid @RequestBody CreateNoticeRequest request) {
        NoticeResponse response = noticeService.createNotice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NoticeResponse>> getAllNotices(
            @RequestParam(required = false, defaultValue = "false") Boolean includeUnpublished) {
        List<NoticeResponse> response = noticeService.getAllNotices(includeUnpublished);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NoticeResponse> getNoticeById(@PathVariable UUID id) {
        NoticeResponse response = noticeService.getNoticeById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/slug/{slug}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NoticeResponse> getNoticeBySlug(@PathVariable String slug) {
        NoticeResponse response = noticeService.getNoticeBySlug(slug);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NoticeResponse> updateNotice(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateNoticeRequest request) {
        NoticeResponse response = noticeService.updateNotice(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NoticeResponse> publishNotice(
            @PathVariable UUID id,
            @RequestBody(required = false) PublishNoticeRequest request) {
        NoticeResponse response = noticeService.publishNotice(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/pin")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NoticeResponse> pinNotice(
            @PathVariable UUID id,
            @RequestBody(required = false) PinNoticeRequest request) {
        NoticeResponse response = noticeService.pinNotice(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteNotice(@PathVariable UUID id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.noContent().build();
    }
}
