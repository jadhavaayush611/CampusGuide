package com.campusguide.modules.search.dto.response;

import com.campusguide.modules.search.enums.SearchType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultResponse {

    private String id;
    private String title;
    private String description;
    private SearchType searchType;
    private Double relevanceScore;
    private Map<String, Object> metadata;
}
