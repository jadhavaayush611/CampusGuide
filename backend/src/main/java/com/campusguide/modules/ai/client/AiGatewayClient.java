package com.campusguide.modules.ai.client;

import com.campusguide.modules.ai.dto.gateway.AiGatewayRequest;
import com.campusguide.modules.ai.dto.gateway.AiGatewayResponse;
import com.campusguide.modules.ai.exception.AiGatewayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiGatewayClient {

    private final RestClient aiGatewayRestClient;

    public AiGatewayResponse sendRequest(AiGatewayRequest request) {
        log.info("AI Gateway: Starting request [correlationId: {}] for conversation ID: {}", 
                request.getCorrelationId(), request.getConversationId());
        long startTime = System.currentTimeMillis();

        try {
            AiGatewayResponse response = aiGatewayRestClient.post()
                    .uri("/api/v1/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, resp) -> {
                        log.error("AI Gateway: Request failed [correlationId: {}] with status code: {}", 
                                request.getCorrelationId(), resp.getStatusCode());
                        throw new AiGatewayException("AI Gateway returned an error status: " + resp.getStatusCode());
                    })
                    .body(AiGatewayResponse.class);

            long duration = System.currentTimeMillis() - startTime;
            log.info("AI Gateway: Response received [correlationId: {}] for conversation ID: {}. Processing took {} ms", 
                    request.getCorrelationId(), request.getConversationId(), duration);

            if (response == null) {
                log.error("AI Gateway: Received null response body [correlationId: {}]", request.getCorrelationId());
                throw new AiGatewayException("AI Gateway returned empty response");
            }

            return response;
        } catch (ResourceAccessException e) {
            log.error("AI Gateway: Resource access exception (timeout or unavailable) [correlationId: {}]: {}", 
                    request.getCorrelationId(), e.getMessage());
            throw new AiGatewayException("AI Gateway is unavailable or request timed out", e);
        } catch (AiGatewayException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI Gateway: Unexpected failure during call [correlationId: {}]: {}", 
                    request.getCorrelationId(), e.getMessage());
            throw new AiGatewayException("AI Gateway request failed due to unexpected error", e);
        }
    }
}
