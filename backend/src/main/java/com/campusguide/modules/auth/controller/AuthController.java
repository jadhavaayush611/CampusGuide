package com.campusguide.modules.auth.controller;

import com.campusguide.modules.auth.dto.AuthResponse;
import com.campusguide.modules.auth.dto.LoginRequest;
import com.campusguide.modules.auth.dto.RegisterRequest;
import com.campusguide.modules.auth.service.AuthService;
import com.campusguide.modules.user.dto.UserResponse;
import com.campusguide.modules.user.entity.User;
import com.campusguide.modules.user.repository.UserRepository;
import com.campusguide.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .department(user.getDepartment())
                .year(user.getYear())
                .profilePictureUrl(user.getProfilePictureUrl())
                .phoneNumber(user.getPhoneNumber())
                .bio(user.getBio())
                .isPremium(user.getIsPremium())
                .isVerified(user.getIsVerified())
                .build();

        return ResponseEntity.ok(response);
    }
}
