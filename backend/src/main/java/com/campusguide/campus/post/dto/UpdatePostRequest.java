package com.campusguide.campus.post.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePostRequest {

    @Size(max = 150, message = "Title cannot exceed 150 characters")
    private String title;

    private String content;

    private List<String> imageUrls;
}
