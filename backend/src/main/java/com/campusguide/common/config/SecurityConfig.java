package com.campusguide.common.config;

import com.campusguide.platform.jwt.JwtAuthenticationFilter;
import com.campusguide.platform.auth.security.AuthRateLimitingFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Security configuration for CampusGuide.
 * Establishes stateless JWT-based HTTP security rules with method-level authorization enabled.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthRateLimitingFilter authRateLimitingFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(headers -> {
                headers.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; connect-src 'self'"));
                headers.frameOptions(frame -> frame.deny());
                headers.contentTypeOptions(org.springframework.security.config.Customizer.withDefaults());
                headers.referrerPolicy(referrer -> referrer.policy(ReferrerPolicy.NO_REFERRER));
                headers.permissionsPolicy(permissions -> permissions.policy("geolocation=(), microphone=(), camera=()"));
                headers.httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000));
            })
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    boolean isAuthenticated = (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) 
                            || request.getUserPrincipal() != null;
                    if (!isAuthenticated) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    }
                })
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/api/v1/auth/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/error",
                        "/api/v1/atlas/health",
                        "/api/v1/atlas/ready",
                        "/api/v1/atlas/live",
                        "/actuator/health",
                        "/actuator/health/**",
                        "/actuator/info"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(authRateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
