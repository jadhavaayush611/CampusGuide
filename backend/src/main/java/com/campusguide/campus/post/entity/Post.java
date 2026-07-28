package com.campusguide.campus.post.entity;

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

@Document(collection = "posts")
@CompoundIndexes({
    @CompoundIndex(name = "community_deleted_created_idx", def = "{'communityId': 1, 'isDeleted': 1, 'createdAt': -1}"),
    @CompoundIndex(name = "author_deleted_created_idx", def = "{'authorId': 1, 'isDeleted': 1, 'createdAt': -1}"),
    @CompoundIndex(name = "deleted_created_idx", def = "{'isDeleted': 1, 'createdAt': -1}")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    private String id;

    private String title;

    private String content;

    @Indexed
    private String authorId;

    @Indexed
    private String communityId;

    private List<String> imageUrls;

    private Integer likeCount;

    private Integer commentCount;

    private Boolean isPinned;

    private Boolean isEdited;

    private Boolean isDeleted;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public static class PostBuilder {
        public PostBuilder createdAt(Instant instant) {
            this.createdAt = instant;
            return this;
        }

        public PostBuilder createdAt(LocalDateTime dateTime) {
            this.createdAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }

        public PostBuilder updatedAt(Instant instant) {
            this.updatedAt = instant;
            return this;
        }

        public PostBuilder updatedAt(LocalDateTime dateTime) {
            this.updatedAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }
    }
}
