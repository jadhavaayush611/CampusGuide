package com.campusguide.campus.notice.entity;

import com.campusguide.campus.notice.enums.NoticeCategory;
import com.campusguide.campus.notice.enums.NoticePriority;
import com.campusguide.campus.notice.enums.NoticeVisibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Document(collection = "notices")
@CompoundIndexes({
    @CompoundIndex(name = "council_published_idx", def = "{'councilId': 1, 'isPublished': 1}")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notice {

    @Id
    @jakarta.validation.constraints.NotNull(message = "ID must not be null")
    private UUID id;

    @jakarta.validation.constraints.NotBlank(message = "Title must not be blank")
    @Indexed
    private String title;

    @jakarta.validation.constraints.NotBlank(message = "Slug must not be blank")
    @Indexed(unique = true)
    private String slug;

    @jakarta.validation.constraints.NotBlank(message = "Content must not be blank")
    private String content;

    private String summary;

    private NoticeCategory category;

    private NoticePriority priority;

    private NoticeVisibility visibility;

    @Indexed
    private UUID councilId;

    private LocalDateTime publishedAt;

    private LocalDateTime expiresAt;

    private Boolean isPinned;

    @Indexed
    private Boolean isPublished;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @org.springframework.data.annotation.Version
    private Long version;

    public static class NoticeBuilder {
        public NoticeBuilder createdAt(Instant instant) {
            this.createdAt = instant;
            return this;
        }

        public NoticeBuilder createdAt(LocalDateTime dateTime) {
            this.createdAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }

        public NoticeBuilder updatedAt(Instant instant) {
            this.updatedAt = instant;
            return this;
        }

        public NoticeBuilder updatedAt(LocalDateTime dateTime) {
            this.updatedAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }
    }
}
