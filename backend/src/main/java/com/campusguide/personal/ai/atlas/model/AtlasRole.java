package com.campusguide.personal.ai.atlas.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AtlasRole {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant");

    private final String value;

    AtlasRole(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static AtlasRole fromValue(String value) {
        if (value == null) {
            return USER;
        }
        for (AtlasRole role : AtlasRole.values()) {
            if (role.value.equalsIgnoreCase(value) || role.name().equalsIgnoreCase(value)) {
                return role;
            }
        }
        return USER;
    }
}
