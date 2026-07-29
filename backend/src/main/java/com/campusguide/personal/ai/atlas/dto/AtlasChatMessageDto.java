package com.campusguide.personal.ai.atlas.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtlasChatMessageDto {
    @NotBlank(message = "Role cannot be blank")
    private String role;

    @NotBlank(message = "Content cannot be blank")
    private String content;
}
