package com.campusguide.platform.auth.dto.request;

import com.campusguide.platform.auth.validation.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
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
public class LoginRequest {

    @NotBlank(message = ValidationMessages.EMAIL_OR_USERNAME_REQUIRED)
    private String emailOrUsername;

    @NotBlank(message = ValidationMessages.PASSWORD_REQUIRED)
    private String password;
}
