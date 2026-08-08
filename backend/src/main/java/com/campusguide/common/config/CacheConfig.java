package com.campusguide.common.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_BUILDINGS = "buildings";
    public static final String CACHE_DEPARTMENTS = "departments";
    public static final String CACHE_FACULTY = "faculty";
    public static final String CACHE_LABORATORIES = "laboratories";
    public static final String CACHE_CLASSROOMS = "classrooms";
    public static final String CACHE_STUDENT_SERVICES = "studentServices";
    public static final String CACHE_EMERGENCY_CONTACTS = "emergencyContacts";
    public static final String CACHE_NAVIGATION = "navigation";
    public static final String CACHE_COUNCILS = "councils";

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(List.of(
                CACHE_BUILDINGS,
                CACHE_DEPARTMENTS,
                CACHE_FACULTY,
                CACHE_LABORATORIES,
                CACHE_CLASSROOMS,
                CACHE_STUDENT_SERVICES,
                CACHE_EMERGENCY_CONTACTS,
                CACHE_NAVIGATION,
                CACHE_COUNCILS
        ));
        return cacheManager;
    }
}
