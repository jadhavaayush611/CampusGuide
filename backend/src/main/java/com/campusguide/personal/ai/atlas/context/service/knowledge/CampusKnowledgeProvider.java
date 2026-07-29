package com.campusguide.personal.ai.atlas.context.service.knowledge;

import java.util.List;
import java.util.Optional;

/**
 * Provider-independent interface for campus knowledge access.
 * Abstraction for underlying SQL, vector search, external API, or memory providers.
 */
public interface CampusKnowledgeProvider {

    List<BuildingInfo> getBuildings();
    Optional<BuildingInfo> getBuildingByNameOrCode(String query);

    List<DepartmentInfo> getDepartments();
    Optional<DepartmentInfo> getDepartmentByName(String name);

    List<FacultyInfo> getFaculty();
    List<FacultyInfo> searchFaculty(String query);

    List<OfficeHoursInfo> getOfficeHoursByFaculty(String facultyIdOrName);

    List<LaboratoryInfo> getLaboratories();
    Optional<LaboratoryInfo> getLaboratoryByName(String name);

    List<ClassroomInfo> getClassrooms();
    Optional<ClassroomInfo> getClassroomByRoomNumber(String roomNumber);

    List<StudentServiceInfo> getStudentServices();
    List<StudentServiceInfo> searchStudentServices(String categoryOrQuery);

    List<CampusAnnouncementInfo> getActiveAnnouncements();

    List<CampusEventInfo> getUpcomingEvents();

    Optional<NavigationMetadataInfo> getNavigationRoute(String origin, String destination);

    List<EmergencyContactInfo> getEmergencyContacts();
}
