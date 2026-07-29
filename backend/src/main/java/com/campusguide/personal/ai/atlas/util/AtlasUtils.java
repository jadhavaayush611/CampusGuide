package com.campusguide.personal.ai.atlas.util;

import lombok.experimental.UtilityClass;

import java.util.Map;

@UtilityClass
public class AtlasUtils {

    /**
     * Sanitizes user input prompt to remove non-printable characters.
     */
    public static String sanitizePrompt(String input) {
        if (input == null) {
            return "";
        }
        return input.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").trim();
    }

    /**
     * Estimates character or token count for basic validation.
     */
    public static int estimateTokens(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return Math.max(1, text.length() / 4);
    }

    /**
     * Formats a template string with placeholders.
     */
    public static String formatTemplate(String template, Map<String, Object> params) {
        if (template == null || params == null || params.isEmpty()) {
            return template;
        }
        String result = template;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                result = result.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
            }
        }
        return result;
    }
}
