package com.campusguide.campus.notice.dto;

import com.campusguide.campus.notice.enums.NoticeCategory;
import com.campusguide.campus.notice.enums.NoticePriority;
import com.campusguide.campus.notice.enums.NoticeVisibility;
import com.campusguide.campus.notice.validation.ValidSlug;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateNoticeRequest {

    @NotBlank(message = "Title is mandatory")
    private String title;

    @NotBlank(message = "Slug is mandatory")
    @ValidSlug
    private String slug;

    @NotBlank(message = "Content is mandatory")
    private String content;

    private String summary;

    private NoticeCategory category;

    private NoticePriority priority;

    private NoticeVisibility visibility;

    private UUID councilId;

    private LocalDateTime publishedAt;

    private LocalDateTime expiresAt;

    private Boolean isPinned;

    private Boolean isPublished;
}
