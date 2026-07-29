package com.campusguide.personal.ai.atlas.knowledge.embedding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates batch embedding requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<String> texts;
    private String model;
    @Builder.Default
    private Map<String, Object> parameters = new HashMap<>();

    public static EmbeddingRequest of(String text) {
        return EmbeddingRequest.builder()
                .texts(List.of(text))
                .model("text-embedding-3-small")
                .build();
    }

    public static EmbeddingRequest of(List<String> texts, String model) {
        return EmbeddingRequest.builder()
                .texts(texts)
                .model(model != null ? model : "text-embedding-3-small")
                .build();
    }
}
