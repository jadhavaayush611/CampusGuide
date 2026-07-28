package com.campusguide.campus.resource.entity;

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
import java.util.List;

@Document(collection = "resources")
@CompoundIndexes({
    @CompoundIndex(name = "uploader_deleted_created_idx", def = "{'uploaderId': 1, 'isDeleted': 1, 'createdAt': -1}"),
    @CompoundIndex(name = "council_deleted_created_idx", def = "{'councilId': 1, 'isDeleted': 1, 'createdAt': -1}"),
    @CompoundIndex(name = "community_deleted_created_idx", def = "{'communityId': 1, 'isDeleted': 1, 'createdAt': -1}"),
    @CompoundIndex(name = "deleted_created_idx", def = "{'isDeleted': 1, 'createdAt': -1}")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resource {

    @Id
    private String id;

    private String title;

    private String description;

    @Indexed
    private String uploaderId;

    @Indexed
    private String councilId;

    @Indexed
    private String communityId;

    private List<String> tags;

    private String fileName;

    private String originalFileName;

    private String fileType;

    private Long fileSize;

    private String downloadUrl;

    @Builder.Default
    private Boolean isDeleted = false;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public static class ResourceBuilder {
        public ResourceBuilder createdAt(Instant instant) {
            this.createdAt = instant;
            return this;
        }

        public ResourceBuilder createdAt(LocalDateTime dateTime) {
            this.createdAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }

        public ResourceBuilder updatedAt(Instant instant) {
            this.updatedAt = instant;
            return this;
        }

        public ResourceBuilder updatedAt(LocalDateTime dateTime) {
            this.updatedAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }
    }
}
