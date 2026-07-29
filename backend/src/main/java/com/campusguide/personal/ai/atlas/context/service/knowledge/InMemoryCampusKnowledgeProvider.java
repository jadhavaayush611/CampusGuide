package com.campusguide.personal.ai.atlas.context.service.knowledge;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Default in-memory implementation of CampusKnowledgeProvider.
 * Provides realistic campus datasets for buildings, departments, faculty, office hours,
 * laboratories, classrooms, student services, announcements, events, navigation, and emergency contacts.
 */
@Component
public class InMemoryCampusKnowledgeProvider implements CampusKnowledgeProvider {

    private final List<BuildingInfo> buildings = new ArrayList<>();
    private final List<DepartmentInfo> departments = new ArrayList<>();
    private final List<FacultyInfo> facultyList = new ArrayList<>();
    private final List<OfficeHoursInfo> officeHoursList = new ArrayList<>();
    private final List<LaboratoryInfo> laboratories = new ArrayList<>();
    private final List<ClassroomInfo> classrooms = new ArrayList<>();
    private final List<StudentServiceInfo> studentServices = new ArrayList<>();
    private final List<CampusAnnouncementInfo> announcements = new ArrayList<>();
    private final List<CampusEventInfo> events = new ArrayList<>();
    private final List<NavigationMetadataInfo> navigationRoutes = new ArrayList<>();
    private final List<EmergencyContactInfo> emergencyContacts = new ArrayList<>();

    public InMemoryCampusKnowledgeProvider() {
        seedKnowledgeData();
    }

    private void seedKnowledgeData() {
        // Buildings
        buildings.add(BuildingInfo.builder()
                .buildingId("bld-cs")
                .name("Turing Computer Science Hall")
                .code("CSH")
                .latitude(37.7749)
                .longitude(-122.4194)
                .address("100 Innovation Way")
                .operatingHours("07:00 - 22:00")
                .departments(List.of("Computer Science", "Data Science"))
                .build());
        buildings.add(BuildingInfo.builder()
                .buildingId("bld-sci")
                .name("Curie Science Complex")
                .code("CSC")
                .latitude(37.7752)
                .longitude(-122.4180)
                .address("200 Discovery Blvd")
                .operatingHours("08:00 - 20:00")
                .departments(List.of("Physics", "Chemistry"))
                .build());

        // Departments
        departments.add(DepartmentInfo.builder()
                .deptId("dept-cs")
                .name("Computer Science")
                .code("CS")
                .buildingId("bld-cs")
                .headOfDepartment("Dr. Alan Turing")
                .contactEmail("cs-dept@campus.edu")
                .phone("555-0101")
                .build());

        // Faculty
        facultyList.add(FacultyInfo.builder()
                .facultyId("fac-smith")
                .name("Prof. John Smith")
                .title("Associate Professor")
                .department("Computer Science")
                .email("jsmith@campus.edu")
                .officeRoom("CSH-304")
                .researchAreas(List.of("Artificial Intelligence", "Algorithms"))
                .build());
        facultyList.add(FacultyInfo.builder()
                .facultyId("fac-davis")
                .name("Prof. Sarah Davis")
                .title("Professor & Head")
                .department("Data Science")
                .email("sdavis@campus.edu")
                .officeRoom("CSH-412")
                .researchAreas(List.of("Machine Learning", "Big Data"))
                .build());

        // Office Hours
        officeHoursList.add(OfficeHoursInfo.builder()
                .id("oh-1")
                .facultyId("fac-smith")
                .facultyName("Prof. John Smith")
                .dayOfWeek("Tuesday")
                .startTime("14:00")
                .endTime("16:00")
                .location("CSH-304")
                .notes("Walk-in or appointment via portal")
                .build());

        // Laboratories
        laboratories.add(LaboratoryInfo.builder()
                .labId("lab-ai")
                .name("AI & Robotics Laboratory")
                .buildingId("bld-cs")
                .roomNumber("CSH-210")
                .capacity(30)
                .equipmentList(List.of("GPU Cluster", "Robotic Arms", "VR Sets"))
                .build());

        // Classrooms
        classrooms.add(ClassroomInfo.builder()
                .classroomId("cls-101")
                .roomNumber("CSH-101")
                .buildingId("bld-cs")
                .capacity(120)
                .features(List.of("Dual Projector", "Lecture Capture", "Accessible Seating"))
                .build());

        // Student Services
        studentServices.add(StudentServiceInfo.builder()
                .serviceId("srv-financial")
                .name("Student Financial Aid & Registrar")
                .category("Administration")
                .location("Student Union Hall, Room 102")
                .contactInfo("finaid@campus.edu")
                .operatingHours("09:00 - 17:00")
                .build());

        // Announcements
        announcements.add(CampusAnnouncementInfo.builder()
                .announcementId("ann-1")
                .title("Midterm Examination Schedule Released")
                .content("The Spring Semester midterm timetable is now available on the student portal.")
                .category("Academic")
                .priority("HIGH")
                .publishedAt(System.currentTimeMillis() - 86400000L)
                .expiresAt(System.currentTimeMillis() + 604800000L)
                .build());

        // Events
        events.add(CampusEventInfo.builder()
                .eventId("evt-1")
                .title("Campus Hackathon 2026")
                .description("Annual 24-hour coding challenge with industry sponsors.")
                .location("Turing Computer Science Hall")
                .startTime("2026-08-15 09:00")
                .endTime("2026-08-16 12:00")
                .organizer("CS Student Association")
                .build());

        // Navigation
        navigationRoutes.add(NavigationMetadataInfo.builder()
                .routeId("nav-cs-sci")
                .origin("Turing Computer Science Hall")
                .destination("Curie Science Complex")
                .distanceMeters(350.0)
                .estimatedWalkMinutes(4)
                .accessible(true)
                .build());

        // Emergency Contacts
        emergencyContacts.add(EmergencyContactInfo.builder()
                .contactId("emg-campus-police")
                .serviceName("Campus Public Safety & Security")
                .phoneNumber("555-0911")
                .altPhone("555-9999")
                .location("Central Campus Safety Gatehouse")
                .available24x7(true)
                .build());
    }

    @Override
    public List<BuildingInfo> getBuildings() {
        return new ArrayList<>(buildings);
    }

    @Override
    public Optional<BuildingInfo> getBuildingByNameOrCode(String query) {
        if (query == null || query.isBlank()) return Optional.empty();
        String lower = query.toLowerCase();
        return buildings.stream()
                .filter(b -> b.getName().toLowerCase().contains(lower) || b.getCode().toLowerCase().contains(lower))
                .findFirst();
    }

    @Override
    public List<DepartmentInfo> getDepartments() {
        return new ArrayList<>(departments);
    }

    @Override
    public Optional<DepartmentInfo> getDepartmentByName(String name) {
        if (name == null || name.isBlank()) return Optional.empty();
        String lower = name.toLowerCase();
        return departments.stream()
                .filter(d -> d.getName().toLowerCase().contains(lower) || d.getCode().toLowerCase().contains(lower))
                .findFirst();
    }

    @Override
    public List<FacultyInfo> getFaculty() {
        return new ArrayList<>(facultyList);
    }

    @Override
    public List<FacultyInfo> searchFaculty(String query) {
        if (query == null || query.isBlank()) return getFaculty();
        String lower = query.toLowerCase();
        return facultyList.stream()
                .filter(f -> f.getName().toLowerCase().contains(lower) || f.getDepartment().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    @Override
    public List<OfficeHoursInfo> getOfficeHoursByFaculty(String facultyIdOrName) {
        if (facultyIdOrName == null || facultyIdOrName.isBlank()) return new ArrayList<>(officeHoursList);
        String lower = facultyIdOrName.toLowerCase();
        return officeHoursList.stream()
                .filter(oh -> oh.getFacultyId().toLowerCase().contains(lower) || oh.getFacultyName().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    @Override
    public List<LaboratoryInfo> getLaboratories() {
        return new ArrayList<>(laboratories);
    }

    @Override
    public Optional<LaboratoryInfo> getLaboratoryByName(String name) {
        if (name == null || name.isBlank()) return Optional.empty();
        String lower = name.toLowerCase();
        return laboratories.stream()
                .filter(l -> l.getName().toLowerCase().contains(lower) || l.getRoomNumber().toLowerCase().contains(lower))
                .findFirst();
    }

    @Override
    public List<ClassroomInfo> getClassrooms() {
        return new ArrayList<>(classrooms);
    }

    @Override
    public Optional<ClassroomInfo> getClassroomByRoomNumber(String roomNumber) {
        if (roomNumber == null || roomNumber.isBlank()) return Optional.empty();
        String lower = roomNumber.toLowerCase();
        return classrooms.stream()
                .filter(c -> c.getRoomNumber().toLowerCase().contains(lower))
                .findFirst();
    }

    @Override
    public List<StudentServiceInfo> getStudentServices() {
        return new ArrayList<>(studentServices);
    }

    @Override
    public List<StudentServiceInfo> searchStudentServices(String categoryOrQuery) {
        if (categoryOrQuery == null || categoryOrQuery.isBlank()) return getStudentServices();
        String lower = categoryOrQuery.toLowerCase();
        return studentServices.stream()
                .filter(s -> s.getName().toLowerCase().contains(lower) || s.getCategory().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    @Override
    public List<CampusAnnouncementInfo> getActiveAnnouncements() {
        return new ArrayList<>(announcements);
    }

    @Override
    public List<CampusEventInfo> getUpcomingEvents() {
        return new ArrayList<>(events);
    }

    @Override
    public Optional<NavigationMetadataInfo> getNavigationRoute(String origin, String destination) {
        if (origin == null || destination == null) return Optional.empty();
        String oLower = origin.toLowerCase();
        String dLower = destination.toLowerCase();
        return navigationRoutes.stream()
                .filter(r -> r.getOrigin().toLowerCase().contains(oLower) && r.getDestination().toLowerCase().contains(dLower))
                .findFirst();
    }

    @Override
    public List<EmergencyContactInfo> getEmergencyContacts() {
        return new ArrayList<>(emergencyContacts);
    }
}
