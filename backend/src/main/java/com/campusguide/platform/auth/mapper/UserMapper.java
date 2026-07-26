package com.campusguide.platform.auth.mapper;

import com.campusguide.platform.auth.dto.request.RegisterRequest;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.entity.UserRole;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(RegisterRequest request, String passwordHash) {
        if (request == null) {
            return null;
        }

        return User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .passwordHash(passwordHash)
                .role(UserRole.STUDENT)
                .enabled(true)
                .emailVerified(false)
                .build();
    }
}
