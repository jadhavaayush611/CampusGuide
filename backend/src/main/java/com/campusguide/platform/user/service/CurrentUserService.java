package com.campusguide.platform.user.service;

import com.campusguide.platform.user.entity.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public interface CurrentUserService {

    User getCurrentUser(UserDetails userDetails);

    User getUserByIdentifier(String identifier);

    String getCurrentUserIdString(UserDetails userDetails);

    String getCurrentUserId(UserDetails userDetails);

    String parseUserId(String idStr);
}
