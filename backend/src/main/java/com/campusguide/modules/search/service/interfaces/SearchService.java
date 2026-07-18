package com.campusguide.modules.search.service.interfaces;

import com.campusguide.modules.search.dto.request.GlobalSearchRequest;
import com.campusguide.modules.search.dto.response.GlobalSearchResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

public interface SearchService {

    GlobalSearchResponse search(UserDetails userDetails, GlobalSearchRequest request, Pageable pageable);
}
