package com.campusguide.campus.notice.entity;

import com.campusguide.campus.notice.enums.NoticeCategory;
import com.campusguide.campus.notice.enums.NoticePriority;
import com.campusguide.campus.notice.enums.NoticeVisibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "notices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notice {

    @Id
    private UUID id;

    @Indexed
    private String title;

    @Indexed(unique = true)
    private String slug;

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

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
