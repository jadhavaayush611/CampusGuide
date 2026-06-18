package com.campusguide.modules.user.dto;

import com.campusguide.modules.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private String department;
    private Integer year;
    private String profilePictureUrl;
    private String phoneNumber;
    private String bio;
    private Boolean isPremium;
    private Boolean isVerified;
}
