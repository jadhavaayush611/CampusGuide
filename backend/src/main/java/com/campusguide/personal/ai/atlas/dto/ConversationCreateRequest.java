package com.campusguide.personal.ai.atlas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationCreateRequest {

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 120, message = "Title cannot exceed 120 characters")
    private String title;

    @Size(max = 50, message = "Type cannot exceed 50 characters")
    private String type;

    private Map<String, Object> metadata;
}
