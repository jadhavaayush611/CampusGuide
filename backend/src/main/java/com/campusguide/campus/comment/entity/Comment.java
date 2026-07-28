package com.campusguide.campus.comment.entity;

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

@Document(collection = "comments")
@CompoundIndexes({
    @CompoundIndex(name = "post_deleted_created_idx", def = "{'postId': 1, 'isDeleted': 1, 'createdAt': 1}"),
    @CompoundIndex(name = "author_deleted_idx", def = "{'authorId': 1, 'isDeleted': 1}"),
    @CompoundIndex(name = "deleted_created_idx", def = "{'isDeleted': 1, 'createdAt': -1}")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    private String id;

    @Indexed
    private String postId;

    @Indexed
    private String authorId;

    private String content;

    private Boolean isEdited;

    private Boolean isDeleted;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public static class CommentBuilder {
        public CommentBuilder createdAt(Instant instant) {
            this.createdAt = instant;
            return this;
        }

        public CommentBuilder createdAt(LocalDateTime dateTime) {
            this.createdAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }

        public CommentBuilder updatedAt(Instant instant) {
            this.updatedAt = instant;
            return this;
        }

        public CommentBuilder updatedAt(LocalDateTime dateTime) {
            this.updatedAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }
    }
}
