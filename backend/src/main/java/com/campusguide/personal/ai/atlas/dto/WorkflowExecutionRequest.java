package com.campusguide.personal.ai.atlas.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowExecutionRequest {

    @NotBlank(message = "Workflow ID is required")
    @Size(max = 100, message = "Workflow ID exceeds maximum allowed length of 100 characters")
    private String workflowId;

    private Map<String, Object> parameters;

    @Builder.Default
    private Boolean async = false;

    @Min(value = 1, message = "Timeout must be at least 1 second")
    @Max(value = 3600, message = "Timeout cannot exceed 3600 seconds")
    private Integer timeoutSeconds;

    @Size(max = 10, message = "Maximum 10 attachments allowed")
    private List<String> attachments;

    @Pattern(regexp = "^(LOW|NORMAL|HIGH|URGENT)$", message = "Priority must be LOW, NORMAL, HIGH, or URGENT")
    private String priority;
}
