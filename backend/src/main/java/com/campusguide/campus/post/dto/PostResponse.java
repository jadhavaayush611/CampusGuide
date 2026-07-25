package com.campusguide.campus.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {

    private String id;

    private String title;

    private String content;

    private String authorId;

    private String communityId;

    private List<String> imageUrls;

    private Integer likeCount;

    private Integer commentCount;

    private Boolean isPinned;

    private Boolean isEdited;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
