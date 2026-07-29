package com.campusguide.personal.ai.atlas.prompt.model;

import com.campusguide.personal.ai.atlas.util.AtlasUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modular context section representation capturing title, formatted content, domain category,
 * priority, required flag, and token estimations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContextSection {
    private String title;
    private String content;
    private String category;
    private int priority;
    private boolean required;
    private int estimatedTokens;

    public static ContextSection of(String title, String content, String category, int priority, boolean required) {
        String safeTitle = title != null ? title : "";
        String safeContent = content != null ? content : "";
        int tokens = AtlasUtils.estimateTokens(safeTitle + "\n" + safeContent);
        return ContextSection.builder()
                .title(safeTitle)
                .content(safeContent)
                .category(category)
                .priority(priority)
                .required(required)
                .estimatedTokens(tokens)
                .build();
    }
}
