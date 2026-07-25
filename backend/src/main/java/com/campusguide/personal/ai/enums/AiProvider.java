package com.campusguide.personal.ai.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AiProvider {
    OPENAI,
    GEMINI,
    CLAUDE,
    OLLAMA,
    AZURE_OPENAI,
    UNKNOWN;

    @JsonCreator
    public static AiProvider fromString(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        try {
            return AiProvider.valueOf(value.toUpperCase().trim().replace('-', '_').replace(' ', '_'));
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
