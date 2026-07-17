package com.campusguide.modules.ai.dto.request;

import com.campusguide.modules.ai.enums.MessageRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    @NotNull(message = "Role is required")
    private MessageRole role;

    @NotBlank(message = "Content is required")
    private String content;

    private Map<String, Object> metadata;
}
