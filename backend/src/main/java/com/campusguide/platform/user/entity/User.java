package com.campusguide.platform.user.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private String id;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be valid")
    @Indexed(unique = true)
    private String email;

    @NotBlank(message = "Username must not be blank")
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    @Indexed(unique = true)
    private String username;

    @NotBlank(message = "Password hash must not be blank")
    private String passwordHash;

    @NotNull(message = "Role must not be null")
    private UserRole role;

    @Builder.Default
    private boolean enabled = true;

    @Builder.Default
    private boolean emailVerified = false;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public String getPassword() {
        return this.passwordHash;
    }

    public static class UserBuilder {
        public UserBuilder password(String password) {
            this.passwordHash = password;
            return this;
        }

        public UserBuilder firstName(String firstName) {
            return this;
        }

        public UserBuilder lastName(String lastName) {
            return this;
        }

        public UserBuilder role(Object roleObj) {
            if (roleObj instanceof UserRole ur) {
                this.role = ur;
            } else if (roleObj != null) {
                try {
                    this.role = UserRole.valueOf(roleObj.toString());
                } catch (Exception ignored) {
                    this.role = roleObj.toString().contains("ADMIN") ? UserRole.ADMIN : UserRole.STUDENT;
                }
            }
            return this;
        }

        public UserBuilder createdAt(Instant instant) {
            this.createdAt = instant;
            return this;
        }

        public UserBuilder createdAt(LocalDateTime dateTime) {
            this.createdAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }

        public UserBuilder updatedAt(Instant instant) {
            this.updatedAt = instant;
            return this;
        }

        public UserBuilder updatedAt(LocalDateTime dateTime) {
            this.updatedAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }
    }
}
