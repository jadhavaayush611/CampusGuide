package com.campusguide.platform.user.service.impl;

import com.campusguide.common.exception.UnauthorisedException;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
import com.campusguide.platform.user.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CurrentUserServiceImpl implements CurrentUserService {

    private final UserRepository userRepository;

    @Override
    public User getCurrentUser(UserDetails userDetails) {
        if (userDetails != null && userDetails.getUsername() != null) {
            return getUserByIdentifier(userDetails.getUsername());
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof UserDetails ud) {
                return getUserByIdentifier(ud.getUsername());
            } else if (principal instanceof String str && !"anonymousUser".equalsIgnoreCase(str)) {
                return getUserByIdentifier(str);
            }
        }

        throw new UnauthorisedException("User is not authenticated");
    }

    @Override
    public User getUserByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new UnauthorisedException("User identifier is required");
        }

        return userRepository.findById(identifier)
                .orElseGet(() -> userRepository.findByEmail(identifier)
                        .orElseGet(() -> userRepository.findByUsername(identifier)
                                .orElseThrow(() -> new UnauthorisedException("User not found: " + identifier))));
    }

    @Override
    public String getCurrentUserIdString(UserDetails userDetails) {
        return getCurrentUser(userDetails).getId();
    }

    @Override
    public String getCurrentUserId(UserDetails userDetails) {
        return getCurrentUserIdString(userDetails);
    }

    @Override
    public String parseUserId(String idStr) {
        if (idStr == null || idStr.isBlank()) {
            throw new UnauthorisedException("User ID is missing");
        }
        return idStr;
    }
}
