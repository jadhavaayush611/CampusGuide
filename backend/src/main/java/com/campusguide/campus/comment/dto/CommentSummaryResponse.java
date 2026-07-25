package com.campusguide.campus.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentSummaryResponse {

    private String id;

    private String content;

    private String authorId;

    private LocalDateTime createdAt;
}
