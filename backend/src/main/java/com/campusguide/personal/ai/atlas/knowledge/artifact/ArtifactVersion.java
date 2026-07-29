package com.campusguide.personal.ai.atlas.knowledge.artifact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;

/**
 * Encapsulates versioning and checksum info for KnowledgeArtifact.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtifactVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String version = "1.0.0";
    @Builder.Default
    private long revision = 1L;
    private String checksum;
    @Builder.Default
    private Instant timestamp = Instant.now();

    public static String computeChecksum(String content) {
        if (content == null) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return String.valueOf(content.hashCode());
        }
    }

    public static ArtifactVersion initial(String content) {
        return ArtifactVersion.builder()
                .version("1.0.0")
                .revision(1L)
                .checksum(computeChecksum(content))
                .timestamp(Instant.now())
                .build();
    }
}
