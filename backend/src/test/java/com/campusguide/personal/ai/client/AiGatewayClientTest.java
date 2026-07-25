package com.campusguide.personal.ai.client;

import com.campusguide.personal.ai.dto.gateway.AiGatewayRequest;
import com.campusguide.personal.ai.dto.gateway.AiGatewayResponse;
import com.campusguide.personal.ai.enums.AiProvider;
import com.campusguide.personal.ai.exception.AiGatewayException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class AiGatewayClientTest {

    private AiGatewayClient aiGatewayClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:8000");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        aiGatewayClient = new AiGatewayClient(restClient);
    }

    @Test
    void sendRequest_Success() {
        String jsonResponse = """
                {
                  "response": "Hello back!",
                  "model": "gpt-4",
                  "provider": "openai",
                  "tokensUsed": 10,
                  "processingTime": 0.5,
                  "metadata": {}
                }
                """;

        mockServer.expect(requestTo("http://localhost:8000/api/v1/chat"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        AiGatewayRequest request = AiGatewayRequest.builder()
                .correlationId("corr-123")
                .conversationId("conv-123")
                .conversationType("GENERAL_CHAT")
                .userMessage("Hello")
                .conversationHistory(new ArrayList<>())
                .metadata(new HashMap<>())
                .build();

        AiGatewayResponse response = aiGatewayClient.sendRequest(request);

        assertNotNull(response);
        assertEquals("Hello back!", response.getResponse());
        assertEquals("gpt-4", response.getModel());
        assertEquals(AiProvider.OPENAI, response.getProvider());
        assertEquals(10, response.getTokensUsed());
        assertEquals(0.5, response.getProcessingTime());
        mockServer.verify();
    }

    @Test
    void sendRequest_HttpError_ThrowsAiGatewayException() {
        mockServer.expect(requestTo("http://localhost:8000/api/v1/chat"))
                .andRespond(withServerError());

        AiGatewayRequest request = AiGatewayRequest.builder()
                .correlationId("corr-123")
                .conversationId("conv-123")
                .conversationType("GENERAL_CHAT")
                .userMessage("Hello")
                .conversationHistory(new ArrayList<>())
                .metadata(new HashMap<>())
                .build();

        assertThrows(AiGatewayException.class, () -> aiGatewayClient.sendRequest(request));
        mockServer.verify();
    }

    @Test
    void sendRequest_InvalidResponse_ThrowsAiGatewayException() {
        mockServer.expect(requestTo("http://localhost:8000/api/v1/chat"))
                .andRespond(withSuccess("not a json", MediaType.APPLICATION_JSON));

        AiGatewayRequest request = AiGatewayRequest.builder()
                .correlationId("corr-123")
                .conversationId("conv-123")
                .conversationType("GENERAL_CHAT")
                .userMessage("Hello")
                .conversationHistory(new ArrayList<>())
                .metadata(new HashMap<>())
                .build();

        assertThrows(AiGatewayException.class, () -> aiGatewayClient.sendRequest(request));
        mockServer.verify();
    }

    @Test
    void sendRequest_Timeout_ThrowsAiGatewayException() {
        mockServer.expect(requestTo("http://localhost:8000/api/v1/chat"))
                .andRespond(withException(new java.io.IOException("Timeout")));

        AiGatewayRequest request = AiGatewayRequest.builder()
                .correlationId("corr-123")
                .conversationId("conv-123")
                .conversationType("GENERAL_CHAT")
                .userMessage("Hello")
                .conversationHistory(new ArrayList<>())
                .metadata(new HashMap<>())
                .build();

        assertThrows(AiGatewayException.class, () -> aiGatewayClient.sendRequest(request));
        mockServer.verify();
    }
}
