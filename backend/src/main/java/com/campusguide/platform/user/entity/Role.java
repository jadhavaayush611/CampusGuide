package com.campusguide.platform.user.entity;

public enum Role {
    STUDENT,
    FACULTY,
    COUNCIL_ADMIN,
    SUPER_ADMIN;

    public static Role fromString(String roleStr) {
        if (roleStr == null) return STUDENT;
        String upper = roleStr.toUpperCase().trim();
        if ("ADMIN".equals(upper)) return SUPER_ADMIN;
        try {
            return Role.valueOf(upper);
        } catch (IllegalArgumentException e) {
            return STUDENT;
        }
    }
}
