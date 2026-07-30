package com.campusguide.personal.ai.atlas.service;

import com.campusguide.personal.ai.atlas.dto.ExecutionStatusResponse;
import com.campusguide.personal.ai.atlas.dto.WorkflowExecutionRequest;
import com.campusguide.personal.ai.atlas.dto.WorkflowExecutionResponse;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface AtlasWorkflowService {

    WorkflowExecutionResponse executeWorkflow(WorkflowExecutionRequest request, UserDetails userDetails);

    ExecutionStatusResponse getExecutionStatus(String executionId, UserDetails userDetails);

    List<ExecutionStatusResponse> getExecutionHistory(UserDetails userDetails);

    ExecutionStatusResponse cancelExecution(String executionId, String reason, UserDetails userDetails);
}
