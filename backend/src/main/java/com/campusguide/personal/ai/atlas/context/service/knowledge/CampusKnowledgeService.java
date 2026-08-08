package com.campusguide.personal.ai.atlas.context.service.knowledge;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * High-level provider-independent service providing reusable access to campus knowledge entities:
 * buildings, departments, faculty, office hours, laboratories, classrooms, student services,
 * announcements, events, navigation metadata, and emergency contacts.
 */
@Service
@Slf4j
public class CampusKnowledgeService {

    private final CampusKnowledgeProvider provider;

    @Autowired
    public CampusKnowledgeService(CampusKnowledgeProvider provider) {
        this.provider = provider != null ? provider : new InMemoryCampusKnowledgeProvider();
    }

    @Cacheable(value = "buildings")
    public List<BuildingInfo> getBuildings() {
        return provider.getBuildings();
    }

    @Cacheable(value = "buildings", key = "#query")
    public Optional<BuildingInfo> getBuilding(String query) {
        return provider.getBuildingByNameOrCode(query);
    }

    @Cacheable(value = "departments")
    public List<DepartmentInfo> getDepartments() {
        return provider.getDepartments();
    }

    @Cacheable(value = "departments", key = "#name")
    public Optional<DepartmentInfo> getDepartment(String name) {
        return provider.getDepartmentByName(name);
    }

    @Cacheable(value = "faculty")
    public List<FacultyInfo> getFaculty() {
        return provider.getFaculty();
    }

    @Cacheable(value = "faculty", key = "#query")
    public List<FacultyInfo> searchFaculty(String query) {
        return provider.searchFaculty(query);
    }

    @Cacheable(value = "faculty", key = "#facultyQuery")
    public List<OfficeHoursInfo> getOfficeHours(String facultyQuery) {
        return provider.getOfficeHoursByFaculty(facultyQuery);
    }

    @Cacheable(value = "laboratories")
    public List<LaboratoryInfo> getLaboratories() {
        return provider.getLaboratories();
    }

    @Cacheable(value = "laboratories", key = "#name")
    public Optional<LaboratoryInfo> getLaboratory(String name) {
        return provider.getLaboratoryByName(name);
    }

    @Cacheable(value = "classrooms")
    public List<ClassroomInfo> getClassrooms() {
        return provider.getClassrooms();
    }

    @Cacheable(value = "classrooms", key = "#roomNumber")
    public Optional<ClassroomInfo> getClassroom(String roomNumber) {
        return provider.getClassroomByRoomNumber(roomNumber);
    }

    @Cacheable(value = "studentServices")
    public List<StudentServiceInfo> getStudentServices() {
        return provider.getStudentServices();
    }

    @Cacheable(value = "studentServices", key = "#categoryOrQuery")
    public List<StudentServiceInfo> searchStudentServices(String categoryOrQuery) {
        return provider.searchStudentServices(categoryOrQuery);
    }

    public List<CampusAnnouncementInfo> getAnnouncements() {
        return provider.getActiveAnnouncements();
    }

    public List<CampusEventInfo> getUpcomingEvents() {
        return provider.getUpcomingEvents();
    }

    @Cacheable(value = "navigation", key = "{#origin, #destination}")
    public Optional<NavigationMetadataInfo> getNavigationRoute(String origin, String destination) {
        return provider.getNavigationRoute(origin, destination);
    }

    @Cacheable(value = "emergencyContacts")
    public List<EmergencyContactInfo> getEmergencyContacts() {
        return provider.getEmergencyContacts();
    }
}
