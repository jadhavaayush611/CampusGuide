package com.campusguide.platform.search.controller;

import com.campusguide.platform.search.dto.request.GlobalSearchRequest;
import com.campusguide.platform.search.dto.response.GlobalSearchResponse;
import com.campusguide.platform.search.dto.response.SearchResultResponse;
import com.campusguide.platform.search.enums.SearchType;
import com.campusguide.platform.search.service.interfaces.SearchService;
import com.campusguide.platform.user.entity.Role;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SearchControllerSecurityIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private SearchService searchService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User studentUser;
    private UserDetails studentDetails;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        studentUser = User.builder()
                .email("student@campusguide.com")
                .password("password")
                .role(Role.STUDENT)
                .firstName("Student")
                .lastName("User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        studentUser = userRepository.save(studentUser);

        studentDetails = org.springframework.security.core.userdetails.User.withUsername("student@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void search_Unauthenticated_ReturnsUnauthorized() throws Exception {
        GlobalSearchRequest request = GlobalSearchRequest.builder().query("Java").build();

        mockMvc.perform(post("/api/v1/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void search_Authenticated_Success() throws Exception {
        GlobalSearchRequest request = GlobalSearchRequest.builder().query("Java").build();

        GlobalSearchResponse mockResponse = GlobalSearchResponse.builder()
                .query("Java")
                .totalResults(1)
                .results(List.of(SearchResultResponse.builder()
                        .id("c1")
                        .title("Java Course")
                        .description("Intro to Java")
                        .searchType(SearchType.COURSE)
                        .relevanceScore(1.0)
                        .metadata(Collections.emptyMap())
                        .build()))
                .build();

        when(searchService.search(any(UserDetails.class), any(GlobalSearchRequest.class), any(Pageable.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/search")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value("Java"))
                .andExpect(jsonPath("$.totalResults").value(1))
                .andExpect(jsonPath("$.results[0].id").value("c1"))
                .andExpect(jsonPath("$.results[0].title").value("Java Course"));
    }

    @Test
    void search_BlankQuery_ReturnsBadRequest() throws Exception {
        GlobalSearchRequest request = GlobalSearchRequest.builder().query("   ").build();

        mockMvc.perform(post("/api/v1/search")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
