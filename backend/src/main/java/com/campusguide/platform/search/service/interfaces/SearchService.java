package com.campusguide.platform.search.service.interfaces;

import com.campusguide.platform.search.dto.request.GlobalSearchRequest;
import com.campusguide.platform.search.dto.response.GlobalSearchResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

public interface SearchService {

    GlobalSearchResponse search(UserDetails userDetails, GlobalSearchRequest request, Pageable pageable);
}
