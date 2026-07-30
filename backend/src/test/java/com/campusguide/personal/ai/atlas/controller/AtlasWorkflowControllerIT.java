package com.campusguide.personal.ai.atlas.controller;

import com.campusguide.personal.ai.atlas.dto.ExecutionStatusResponse;
import com.campusguide.personal.ai.atlas.dto.WorkflowExecutionRequest;
import com.campusguide.personal.ai.atlas.dto.WorkflowExecutionResponse;
import com.campusguide.personal.ai.atlas.exception.AtlasForbiddenException;
import com.campusguide.personal.ai.atlas.exception.AtlasNotFoundException;
import com.campusguide.personal.ai.atlas.exception.AtlasRateLimitException;
import com.campusguide.personal.ai.atlas.service.AtlasWorkflowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AtlasWorkflowControllerIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AtlasWorkflowService workflowService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public AtlasWorkflowService mockWorkflowService() {
            return mock(AtlasWorkflowService.class);
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        reset(workflowService);
    }

    @Test
    void testExecuteWorkflow_Unauthenticated_ReturnsUnauthorized() throws Exception {
        WorkflowExecutionRequest request = WorkflowExecutionRequest.builder()
                .workflowId("academic_advising_workflow")
                .build();

        mockMvc.perform(post("/api/v1/atlas/workflows/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user1", roles = {"STUDENT"})
    void testExecuteWorkflow_Success_ReturnsAccepted() throws Exception {
        WorkflowExecutionRequest request = WorkflowExecutionRequest.builder()
                .workflowId("academic_advising_workflow")
                .priority("HIGH")
                .build();

        WorkflowExecutionResponse response = WorkflowExecutionResponse.builder()
                .executionId("exec_1234")
                .workflowId("academic_advising_workflow")
                .status("RUNNING")
                .startedAt(Instant.now())
                .result(Map.of())
                .build();

        when(workflowService.executeWorkflow(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/atlas/workflows/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.executionId").value("exec_1234"))
                .andExpect(jsonPath("$.workflowId").value("academic_advising_workflow"));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"STUDENT"})
    void testExecuteWorkflow_QuotaExceeded_ReturnsTooManyRequests() throws Exception {
        WorkflowExecutionRequest request = WorkflowExecutionRequest.builder()
                .workflowId("academic_advising_workflow")
                .build();

        when(workflowService.executeWorkflow(any(), any()))
                .thenThrow(new AtlasRateLimitException("Concurrent workflow execution limit reached"));

        mockMvc.perform(post("/api/v1/atlas/workflows/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("Concurrent workflow execution limit reached"));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"STUDENT"})
    void testGetExecutionStatus_Success() throws Exception {
        ExecutionStatusResponse statusResponse = ExecutionStatusResponse.builder()
                .executionId("exec_1234")
                .workflowId("academic_advising_workflow")
                .userId("user1")
                .status("COMPLETED")
                .progressPercent(100)
                .currentStep("FINISHED")
                .build();

        when(workflowService.getExecutionStatus(eq("exec_1234"), any())).thenReturn(statusResponse);

        mockMvc.perform(get("/api/v1/atlas/workflows/executions/exec_1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionId").value("exec_1234"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"STUDENT"})
    void testGetExecutionStatus_ForbiddenForNonOwner() throws Exception {
        when(workflowService.getExecutionStatus(eq("exec_9999"), any()))
                .thenThrow(new AtlasForbiddenException("Access denied: You do not own this workflow execution"));

        mockMvc.perform(get("/api/v1/atlas/workflows/executions/exec_9999"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied: You do not own this workflow execution"));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"STUDENT"})
    void testGetExecutionHistory_Success() throws Exception {
        ExecutionStatusResponse status1 = ExecutionStatusResponse.builder()
                .executionId("exec_1234")
                .workflowId("academic_advising_workflow")
                .status("COMPLETED")
                .build();

        when(workflowService.getExecutionHistory(any())).thenReturn(List.of(status1));

        mockMvc.perform(get("/api/v1/atlas/workflows/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].executionId").value("exec_1234"));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"STUDENT"})
    void testCancelExecution_Success() throws Exception {
        ExecutionStatusResponse statusResponse = ExecutionStatusResponse.builder()
                .executionId("exec_1234")
                .status("CANCELLED")
                .build();

        when(workflowService.cancelExecution(eq("exec_1234"), any(), any())).thenReturn(statusResponse);

        mockMvc.perform(post("/api/v1/atlas/workflows/executions/exec_1234/cancel?reason=User_cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
