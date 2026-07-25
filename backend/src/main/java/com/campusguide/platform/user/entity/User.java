package com.campusguide.platform.user.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private String id;

    private String email;

    private String password;

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

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
