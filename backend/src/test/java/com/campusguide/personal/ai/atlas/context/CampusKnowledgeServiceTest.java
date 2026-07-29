package com.campusguide.personal.ai.atlas.context;

import com.campusguide.personal.ai.atlas.context.service.knowledge.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CampusKnowledgeServiceTest {

    private CampusKnowledgeService service;

    @BeforeEach
    void setUp() {
        service = new CampusKnowledgeService(new InMemoryCampusKnowledgeProvider());
    }

    @Test
    @DisplayName("CampusKnowledgeService queries buildings and codes correctly")
    void testGetBuildings() {
        List<BuildingInfo> buildings = service.getBuildings();
        assertFalse(buildings.isEmpty());

        Optional<BuildingInfo> csHall = service.getBuilding("CSH");
        assertTrue(csHall.isPresent());
        assertEquals("Turing Computer Science Hall", csHall.get().getName());
    }

    @Test
    @DisplayName("CampusKnowledgeService searches faculty and office hours")
    void testFacultyAndOfficeHours() {
        List<FacultyInfo> faculty = service.searchFaculty("Smith");
        assertFalse(faculty.isEmpty());
        assertEquals("Prof. John Smith", faculty.get(0).getName());

        List<OfficeHoursInfo> officeHours = service.getOfficeHours("fac-smith");
        assertFalse(officeHours.isEmpty());
        assertEquals("Tuesday", officeHours.get(0).getDayOfWeek());
    }

    @Test
    @DisplayName("CampusKnowledgeService accesses departments, labs, classrooms, services, events, emergency")
    void testAllKnowledgeCategories() {
        assertFalse(service.getDepartments().isEmpty());
        assertFalse(service.getLaboratories().isEmpty());
        assertFalse(service.getClassrooms().isEmpty());
        assertFalse(service.getStudentServices().isEmpty());
        assertFalse(service.getAnnouncements().isEmpty());
        assertFalse(service.getUpcomingEvents().isEmpty());
        assertTrue(service.getNavigationRoute("Turing Computer Science Hall", "Curie Science Complex").isPresent());
        assertFalse(service.getEmergencyContacts().isEmpty());
    }
}
