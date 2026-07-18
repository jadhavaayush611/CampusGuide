package com.campusguide.modules.search.dto.request;

import com.campusguide.modules.search.enums.SearchType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalSearchRequest {

    @NotBlank(message = "Search query cannot be blank")
    private String query;

    private List<SearchType> types;
}
