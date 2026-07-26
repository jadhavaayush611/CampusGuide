package com.campusguide.platform.auth.dto.request;

import com.campusguide.platform.auth.validation.PasswordStrength;
import com.campusguide.platform.auth.validation.ValidationMessages;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = ValidationMessages.EMAIL_REQUIRED)
    @Email(message = ValidationMessages.INVALID_EMAIL_FORMAT)
    @Size(max = 255, message = ValidationMessages.EMAIL_MAX_LENGTH)
    private String email;

    @NotBlank(message = ValidationMessages.USERNAME_REQUIRED)
    @Size(min = 3, max = 50, message = ValidationMessages.USERNAME_SIZE)
    @Pattern(regexp = ValidationMessages.USERNAME_PATTERN_REGEXP, message = ValidationMessages.USERNAME_PATTERN)
    private String username;

    @NotBlank(message = ValidationMessages.PASSWORD_REQUIRED)
    @PasswordStrength(message = ValidationMessages.PASSWORD_STRENGTH)
    private String password;
}
