package com.campusguide.platform.user.dto;

import com.campusguide.platform.user.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String email;
    private String username;
    private UserRole role;
    private boolean enabled;
    private boolean emailVerified;
    private Instant createdAt;
    private Instant updatedAt;
}
