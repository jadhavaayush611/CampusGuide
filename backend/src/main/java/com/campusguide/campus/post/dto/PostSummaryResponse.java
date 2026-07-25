package com.campusguide.campus.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostSummaryResponse {

    private String id;

    private String title;

    private String authorId;

    private String communityId;

    private Integer likeCount;

    private Integer commentCount;

    private LocalDateTime createdAt;
}
