package com.campusguide.modules.council.controller;

import com.campusguide.modules.council.dto.CreateCouncilRequest;
import com.campusguide.modules.council.dto.UpdateCouncilRequest;
import com.campusguide.modules.council.dto.CouncilResponse;
import com.campusguide.modules.council.dto.CouncilSummaryResponse;
import com.campusguide.modules.council.service.CouncilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/councils")
@RequiredArgsConstructor
public class CouncilController {

    private final CouncilService councilService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CouncilResponse> createCouncil(@Valid @RequestBody CreateCouncilRequest request) {
        CouncilResponse response = councilService.createCouncil(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{councilId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CouncilResponse> updateCouncil(
            @PathVariable String councilId,
            @Valid @RequestBody UpdateCouncilRequest request) {
        CouncilResponse response = councilService.updateCouncil(councilId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CouncilSummaryResponse>> getAllCouncils() {
        List<CouncilSummaryResponse> response = councilService.getAllCouncils();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{councilId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CouncilResponse> getCouncilById(@PathVariable String councilId) {
        CouncilResponse response = councilService.getCouncilById(councilId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CouncilSummaryResponse>> getCouncilsByCategory(@PathVariable String category) {
        List<CouncilSummaryResponse> response = councilService.getCouncilsByCategory(category);
        return ResponseEntity.ok(response);
    }
}
