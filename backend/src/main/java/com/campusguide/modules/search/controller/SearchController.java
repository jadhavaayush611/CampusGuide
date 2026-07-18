package com.campusguide.modules.search.controller;

import com.campusguide.modules.search.dto.request.GlobalSearchRequest;
import com.campusguide.modules.search.dto.response.GlobalSearchResponse;
import com.campusguide.modules.search.service.interfaces.SearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class SearchController {

    private final SearchService searchService;

    @PostMapping
    public ResponseEntity<GlobalSearchResponse> search(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody GlobalSearchRequest request,
            @PageableDefault(size = 10) Pageable pageable) {
        GlobalSearchResponse response = searchService.search(userDetails, request, pageable);
        return ResponseEntity.ok(response);
    }
}
