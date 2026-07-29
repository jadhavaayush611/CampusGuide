package com.campusguide.personal.ai.atlas.context.service.knowledge;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    public List<BuildingInfo> getBuildings() {
        return provider.getBuildings();
    }

    public Optional<BuildingInfo> getBuilding(String query) {
        return provider.getBuildingByNameOrCode(query);
    }

    public List<DepartmentInfo> getDepartments() {
        return provider.getDepartments();
    }

    public Optional<DepartmentInfo> getDepartment(String name) {
        return provider.getDepartmentByName(name);
    }

    public List<FacultyInfo> getFaculty() {
        return provider.getFaculty();
    }

    public List<FacultyInfo> searchFaculty(String query) {
        return provider.searchFaculty(query);
    }

    public List<OfficeHoursInfo> getOfficeHours(String facultyQuery) {
        return provider.getOfficeHoursByFaculty(facultyQuery);
    }

    public List<LaboratoryInfo> getLaboratories() {
        return provider.getLaboratories();
    }

    public Optional<LaboratoryInfo> getLaboratory(String name) {
        return provider.getLaboratoryByName(name);
    }

    public List<ClassroomInfo> getClassrooms() {
        return provider.getClassrooms();
    }

    public Optional<ClassroomInfo> getClassroom(String roomNumber) {
        return provider.getClassroomByRoomNumber(roomNumber);
    }

    public List<StudentServiceInfo> getStudentServices() {
        return provider.getStudentServices();
    }

    public List<StudentServiceInfo> searchStudentServices(String categoryOrQuery) {
        return provider.searchStudentServices(categoryOrQuery);
    }

    public List<CampusAnnouncementInfo> getAnnouncements() {
        return provider.getActiveAnnouncements();
    }

    public List<CampusEventInfo> getUpcomingEvents() {
        return provider.getUpcomingEvents();
    }

    public Optional<NavigationMetadataInfo> getNavigationRoute(String origin, String destination) {
        return provider.getNavigationRoute(origin, destination);
    }

    public List<EmergencyContactInfo> getEmergencyContacts() {
        return provider.getEmergencyContacts();
    }
}
