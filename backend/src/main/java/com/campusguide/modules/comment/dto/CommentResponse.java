package com.campusguide.modules.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private String id;

    private String content;

    private String authorId;

    private String postId;

    private Boolean isEdited;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
