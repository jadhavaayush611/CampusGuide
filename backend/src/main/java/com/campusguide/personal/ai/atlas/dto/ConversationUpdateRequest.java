package com.campusguide.personal.ai.atlas.dto;

import jakarta.validation.constraints.Pattern;
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
public class ConversationUpdateRequest {

    @Size(max = 120, message = "Title cannot exceed 120 characters")
    private String title;

    @Pattern(regexp = "^(ACTIVE|ARCHIVED|CLOSED)$", message = "Status must be ACTIVE, ARCHIVED, or CLOSED")
    private String status;

    private Map<String, Object> metadata;
}
