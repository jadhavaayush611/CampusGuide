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
                .buildingId("bld-vesit")
                .name("VES Institute of Technology")
                .code("VESIT")
                .latitude(19.0468)
                .longitude(72.8893)
                .address("Hashu Advani Memorial Complex, Collector's Colony, Chembur, Mumbai, Maharashtra 400074")
                .operatingHours("08:00 - 18:00")
                .departments(List.of("ECS", "AIDS", "CMPN", "AURO", "EXTC", "IT"))
                .build());
        buildings.add(BuildingInfo.builder()
                .buildingId("bld-vescop")
                .name("VES College of Pharmacy")
                .code("VESCOP")
                .latitude(19.0465)
                .longitude(72.8885)
                .address("Hashu Advani Memorial Complex, Chembur, Mumbai")
                .operatingHours("09:00 - 17:00")
                .departments(List.of("Pharmacy"))
                .build());
        buildings.add(BuildingInfo.builder()
                .buildingId("bld-vesim")
                .name("VES Institute of Management")
                .code("VESIM")
                .latitude(19.0471)
                .longitude(72.8899)
                .address("Hashu Advani Memorial Complex, Chembur, Mumbai")
                .operatingHours("09:00 - 17:00")
                .departments(List.of("Management"))
                .build());
        buildings.add(BuildingInfo.builder()
                .buildingId("bld-h1")
                .name("Hostel Block 1")
                .code("Hostel 1")
                .latitude(19.0460)
                .longitude(72.8890)
                .address("VES Campus Boys' Hostel")
                .operatingHours("24 Hours")
                .departments(List.of())
                .build());
        buildings.add(BuildingInfo.builder()
                .buildingId("bld-h2")
                .name("Hostel Block 2")
                .code("Hostel 2")
                .latitude(19.0458)
                .longitude(72.8892)
                .address("VES Campus Girls' Hostel")
                .operatingHours("24 Hours")
                .departments(List.of())
                .build());
        buildings.add(BuildingInfo.builder()
                .buildingId("bld-vesca")
                .name("VES Cricket Academy")
                .code("VESCA")
                .latitude(19.0475)
                .longitude(72.8890)
                .address("VES Sports Ground")
                .operatingHours("06:00 - 21:00")
                .departments(List.of())
                .build());
        buildings.add(BuildingInfo.builder()
                .buildingId("bld-csh")
                .name("Turing Computer Science Hall")
                .code("CSH")
                .latitude(19.0468)
                .longitude(72.8893)
                .address("VESIT Turing Hall")
                .operatingHours("08:00 - 18:00")
                .departments(List.of("CMPN"))
                .build());

        // Departments
        departments.add(DepartmentInfo.builder()
                .deptId("dept-ecs")
                .name("Electronics and Computer Science")
                .code("ECS")
                .buildingId("bld-vesit")
                .headOfDepartment("Dr. Manish Trivedi")
                .contactEmail("hod.ecs@ves.ac.in")
                .phone("022-61532501")
                .build());
        departments.add(DepartmentInfo.builder()
                .deptId("dept-auro")
                .name("Automation and Robotics")
                .code("AURO")
                .buildingId("bld-vesit")
                .headOfDepartment("Dr. Deepak Mishra")
                .contactEmail("hod.auro@ves.ac.in")
                .phone("022-61532502")
                .build());
        departments.add(DepartmentInfo.builder()
                .deptId("dept-aids")
                .name("Artificial Intelligence and Data Science")
                .code("AIDS")
                .buildingId("bld-vesit")
                .headOfDepartment("Dr. Sanjay Patel")
                .contactEmail("hod.aids@ves.ac.in")
                .phone("022-61532503")
                .build());
        departments.add(DepartmentInfo.builder()
                .deptId("dept-cmpn")
                .name("Computer Engineering")
                .code("CMPN")
                .buildingId("bld-vesit")
                .headOfDepartment("Dr. Asha Bharambe")
                .contactEmail("hod.cmpn@ves.ac.in")
                .phone("022-61532504")
                .build());
        departments.add(DepartmentInfo.builder()
                .deptId("dept-extc")
                .name("Electronics and Telecommunication")
                .code("EXTC")
                .buildingId("bld-vesit")
                .headOfDepartment("Dr. Raj Reddy")
                .contactEmail("hod.extc@ves.ac.in")
                .phone("022-61532505")
                .build());
        departments.add(DepartmentInfo.builder()
                .deptId("dept-it")
                .name("Information Technology")
                .code("IT")
                .buildingId("bld-vesit")
                .headOfDepartment("Dr. Shreya Mukherjee")
                .contactEmail("hod.it@ves.ac.in")
                .phone("022-61532506")
                .build());

        // Faculty
        // ECS
        facultyList.add(FacultyInfo.builder().facultyId("fac-manish").name("Dr. Manish Trivedi").title("Professor & HOD").department("Electronics and Computer Science").email("hod.ecs@ves.ac.in").officeRoom("ECS HOD Office (Ground Floor)").researchAreas(List.of("Digital Electronics", "Embedded Systems")).build());
        facultyList.add(FacultyInfo.builder().facultyId("fac-aanchal").name("Prof. Aanchal Joshi").title("Assistant Professor").department("Electronics and Computer Science").email("aanchal.joshi@ves.ac.in").officeRoom("ECS Staff Room (Ground Floor)").researchAreas(List.of("Microprocessors", "VLSI Design")).build());
        facultyList.add(FacultyInfo.builder().facultyId("fac-harish").name("Prof. Harish Kumar").title("Assistant Professor").department("Electronics and Computer Science").email("harish.kumar@ves.ac.in").officeRoom("ECS Staff Room (Ground Floor)").researchAreas(List.of("IoT", "Computer Architecture")).build());
        facultyList.add(FacultyInfo.builder().facultyId("fac-preeti").name("Prof. Preeti Rao").title("Assistant Professor").department("Electronics and Computer Science").email("preeti.rao@ves.ac.in").officeRoom("ECS Staff Room (Ground Floor)").researchAreas(List.of("Analog Electronics")).build());
        facultyList.add(FacultyInfo.builder().facultyId("fac-sunil").name("Prof. Sunil Pande").title("Assistant Professor").department("Electronics and Computer Science").email("sunil.pande@ves.ac.in").officeRoom("ECS Staff Room (Ground Floor)").researchAreas(List.of("Signals and Systems")).build());
        facultyList.add(FacultyInfo.builder().facultyId("fac-kavita").name("Prof. Kavita Chavan").title("Assistant Professor").department("Electronics and Computer Science").email("kavita.chavan@ves.ac.in").officeRoom("ECS Staff Room (Ground Floor)").researchAreas(List.of("Electronic Devices")).build());
        // AURO
        facultyList.add(FacultyInfo.builder().facultyId("fac-deepak").name("Dr. Deepak Mishra").title("Professor & HOD").department("Automation and Robotics").email("hod.auro@ves.ac.in").officeRoom("AURO HOD Office (1st Floor)").researchAreas(List.of("Robotics", "Automation")).build());
        facultyList.add(FacultyInfo.builder().facultyId("fac-shruti").name("Prof. Shruti Pandey").title("Assistant Professor").department("Automation and Robotics").email("shruti.pandey@ves.ac.in").officeRoom("AURO Staff Room (1st Floor)").researchAreas(List.of("Control Systems", "Industrial Automation")).build());
        facultyList.add(FacultyInfo.builder().facultyId("fac-sameer").name("Prof. Sameer Dubey").title("Assistant Professor").department("Automation and Robotics").email("sameer.dubey@ves.ac.in").officeRoom("AURO Staff Room (1st Floor)").researchAreas(List.of("Embedded Systems", "Computer Vision")).build());
        facultyList.add(FacultyInfo.builder().facultyId("fac-jyoti").name("Prof. Jyoti Tiwari").title("Assistant Professor").department("Automation and Robotics").email("jyoti.tiwari@ves.ac.in").officeRoom("AURO Staff Room (1st Floor)").researchAreas(List.of("Robotic Kinematics")).build());
        facultyList.add(FacultyInfo.builder().facultyId("fac-alok").name("Prof. Alok Bajpai").title("Assistant Professor").department("Automation and Robotics").email("alok.bajpai@ves.ac.in").officeRoom("AURO Staff Room (1st Floor)").researchAreas(List.of("PLC & SCADA")).build());
        facultyList.add(FacultyInfo.builder().facultyId("fac-ritu").name("Prof. Ritu Shukla").title("Assistant Professor").department("Automation and Robotics").email("ritu.shukla@ves.ac.in").officeRoom("AURO Staff Room (1st Floor)").researchAreas(List.of("Sensors and Actuators")).build());
        // AIDS
        facultyList.add(FacultyInfo.builder().facultyId("fac-sanjay").name("Dr. Sanjay Patel").title("Professor & HOD").department("Artificial Intelligence and Data Science").email("hod.aids@ves.ac.in").officeRoom("AIDS HOD Office (2nd Floor)").researchAreas(List.of("Machine Learning", "Probability & Statistics")).build());
        facultyList.add(FacultyInfo.builder().facultyId("fac-aarti").name("Prof. Aarti Gupta").title("Assistant Professor").department("Artificial Intelligence and Data Science").email("aarti.gupta@ves.ac.in").officeRoom("AIDS Staff Room (2nd Floor)").researchAreas(List.of("Deep Learning", "Generative AI")).build());
        facultyList.add(FacultyInfo.builder().facultyId("fac-vikram").name("Prof. Vikram Shah").title("Assistant Professor").department("Artificial Intelligence and Data Science").email("vikram.shah@ves.ac.in").officeRoom("AIDS Staff Room (2nd Floor)").researchAreas(List.of("Data Analytics", "MLOps")).build());
        facultyList.add(FacultyInfo.builder().facultyId("fac-pooja").name("Prof. Pooja Iyer").title("Assistant Professor").department("Artificial Intelligence and Data Science").email("pooja.iyer@ves.ac.in").officeRoom("AIDS Staff Room (2nd Floor)").researchAreas(List.of("Natural Language Processing")).build());
        facultyList.add(FacultyInfo.builder().facultyId("fac-karan").name("Prof. Karan Nair").title("Assistant Professor").department("Artificial Intelligence and Data Science").email("karan.nair@ves.ac.in").officeRoom("AIDS Staff Room (2nd Floor)").researchAreas(List.of("Computer Vision")).build());
        facultyList.add(FacultyInfo.builder().facultyId("fac-neha").name("Prof. Neha Singh").title("Assistant Professor").department("Artificial Intelligence and Data Science").email("neha.singh@ves.ac.in").officeRoom("AIDS Staff Room (2nd Floor)").researchAreas(List.of("Big Data Analytics")).build());
        // CMPN
        facultyList.add(FacultyInfo.builder().facultyId("fac-asha").name("Dr. Asha Bharambe").title("Professor & HOD").department("Computer Engineering").email("hod.cmpn@ves.ac.in").officeRoom("CMPN HOD Office (3rd Floor)").researchAreas(List.of("Data Structures", "Algorithms")).build());
        facultyList.add(FacultyInfo.builder().facultyId("fac-rajesh").name("Prof. Rajesh Kulkarni").title("Assistant Professor").department("Computer Engineering").email("rajesh.kulkarni@ves.ac.in").officeRoom("CMPN Staff Room (3rd Floor)").researchAreas(List.of("Database Management Systems", "Operating Systems")).build());
        facultyList.add(FacultyInfo.builder().facultyId("fac-sneha").name("Prof. Sneha Patil").title("Assistant Professor").department("Computer Engineering").email("sneha.patil@ves.ac.in").officeRoom("CMPN Staff Room (3rd Floor)").researchAreas(List.of("Computer Networks", "Software Engineering")).build());
        facultyList.add(FacultyInfo.builder().facultyId("fac-amit").name("Prof. Amit Verma").title("Assistant Professor").department("Computer Engineering").email("amit.verma@ves.ac.in").officeRoom("CMPN Staff Room (3rd Floor)").researchAreas(List.of("Distributed Systems")).build());
        facultyList.add(FacultyInfo.builder().facultyId("fac-riya").name("Prof. Riya Sharma").title("Assistant Professor").department("Computer Engineering").email("riya.sharma@ves.ac.in").officeRoom("CMPN Staff Room (3rd Floor)").researchAreas(List.of("Object-Oriented Programming")).build());
        facultyList.add(FacultyInfo.builder().facultyId("fac-nilesh").name("Prof. Nilesh Deshmukh").title("Assistant Professor").department("Computer Engineering").email("nilesh.deshmukh@ves.ac.in").officeRoom("CMPN Staff Room (3rd Floor)").researchAreas(List.of("Cloud Computing")).build());
        facultyList.add(FacultyInfo.builder().facultyId("fac-smith").name("Prof. John Smith").title("Associate Professor").department("Computer Engineering").email("john.smith@ves.ac.in").officeRoom("CSH-302").researchAreas(List.of("Computer Networks")).build());
        // EXTC
        facultyList.add(FacultyInfo.builder().facultyId("fac-raj").name("Dr. Raj Reddy").title("Professor & HOD").department("Electronics and Telecommunication").email("hod.extc@ves.ac.in").officeRoom("EXTC HOD Office (4th Floor)").researchAreas(List.of("Signals and Systems", "Digital Communication")).build());
        facultyList.add(FacultyInfo.builder().facultyId("fac-aishwarya").name("Prof. Aishwarya Pillai").title("Assistant Professor").department("Electronics and Telecommunication").email("aishwarya.pillai@ves.ac.in").officeRoom("EXTC Staff Room (4th Floor)").researchAreas(List.of("VLSI Design", "Embedded Systems")).build());
        facultyList.add(FacultyInfo.builder().facultyId("fac-kunal").name("Prof. Kunal Sen").title("Assistant Professor").department("Electronics and Telecommunication").email("kunal.sen@ves.ac.in").officeRoom("EXTC Staff Room (4th Floor)").researchAreas(List.of("Digital Signal Processing")).build());
        facultyList.add(FacultyInfo.builder().facultyId("fac-priya").name("Prof. Priya Roy").title("Assistant Professor").department("Electronics and Telecommunication").email("priya.roy@ves.ac.in").officeRoom("EXTC Staff Room (4th Floor)").researchAreas(List.of("Fiber Optic Communication")).build());
        facultyList.add(FacultyInfo.builder().facultyId("fac-sameerb").name("Prof. Sameer Bose").title("Assistant Professor").department("Electronics and Telecommunication").email("sameer.bose@ves.ac.in").officeRoom("EXTC Staff Room (4th Floor)").researchAreas(List.of("Electromagnetics")).build());
        facultyList.add(FacultyInfo.builder().facultyId("fac-preeti-d").name("Prof. Preeti Dutta").title("Assistant Professor").department("Electronics and Telecommunication").email("preeti.dutta@ves.ac.in").officeRoom("EXTC Staff Room (4th Floor)").researchAreas(List.of("Wireless Communication")).build());
        // IT
        facultyList.add(FacultyInfo.builder().facultyId("fac-shreya").name("Dr. Shreya Mukherjee").title("Professor & HOD").department("Information Technology").email("hod.it@ves.ac.in").officeRoom("IT HOD Office (5th Floor)").researchAreas(List.of("Cloud Computing", "Distributed Systems")).build());
        facultyList.add(FacultyInfo.builder().facultyId("fac-abhishek").name("Prof. Abhishek Chatterjee").title("Assistant Professor").department("Information Technology").email("abhishek.chatterjee@ves.ac.in").officeRoom("IT Staff Room (5th Floor)").researchAreas(List.of("Web Technologies", "Internet of Things")).build());
        facultyList.add(FacultyInfo.builder().facultyId("fac-divya").name("Prof. Divya Das").title("Assistant Professor").department("Information Technology").email("divya.das@ves.ac.in").officeRoom("IT Staff Room (5th Floor)").researchAreas(List.of("Software Testing")).build());
        facultyList.add(FacultyInfo.builder().facultyId("fac-vivek").name("Prof. Vivek Banerjee").title("Assistant Professor").department("Information Technology").email("vivek.banerjee@ves.ac.in").officeRoom("IT Staff Room (5th Floor)").researchAreas(List.of("Network Security")).build());
        facultyList.add(FacultyInfo.builder().facultyId("fac-kriti").name("Prof. Kriti Shukla").title("Assistant Professor").department("Information Technology").email("kriti.shukla@ves.ac.in").officeRoom("IT Staff Room (5th Floor)").researchAreas(List.of("Information Retrieval")).build());
        facultyList.add(FacultyInfo.builder().facultyId("fac-pranav").name("Prof. Pranav Mehta").title("Assistant Professor").department("Information Technology").email("pranav.mehta@ves.ac.in").officeRoom("IT Staff Room (5th Floor)").researchAreas(List.of("Mobile Application Development")).build());

        // Office Hours
        officeHoursList.add(OfficeHoursInfo.builder().id("oh-smith").facultyId("fac-smith").facultyName("Prof. John Smith").dayOfWeek("Tuesday").startTime("10:00").endTime("12:00").location("CSH-302").notes("Walk-in").build());
        officeHoursList.add(OfficeHoursInfo.builder().id("oh-manish").facultyId("fac-manish").facultyName("Dr. Manish Trivedi").dayOfWeek("Monday").startTime("11:00").endTime("13:00").location("ECS HOD Office (Ground Floor)").notes("Prior appointment requested").build());
        officeHoursList.add(OfficeHoursInfo.builder().id("oh-asha").facultyId("fac-asha").facultyName("Dr. Asha Bharambe").dayOfWeek("Wednesday").startTime("14:00").endTime("16:00").location("CMPN HOD Office (3rd Floor)").notes("Walk-in allowed").build());
        officeHoursList.add(OfficeHoursInfo.builder().id("oh-sanjay").facultyId("fac-sanjay").facultyName("Dr. Sanjay Patel").dayOfWeek("Friday").startTime("10:00").endTime("12:00").location("AIDS HOD Office (2nd Floor)").notes("For project guidance").build());

        // Laboratories
        laboratories.add(LaboratoryInfo.builder().labId("lab-ecs").name("ECS Instrumentation Lab").buildingId("bld-vesit").roomNumber("Ground Floor - Lab 02").capacity(30).equipmentList(List.of("Oscilloscopes", "Microcontroller Kits", "Soldering Stations")).build());
        laboratories.add(LaboratoryInfo.builder().labId("lab-auro").name("Robotics and Automation Lab").buildingId("bld-vesit").roomNumber("1st Floor - Lab 102").capacity(25).equipmentList(List.of("Robotic Arm", "PLC Simulator", "Sensors Kit")).build());
        laboratories.add(LaboratoryInfo.builder().labId("lab-aids").name("AIDS Analytics Lab").buildingId("bld-vesit").roomNumber("2nd Floor - Lab 202").capacity(35).equipmentList(List.of("High-End Workstations", "GPU Servers", "Python ML Suite")).build());
        laboratories.add(LaboratoryInfo.builder().labId("lab-cmpn").name("CMPN Programming Lab").buildingId("bld-vesit").roomNumber("3rd Floor - Lab 302").capacity(40).equipmentList(List.of("Linux Workstations", "Database Server", "C/C++ Tools")).build());
        laboratories.add(LaboratoryInfo.builder().labId("lab-extc").name("EXTC Communication Lab").buildingId("bld-vesit").roomNumber("4th Floor - Lab 402").capacity(30).equipmentList(List.of("Spectrum Analyzer", "Antenna Trainer", "Signal Generator")).build());
        laboratories.add(LaboratoryInfo.builder().labId("lab-it").name("IT Cloud and Web Lab").buildingId("bld-vesit").roomNumber("5th Floor - Lab 502").capacity(40).equipmentList(List.of("Docker Containers", "VM Cluster", "NodeJS Env")).build());

        // Classrooms & Facilities
        // Ground Floor
        classrooms.add(ClassroomInfo.builder().classroomId("cls-principal").roomNumber("Principal's Office").buildingId("bld-vesit").capacity(15).features(List.of("Meeting Table", "AC", "Ground Floor - to the right of Lift 1")).build());
        classrooms.add(ClassroomInfo.builder().classroomId("cls-admission").roomNumber("Admission Office").buildingId("bld-vesit").capacity(30).features(List.of("Inquiry Counters", "Ground Floor - between Girls' Common Room and Workshops")).build());
        classrooms.add(ClassroomInfo.builder().classroomId("cls-auditorium").roomNumber("Auditorium").buildingId("bld-vesit").capacity(400).features(List.of("Sound System", "Stage", "AC", "Ground Floor")).build());
        classrooms.add(ClassroomInfo.builder().classroomId("cls-music").roomNumber("Music Room").buildingId("bld-vesit").capacity(20).features(List.of("Instruments", "Ground Floor")).build());
        classrooms.add(ClassroomInfo.builder().classroomId("cls-boys-cr").roomNumber("Boys' Common Room").buildingId("bld-vesit").capacity(60).features(List.of("Seating", "Indoor Games", "Ground Floor")).build());
        classrooms.add(ClassroomInfo.builder().classroomId("cls-girls-cr").roomNumber("Girls' Common Room").buildingId("bld-vesit").capacity(60).features(List.of("Seating", "Ground Floor - beside FE workshops")).build());
        classrooms.add(ClassroomInfo.builder().classroomId("cls-wood-ws").roomNumber("FE Woodwork Workshop").buildingId("bld-vesit").capacity(50).features(List.of("Wood Lathes", "Workbenches", "Ground Floor")).build());
        classrooms.add(ClassroomInfo.builder().classroomId("cls-metal-ws").roomNumber("FE Metalwork Workshop").buildingId("bld-vesit").capacity(50).features(List.of("Welding Kits", "Ground Floor")).build());
        classrooms.add(ClassroomInfo.builder().classroomId("cls-canteen").roomNumber("Canteen").buildingId("bld-vesit").capacity(200).features(List.of("Food Counters", "Ground Floor")).build());

        // Department HOD Offices and Staff Rooms
        // Ground Floor (ECS)
        classrooms.add(ClassroomInfo.builder().classroomId("cls-ecs-hod").roomNumber("ECS HOD Office").buildingId("bld-vesit").capacity(5).features(List.of("Ground Floor", "Manish Trivedi")).build());
        classrooms.add(ClassroomInfo.builder().classroomId("cls-ecs-staff").roomNumber("ECS Staff Room").buildingId("bld-vesit").capacity(20).features(List.of("Ground Floor")).build());
        // 1st Floor (AURO)
        classrooms.add(ClassroomInfo.builder().classroomId("cls-auro-hod").roomNumber("AURO HOD Office").buildingId("bld-vesit").capacity(5).features(List.of("1st Floor", "Deepak Mishra")).build());
        classrooms.add(ClassroomInfo.builder().classroomId("cls-auro-staff").roomNumber("AURO Staff Room").buildingId("bld-vesit").capacity(20).features(List.of("1st Floor")).build());
        // 2nd Floor (AIDS)
        classrooms.add(ClassroomInfo.builder().classroomId("cls-aids-hod").roomNumber("AIDS HOD Office").buildingId("bld-vesit").capacity(5).features(List.of("2nd Floor", "Sanjay Patel")).build());
        classrooms.add(ClassroomInfo.builder().classroomId("cls-aids-staff").roomNumber("AIDS Staff Room").buildingId("bld-vesit").capacity(20).features(List.of("2nd Floor")).build());
        // 3rd Floor (CMPN)
        classrooms.add(ClassroomInfo.builder().classroomId("cls-cmpn-hod").roomNumber("CMPN HOD Office").buildingId("bld-vesit").capacity(5).features(List.of("3rd Floor", "Asha Bharambe")).build());
        classrooms.add(ClassroomInfo.builder().classroomId("cls-cmpn-staff").roomNumber("CMPN Staff Room").buildingId("bld-vesit").capacity(20).features(List.of("3rd Floor")).build());
        // 4th Floor (EXTC)
        classrooms.add(ClassroomInfo.builder().classroomId("cls-extc-hod").roomNumber("EXTC HOD Office").buildingId("bld-vesit").capacity(5).features(List.of("4th Floor", "Raj Reddy")).build());
        classrooms.add(ClassroomInfo.builder().classroomId("cls-extc-staff").roomNumber("EXTC Staff Room").buildingId("bld-vesit").capacity(20).features(List.of("4th Floor")).build());
        // 5th Floor (IT)
        classrooms.add(ClassroomInfo.builder().classroomId("cls-it-hod").roomNumber("IT HOD Office").buildingId("bld-vesit").capacity(5).features(List.of("5th Floor", "Shreya Mukherjee")).build());
        classrooms.add(ClassroomInfo.builder().classroomId("cls-it-staff").roomNumber("IT Staff Room").buildingId("bld-vesit").capacity(20).features(List.of("5th Floor")).build());
        
        // 1st Floor
        classrooms.add(ClassroomInfo.builder().classroomId("cls-library").roomNumber("Library").buildingId("bld-vesit").capacity(150).features(List.of("Study Cubicles", "Reference Section", "1st Floor")).build());
        for (int r = 101; r <= 121; r++) {
            classrooms.add(ClassroomInfo.builder().classroomId("cls-" + r).roomNumber("Classroom " + r).buildingId("bld-vesit").capacity(60).features(List.of("Projector", "Board", "1st Floor")).build());
        }

        // 2nd Floor
        classrooms.add(ClassroomInfo.builder().classroomId("cls-amphi").roomNumber("Amphitheatre").buildingId("bld-vesit").capacity(120).features(List.of("Satellite End", "Stage End", "2nd Floor")).build());
        for (int r = 201; r <= 215; r++) {
            classrooms.add(ClassroomInfo.builder().classroomId("cls-" + r).roomNumber("Room " + r).buildingId("bld-vesit").capacity(50).features(List.of("Projector", "2nd Floor")).build());
        }

        // 3rd Floor (CMPN)
        for (int r = 301; r <= 315; r++) {
            classrooms.add(ClassroomInfo.builder().classroomId("cls-" + r).roomNumber("Room " + r).buildingId("bld-vesit").capacity(60).features(List.of("Projector", "3rd Floor")).build());
        }

        // 4th Floor (EXTC)
        for (int r = 401; r <= 415; r++) {
            classrooms.add(ClassroomInfo.builder().classroomId("cls-" + r).roomNumber("Room " + r).buildingId("bld-vesit").capacity(60).features(List.of("Projector", "4th Floor")).build());
        }

        // 5th Floor (IT)
        for (int r = 501; r <= 520; r++) {
            classrooms.add(ClassroomInfo.builder().classroomId("cls-" + r).roomNumber("Room " + r).buildingId("bld-vesit").capacity(60).features(List.of("Projector", "5th Floor")).build());
        }

        // Lifts & Washrooms
        for (int f = 0; f <= 5; f++) {
            String suffix = f == 0 ? "Ground Floor" : f + " Floor";
            classrooms.add(ClassroomInfo.builder().classroomId("lift-front-" + f).roomNumber("Front Lift Section - Floor " + f).buildingId("bld-vesit").capacity(15).features(List.of("Contains 2 lifts, faces Rear Lift Section across the stairway", suffix)).build());
            classrooms.add(ClassroomInfo.builder().classroomId("lift-rear-" + f).roomNumber("Rear Lift Section - Floor " + f).buildingId("bld-vesit").capacity(15).features(List.of("Contains 2 lifts, faces Front Lift Section across the stairway", suffix)).build());
            classrooms.add(ClassroomInfo.builder().classroomId("wr-male-front-" + f).roomNumber("Male Washroom (Front Lift) - Floor " + f).buildingId("bld-vesit").capacity(5).features(List.of("Clean", suffix, "Near Front Lift")).build());
            classrooms.add(ClassroomInfo.builder().classroomId("wr-female-front-" + f).roomNumber("Female Washroom (Front Lift) - Floor " + f).buildingId("bld-vesit").capacity(5).features(List.of("Clean", suffix, "Near Front Lift")).build());
            classrooms.add(ClassroomInfo.builder().classroomId("wr-male-rear-" + f).roomNumber("Male Washroom (Rear Lift) - Floor " + f).buildingId("bld-vesit").capacity(5).features(List.of("Clean", suffix, "Near Rear Lift")).build());
            classrooms.add(ClassroomInfo.builder().classroomId("wr-female-rear-" + f).roomNumber("Female Washroom (Rear Lift) - Floor " + f).buildingId("bld-vesit").capacity(5).features(List.of("Clean", suffix, "Near Rear Lift")).build());
        }

        // Student Services
        studentServices.add(StudentServiceInfo.builder().serviceId("srv-admin").name("VESIT Admission Office").category("Administration").location("Ground Floor, near Girls' Common Room").contactInfo("admission.vesit@ves.ac.in").operatingHours("09:00 - 17:00").build());
        studentServices.add(StudentServiceInfo.builder().serviceId("srv-library").name("VESIT Central Library").category("Academic").location("1st Floor").contactInfo("library.vesit@ves.ac.in").operatingHours("08:00 - 20:00").build());
        studentServices.add(StudentServiceInfo.builder().serviceId("srv-canteen").name("VESIT Main Canteen").category("Catering").location("Ground Floor, backdoor side").contactInfo("canteen@ves.ac.in").operatingHours("08:00 - 17:30").build());

        // Announcements
        announcements.add(CampusAnnouncementInfo.builder().announcementId("ann-exams").title("Semester Exams Registration Deadlines").content("All students are requested to complete exam forms on the VESIT portal before August 15, 2026.").category("Academic").priority("HIGH").publishedAt(System.currentTimeMillis() - 86400000L).expiresAt(System.currentTimeMillis() + 604800000L).build());
        announcements.add(CampusAnnouncementInfo.builder().announcementId("ann-gdg").title("GDG Meetup: Intro to Cloud").content("Join GDG Club this Thursday at the AIDS Seminar Room (2nd floor) for an interactive cloud computing hands-on workshop.").category("Event").priority("MEDIUM").publishedAt(System.currentTimeMillis() - 43200000L).expiresAt(System.currentTimeMillis() + 172800000L).build());

        // Events
        events.add(CampusEventInfo.builder().eventId("evt-hack").title("VESIT Annual Hackathon 2026").description("24-Hour software development challenge hosted by the CSI Council.").location("CMPN Labs, 3rd Floor").startTime("2026-08-25 09:00").endTime("2026-08-26 12:00").organizer("CSI Council").build());
        events.add(CampusEventInfo.builder().eventId("evt-sports").title("Inter-department Football Tournament").description("Annual sports tournament at the VES Cricket Academy Ground.").location("VES Cricket Academy").startTime("2026-09-02 08:00").endTime("2026-09-05 17:00").organizer("Sports Council").build());

        // Navigation
        navigationRoutes.add(NavigationMetadataInfo.builder().routeId("nav-front-rear").origin("Main Entrance Lift Section").destination("Backdoor Lift Section").distanceMeters(60.0).estimatedWalkMinutes(1).accessible(true).build());
        navigationRoutes.add(NavigationMetadataInfo.builder().routeId("nav-canteen-lib").origin("Canteen (Ground Floor)").destination("Library (1st Floor)").distanceMeters(120.0).estimatedWalkMinutes(2).accessible(true).build());
        navigationRoutes.add(NavigationMetadataInfo.builder().routeId("nav-csh-curie").origin("Turing Computer Science Hall").destination("Curie Science Complex").distanceMeters(100.0).estimatedWalkMinutes(2).accessible(true).build());

        // Emergency Contacts
        emergencyContacts.add(EmergencyContactInfo.builder().contactId("emg-gate").serviceName("VESIT Main Gate Security").phoneNumber("022-61532555").altPhone("022-61532500").location("VESIT Main Gate").available24x7(true).build());
        emergencyContacts.add(EmergencyContactInfo.builder().contactId("emg-medical").serviceName("VESCOP Medical Emergency Room").phoneNumber("022-61532600").altPhone("None").location("VESCOP Building, Ground Floor").available24x7(false).build());
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
