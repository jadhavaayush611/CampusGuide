package com.campusguide.platform.analytics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AnalyticsControllerSecurityIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    private UserDetails adminDetails;
    private UserDetails studentDetails;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        adminDetails = User.withUsername("admin@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")))
                .build();

        studentDetails = User.withUsername("student@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();
    }

    @Test
    void getDashboardSummary_AdminRole_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/dashboard")
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").exists())
                .andExpect(jsonPath("$.generatedAt").exists());
    }

    @Test
    void getDashboardSummary_StudentRole_Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/dashboard")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getDashboardSummary_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserStatistics_AdminRole_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/users")
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").exists());
    }

    @Test
    void getUserStatistics_StudentRole_Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/users")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getEventStatistics_AdminRole_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/events")
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").exists());
    }

    @Test
    void getCommunityStatistics_AdminRole_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/communities")
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").exists());
    }
}
