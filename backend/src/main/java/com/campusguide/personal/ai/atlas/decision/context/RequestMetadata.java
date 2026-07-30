package com.campusguide.personal.ai.atlas.decision.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Metadata associated with incoming request triggering the decision.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String requestId = UUID.randomUUID().toString();

    @Builder.Default
    private Instant timestamp = Instant.now();

    @Builder.Default
    private String requestType = "USER_INTERACTION";

    @Builder.Default
    private String sourceChannel = "WEB_APP";

    @Builder.Default
    private String traceId = "trc_" + UUID.randomUUID().toString().substring(0, 8);

    @Builder.Default
    private String clientVersion = "1.0.0";

    public static RequestMetadata create(String requestId, String sourceChannel) {
        return RequestMetadata.builder()
                .requestId(requestId != null ? requestId : UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .requestType("USER_INTERACTION")
                .sourceChannel(sourceChannel != null ? sourceChannel : "WEB_APP")
                .traceId("trc_" + UUID.randomUUID().toString().substring(0, 8))
                .clientVersion("1.0.0")
                .build();
    }
}
