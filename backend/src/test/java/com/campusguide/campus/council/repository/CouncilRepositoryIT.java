package com.campusguide.campus.council.repository;

import com.campusguide.campus.council.entity.Council;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CouncilRepositoryIT {

    @Autowired
    private CouncilRepository councilRepository;

    private Council council1;
    private Council council2;

    @BeforeEach
    void setUp() {
        councilRepository.deleteAll();

        council1 = Council.builder()
                .id(UUID.randomUUID())
                .name("Technical Council")
                .slug("tech-council")
                .description("Handles all technical clubs")
                .logoUrl("https://example.com/tech.png")
                .email("tech@campus.edu")
                .contactNumber("+1234567890")
                .facultyAdvisor("Dr. Smith")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        council2 = Council.builder()
                .id(UUID.randomUUID())
                .name("Cultural Council")
                .slug("cultural-council")
                .description("Handles all cultural clubs")
                .logoUrl("https://example.com/cultural.png")
                .email("cultural@campus.edu")
                .contactNumber("+0987654321")
                .facultyAdvisor("Dr. Jones")
                .isActive(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        councilRepository.save(council1);
        councilRepository.save(council2);
    }

    @AfterEach
    void tearDown() {
        councilRepository.deleteAll();
    }

    @Test
    void testFindById() {
        Optional<Council> found = councilRepository.findById(council1.getId());
        assertTrue(found.isPresent());
        assertEquals("Technical Council", found.get().getName());
    }

    @Test
    void testFindBySlug() {
        Optional<Council> found = councilRepository.findBySlug("cultural-council");
        assertTrue(found.isPresent());
        assertEquals(council2.getId(), found.get().getId());
    }

    @Test
    void testFindByName() {
        Optional<Council> found = councilRepository.findByName("Technical Council");
        assertTrue(found.isPresent());
        assertEquals("tech-council", found.get().getSlug());
    }

    @Test
    void testExistsByName() {
        assertTrue(councilRepository.existsByName("Technical Council"));
        assertFalse(councilRepository.existsByName("Non Existent Council"));
    }

    @Test
    void testExistsBySlug() {
        assertTrue(councilRepository.existsBySlug("tech-council"));
        assertFalse(councilRepository.existsBySlug("non-existent-slug"));
    }

    @Test
    void testExistsByNameAndIdNot() {
        assertTrue(councilRepository.existsByNameAndIdNot("Technical Council", council2.getId()));
        assertFalse(councilRepository.existsByNameAndIdNot("Technical Council", council1.getId()));
    }

    @Test
    void testExistsBySlugAndIdNot() {
        assertTrue(councilRepository.existsBySlugAndIdNot("tech-council", council2.getId()));
        assertFalse(councilRepository.existsBySlugAndIdNot("tech-council", council1.getId()));
    }
}
