package com.campusguide.common.migration;

import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.entity.Role;
import com.campusguide.platform.user.repository.UserRepository;
import com.campusguide.campus.academic.course.entity.Course;
import com.campusguide.campus.academic.course.repository.CourseRepository;
import com.campusguide.campus.academic.progress.entity.StudentProgress;
import com.campusguide.campus.academic.progress.repository.StudentProgressRepository;
import com.campusguide.campus.academic.roadmap.entity.Roadmap;
import com.campusguide.campus.academic.roadmap.repository.RoadmapRepository;
import com.campusguide.campus.academic.semesterplanner.entity.SemesterPlan;
import com.campusguide.campus.academic.semesterplanner.repository.SemesterPlanRepository;
import com.campusguide.campus.council.entity.Council;
import com.campusguide.campus.council.repository.CouncilRepository;
import com.campusguide.campus.community.entity.Community;
import com.campusguide.campus.community.repository.CommunityRepository;
import com.campusguide.campus.resource.entity.Resource;
import com.campusguide.campus.resource.repository.ResourceRepository;
import com.campusguide.campus.notice.entity.Notice;
import com.campusguide.campus.notice.enums.NoticeCategory;
import com.campusguide.campus.notice.enums.NoticePriority;
import com.campusguide.campus.notice.enums.NoticeVisibility;
import com.campusguide.campus.notice.repository.NoticeRepository;
import com.campusguide.campus.event.entity.Event;
import com.campusguide.campus.event.entity.EventStatus;
import com.campusguide.campus.event.entity.EventType;
import com.campusguide.campus.event.repository.EventRepository;
import com.campusguide.personal.planner.entity.PlannerTask;
import com.campusguide.personal.planner.entity.TaskType;
import com.campusguide.personal.planner.entity.TaskPriority;
import com.campusguide.personal.planner.entity.TaskStatus;
import com.campusguide.personal.planner.repository.PlannerTaskRepository;
import com.campusguide.personal.calendar.entity.CalendarEntry;
import com.campusguide.personal.calendar.entity.CalendarEntryType;
import com.campusguide.personal.calendar.repository.CalendarEntryRepository;
import com.campusguide.personal.achievement.entity.AchievementProgress;
import com.campusguide.personal.achievement.entity.AchievementCategory;
import com.campusguide.personal.achievement.entity.AchievementStatus;
import com.campusguide.personal.achievement.repository.AchievementProgressRepository;
import com.campusguide.personal.notification.entity.Notification;
import com.campusguide.personal.notification.enums.NotificationPriority;
import com.campusguide.personal.notification.enums.NotificationType;
import com.campusguide.personal.notification.repository.NotificationRepository;
import com.campusguide.campus.post.entity.Post;
import com.campusguide.campus.post.repository.PostRepository;
import com.campusguide.campus.comment.entity.Comment;
import com.campusguide.campus.comment.repository.CommentRepository;
import com.campusguide.personal.ai.entity.Conversation;
import com.campusguide.personal.ai.entity.Message;
import com.campusguide.personal.ai.enums.ConversationStatus;
import com.campusguide.personal.ai.enums.ConversationType;
import com.campusguide.personal.ai.enums.MessageRole;
import com.campusguide.personal.ai.repository.ConversationRepository;
import com.campusguide.personal.ai.repository.MessageRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Component
public class V1_1__MVPSeedDataset implements Migration {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentProgressRepository studentProgressRepository;

    @Autowired
    private RoadmapRepository roadmapRepository;

    @Autowired
    private SemesterPlanRepository semesterPlanRepository;

    @Autowired
    private CouncilRepository councilRepository;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private PlannerTaskRepository plannerTaskRepository;

    @Autowired
    private CalendarEntryRepository calendarEntryRepository;

    @Autowired
    private AchievementProgressRepository achievementProgressRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Environment environment;

    @Override
    public String getVersion() {
        return "1.1";
    }

    @Override
    public String getDescription() {
        return "Seeding the complete realistic VESIT MVP dataset for local testing and manual QA.";
    }

    @Override
    public void execute(MongoTemplate mongoTemplate) throws Exception {
        log.info("Checking active profiles for MVP Seeding execution...");
        boolean isDevOrTest = false;
        for (String profile : environment.getActiveProfiles()) {
            if ("dev".equalsIgnoreCase(profile) || "test".equalsIgnoreCase(profile)) {
                isDevOrTest = true;
                break;
            }
        }

        if (!isDevOrTest) {
            log.info("Not in dev or test profile. Skipping MVP dataset seeding.");
            return;
        }

        log.info("Executing MVP dataset migration V1.1...");

        // 1. Password
        String commonPasswordHash = passwordEncoder.encode("Password123");

        // 2. Seed Councils (9 total)
        Map<String, Council> seededCouncils = seedCouncils();

        // 3. Seed Communities (5 total)
        Map<String, Community> seededCommunities = seedCommunities(seededCouncils);

        // 4. Seed Course Catalog (45 total)
        Map<String, Course> seededCourses = seedCourses();

        // 5. Seed Roadmaps (1 per department)
        Map<String, Roadmap> seededRoadmaps = seedRoadmaps();

        // 6. Seed Admin Accounts (12 total)
        List<User> seededAdmins = seedAdmins(commonPasswordHash);

        // 7. Seed Faculty Accounts (36 total: 6 HODs + 30 Faculty)
        Map<String, User> seededFaculty = seedFaculty(commonPasswordHash);

        // 8. Seed Student Accounts (72 total, distributed across batches)
        // Including the Golden QA Student
        Map<String, User> seededStudents = seedStudents(commonPasswordHash, seededRoadmaps, seededCourses);

        // Retrieve Golden QA Student User
        User goldenStudent = seededStudents.get("golden.student@ves.ac.in");

        // 9. Seed Resources (35 total)
        seedResources(goldenStudent.getId(), seededCouncils, seededCommunities);

        // 10. Seed Notices (30 total)
        seedNotices(seededCouncils);

        // 11. Seed Calendar Events (25 total)
        seedEvents(seededCouncils);

        // 12. Seed Planner Tasks & Goals (40 total)
        seedPlannerTasksAndGoals(goldenStudent.getId(), seededStudents);

        // 13. Seed Achievements
        seedAchievements(goldenStudent.getId(), seededStudents);

        // 14. Seed Notifications (50 total)
        seedNotifications(goldenStudent.getId(), seededStudents);

        // 15. Seed Posts & Comments (50+ interactions)
        seedPostsAndComments(goldenStudent.getId(), seededStudents, seededCommunities);

        // 16. Seed Atlas Conversions
        seedAtlasConversations(goldenStudent.getId());

        log.info("V1.1 MVP Seeding execution completed successfully.");
    }

    private Map<String, Council> seedCouncils() {
        log.info("Seeding Councils...");
        Map<String, Council> map = new HashMap<>();

        // Auto-fix existing iSTE entries in DB to ISTE
        for (Council c : councilRepository.findAll()) {
            if (c.getName().contains("iSTE") || c.getDescription().contains("iSTE")) {
                c.setName(c.getName().replace("iSTE", "ISTE"));
                c.setDescription(c.getDescription().replace("iSTE", "ISTE"));
                councilRepository.save(c);
            }
        }
        for (Event e : eventRepository.findAll()) {
            if (e.getTitle().contains("iSTE") || e.getDescription().contains("iSTE")) {
                e.setTitle(e.getTitle().replace("iSTE", "ISTE"));
                e.setDescription(e.getDescription().replace("iSTE", "ISTE"));
                eventRepository.save(e);
            }
        }
        for (Notice n : noticeRepository.findAll()) {
            if (n.getTitle().contains("iSTE") || n.getContent().contains("iSTE")) {
                n.setTitle(n.getTitle().replace("iSTE", "ISTE"));
                n.setContent(n.getContent().replace("iSTE", "ISTE"));
                noticeRepository.save(n);
            }
        }
        for (Resource r : resourceRepository.findAll()) {
            if (r.getTitle().contains("iSTE") || r.getDescription().contains("iSTE")) {
                r.setTitle(r.getTitle().replace("iSTE", "ISTE"));
                r.setDescription(r.getDescription().replace("iSTE", "ISTE"));
                resourceRepository.save(r);
            }
        }

        String[][] councilData = {
                {"VESLANG", "veslang", "VES's Language Council, promoting debate and public speaking.", "veslang@ves.ac.in"},
                {"VESLIT", "veslit", "VES's Literature Council, celebrating writing, poetry, and literature.", "veslit@ves.ac.in"},
                {"SORT", "sort", "Social Outreach and Reflexive Tribulations (SORT) - Social work and donation drives.", "sort@ves.ac.in"},
                {"CC", "cc", "Cultural Council (CC), organizing music, dance, and drama events.", "cc@ves.ac.in"},
                {"Sports", "sports", "Sports Council, managing athletic events and tournaments.", "sports@ves.ac.in"},
                {"IEEE", "ieee", "IEEE Student Branch VESIT, facilitating technical growth.", "ieee@ves.ac.in"},
                {"ISTE", "iste", "Indian Society for Technical Education (ISTE) VESIT Chapter.", "iste@ves.ac.in"},
                {"ISA", "isa", "International Society of Automation (ISA) VESIT Chapter.", "isa@ves.ac.in"},
                {"CSI", "csi", "Computer Society of India (CSI) VESIT Chapter.", "csi@ves.ac.in"}
        };

        for (String[] data : councilData) {
            String name = data[0];
            String slug = data[1];
            String desc = data[2];
            String email = data[3];

            Council council = councilRepository.findBySlug(slug).orElse(null);
            if (council == null) {
                council = Council.builder()
                        .id(UUID.randomUUID())
                        .name(name)
                        .slug(slug)
                        .description(desc)
                        .email(email)
                        .contactNumber("022-61532500")
                        .facultyAdvisor("Prof. Advisor")
                        .isActive(true)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();
                council = councilRepository.save(council);
            } else {
                council.setName(name);
                council.setDescription(desc);
                council = councilRepository.save(council);
            }
            map.put(slug, council);
        }
        return map;
    }

    private Map<String, Community> seedCommunities(Map<String, Council> councils) {
        log.info("Seeding Communities...");
        Map<String, Community> map = new HashMap<>();
        String[][] communityData = {
                {"Google Developer Groups (GDG)", "Google Developer Groups VESIT", "csi"},
                {"AI & ML Club", "Artificial Intelligence & Machine Learning Club", "csi"},
                {"Cybersecurity Club", "Cybersecurity and Ethical Hacking Club", "ieee"},
                {"Web Development Club", "Modern Web Development and Design Club", "csi"},
                {"Photography Club", "Capturing moments and creative expression", "cc"}
        };

        for (String[] data : communityData) {
            String name = data[0];
            String desc = data[1];
            String councilSlug = data[2];
            Council council = councils.get(councilSlug);

            Community comm = communityRepository.findByName(name).orElse(null);
            if (comm == null) {
                comm = Community.builder()
                        .name(name)
                        .description(desc)
                        .bannerUrl("/placeholder-banner.png")
                        .councilId(council != null ? council.getId().toString() : UUID.randomUUID().toString())
                        .memberCount(0)
                        .isActive(true)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();
                comm = communityRepository.save(comm);
            }
            map.put(name, comm);
        }
        return map;
    }

    private Map<String, Course> seedCourses() {
        log.info("Seeding Courses...");
        Map<String, Course> map = new HashMap<>();

        // Helper to get or save course
        List<CourseData> coursesToSeed = getCourseDataCatalog();
        for (CourseData cd : coursesToSeed) {
            Course course = courseRepository.findByCourseCode(cd.code).orElse(null);
            if (course == null) {
                course = Course.builder()
                        .courseCode(cd.code)
                        .courseName(cd.name)
                        .description(cd.desc)
                        .department(cd.dept)
                        .credits(cd.credits)
                        .semester(cd.sem)
                        .elective(cd.elective)
                        .active(true)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();
                course = courseRepository.save(course);
            }
            map.put(cd.code, course);
        }
        // Clear all course prerequisites in database to support prerequisite removal
        for (Course c : courseRepository.findAll()) {
            if (c.getPrerequisiteCourseIds() != null && !c.getPrerequisiteCourseIds().isEmpty()) {
                c.setPrerequisiteCourseIds(new ArrayList<>());
                courseRepository.save(c);
            }
        }

        return map;
    }

    private void linkPrerequisite(Map<String, Course> map, String courseCode, String prereqCode) {
        // No-op to remove prerequisites feature
    }

    private Map<String, Roadmap> seedRoadmaps() {
        log.info("Seeding Roadmaps...");
        Map<String, Roadmap> map = new HashMap<>();
        String[] depts = {"ECS", "AURO", "AIDS", "CMPN", "EXTC", "IT"};
        for (String dept : depts) {
            String title = dept + " Undergraduate Roadmap";
            Roadmap roadmap = roadmapRepository.findByDepartmentIgnoreCaseAndIsDeletedFalseOrderByCreatedAtDesc(dept)
                    .stream().findFirst().orElse(null);
            if (roadmap == null) {
                roadmap = Roadmap.builder()
                        .title(title)
                        .description("Default academic curriculum roadmap for " + dept + " department.")
                        .department(dept)
                        .degreeProgram("B.Tech " + dept)
                        .totalCredits(160)
                        .expectedGraduationYear(2028)
                        .createdBy("SYSTEM")
                        .isDeleted(false)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();
                roadmap = roadmapRepository.save(roadmap);
            } else {
                roadmap.setDegreeProgram("B.Tech " + dept);
                roadmap = roadmapRepository.save(roadmap);
            }
            map.put(dept, roadmap);
        }
        return map;
    }

    private List<User> seedAdmins(String passwordHash) {
        log.info("Seeding Admins...");
        List<User> list = new ArrayList<>();
        String[][] adminData = {
                {"super.admin@ves.ac.in", "super.admin", "SUPER_ADMIN"},
                {"admin.staff@ves.ac.in", "admin.staff", "SUPER_ADMIN"},
                {"council.admin@ves.ac.in", "council.admin", "COUNCIL_ADMIN"},
                {"community.admin@ves.ac.in", "community.admin", "COUNCIL_ADMIN"},
                {"content.admin@ves.ac.in", "content.admin", "COUNCIL_ADMIN"},
                {"veslang.admin@ves.ac.in", "veslang.admin", "COUNCIL_ADMIN"},
                {"veslit.admin@ves.ac.in", "veslit.admin", "COUNCIL_ADMIN"},
                {"sort.admin@ves.ac.in", "sort.admin", "COUNCIL_ADMIN"},
                {"cc.admin@ves.ac.in", "cc.admin", "COUNCIL_ADMIN"},
                {"sports.admin@ves.ac.in", "sports.admin", "COUNCIL_ADMIN"},
                {"ieee.admin@ves.ac.in", "ieee.admin", "COUNCIL_ADMIN"},
                {"csi.admin@ves.ac.in", "csi.admin", "COUNCIL_ADMIN"}
        };

        for (String[] data : adminData) {
            String email = data[0];
            String username = data[1];
            Role role = Role.fromString(data[2]);

            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                user = User.builder()
                        .email(email)
                        .username(username)
                        .passwordHash(passwordHash)
                        .role(role)
                        .enabled(true)
                        .emailVerified(true)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();
                user = userRepository.save(user);
            }
            list.add(user);
        }
        return list;
    }

    private Map<String, User> seedFaculty(String passwordHash) {
        log.info("Seeding Faculty...");
        Map<String, User> map = new HashMap<>();
        String[][] facultyData = {
                // ECS
                {"hod.ecs@ves.ac.in", "hod.ecs", "Dr. Manish Trivedi"},
                {"aanchal.joshi@ves.ac.in", "aanchal.joshi", "Prof. Aanchal Joshi"},
                {"harish.kumar@ves.ac.in", "harish.kumar", "Prof. Harish Kumar"},
                {"preeti.rao@ves.ac.in", "preeti.rao", "Prof. Preeti Rao"},
                {"sunil.pande@ves.ac.in", "sunil.pande", "Prof. Sunil Pande"},
                {"kavita.chavan@ves.ac.in", "kavita.chavan", "Prof. Kavita Chavan"},
                // AURO
                {"hod.auro@ves.ac.in", "hod.auro", "Dr. Deepak Mishra"},
                {"shruti.pandey@ves.ac.in", "shruti.pandey", "Prof. Shruti Pandey"},
                {"sameer.dubey@ves.ac.in", "sameer.dubey", "Prof. Sameer Dubey"},
                {"jyoti.tiwari@ves.ac.in", "jyoti.tiwari", "Prof. Jyoti Tiwari"},
                {"alok.bajpai@ves.ac.in", "alok.bajpai", "Prof. Alok Bajpai"},
                {"ritu.shukla@ves.ac.in", "ritu.shukla", "Prof. Ritu Shukla"},
                // AIDS
                {"hod.aids@ves.ac.in", "hod.aids", "Dr. Sanjay Patel"},
                {"aarti.gupta@ves.ac.in", "aarti.gupta", "Prof. Aarti Gupta"},
                {"vikram.shah@ves.ac.in", "vikram.shah", "Prof. Vikram Shah"},
                {"pooja.iyer@ves.ac.in", "pooja.iyer", "Prof. Pooja Iyer"},
                {"karan.nair@ves.ac.in", "karan.nair", "Prof. Karan Nair"},
                {"neha.singh@ves.ac.in", "neha.singh", "Prof. Neha Singh"},
                // CMPN
                {"hod.cmpn@ves.ac.in", "hod.cmpn", "Dr. Asha Bharambe"},
                {"rajesh.kulkarni@ves.ac.in", "rajesh.kulkarni", "Prof. Rajesh Kulkarni"},
                {"sneha.patil@ves.ac.in", "sneha.patil", "Prof. Sneha Patil"},
                {"amit.verma@ves.ac.in", "amit.verma", "Prof. Amit Verma"},
                {"riya.sharma@ves.ac.in", "riya.sharma", "Prof. Riya Sharma"},
                {"nilesh.deshmukh@ves.ac.in", "nilesh.deshmukh", "Prof. Nilesh Deshmukh"},
                // EXTC
                {"hod.extc@ves.ac.in", "hod.extc", "Dr. Raj Reddy"},
                {"aishwarya.pillai@ves.ac.in", "aishwarya.pillai", "Prof. Aishwarya Pillai"},
                {"kunal.sen@ves.ac.in", "kunal.sen", "Prof. Kunal Sen"},
                {"priya.roy@ves.ac.in", "priya.roy", "Prof. Priya Roy"},
                {"sameer.bose@ves.ac.in", "sameer.bose", "Prof. Sameer Bose"},
                {"preeti.dutta@ves.ac.in", "preeti.dutta", "Prof. Preeti Dutta"},
                // IT
                {"hod.it@ves.ac.in", "hod.it", "Dr. Shreya Mukherjee"},
                {"abhishek.chatterjee@ves.ac.in", "abhishek.chatterjee", "Prof. Abhishek Chatterjee"},
                {"divya.das@ves.ac.in", "divya.das", "Prof. Divya Das"},
                {"vivek.banerjee@ves.ac.in", "vivek.banerjee", "Prof. Vivek Banerjee"},
                {"kriti.shukla@ves.ac.in", "kriti.shukla", "Prof. Kriti Shukla"},
                {"pranav.mehta@ves.ac.in", "pranav.mehta", "Prof. Pranav Mehta"}
        };

        for (String[] data : facultyData) {
            String email = data[0];
            String username = data[1];

            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                user = User.builder()
                        .email(email)
                        .username(username)
                        .passwordHash(passwordHash)
                        .role(Role.FACULTY)
                        .enabled(true)
                        .emailVerified(true)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();
                user = userRepository.save(user);
            }
            map.put(email, user);
        }
        return map;
    }

    private Map<String, User> seedStudents(String passwordHash, Map<String, Roadmap> roadmaps, Map<String, Course> courses) {
        log.info("Seeding Students...");
        Map<String, User> map = new HashMap<>();

        String[] depts = {"ECS", "AIDS", "CMPN", "AURO", "EXTC", "IT"};
        String[][] batches = {
                {"D1EC", "D1ADA", "D2A", "D3", "D4A", "D5A"},
                {"D6EC", "D6ADA", "D7A", "D8", "D9A", "D10A"},
                {"D11EC", "D11ADA", "D12A", "D13", "D14A", "D15A"},
                {"D16EC", "D16ADA", "D17A", "D18", "D19A", "D20A"}
        };

        String[] years = {"FE", "SE", "TE", "BE"};

        String[] firstNames = {
                "Aarav", "Aditya", "Amit", "Ananya", "Arjun", "Dev", "Diya", "Gaurav", "Isha", "Kabir", 
                "Meera", "Neha", "Pranav", "Rohan", "Sanjana", "Tanvi", "Varun", "Yash", "Karan", "Nisha", 
                "Riya", "Siddharth", "Rahul", "Priya", "Vivek", "Shruti", "Manish", "Divya", "Deepak", "Aanchal",
                "Akash", "Kriti", "Harsh", "Pooja", "Vikram", "Sneha", "Alok", "Shreya", "Kunal", "Payal"
        };
        String[] lastNames = {
                "Sharma", "Verma", "Gupta", "Mehta", "Joshi", "Patel", "Shah", "Iyer", "Nair", "Singh",
                "Kumar", "Trivedi", "Deshmukh", "Kulkarni", "More", "Patil", "Pande", "Chavan", "Rao", "Reddy"
        };

        int studentIdx = 0;

        for (int yIndex = 0; yIndex < years.length; yIndex++) {
            String year = years[yIndex];
            int currentSem = (yIndex * 2) + 1;

            for (int dIndex = 0; dIndex < depts.length; dIndex++) {
                String dept = depts[dIndex];

                for (int sNum = 1; sNum <= 3; sNum++) {
                    String firstName = firstNames[(studentIdx) % firstNames.length];
                    String lastName = lastNames[(studentIdx * 3) % lastNames.length];
                    String username = firstName.toLowerCase() + "." + lastName.toLowerCase() + studentIdx;
                    String email = username + "@ves.ac.in";

                    User user = userRepository.findByEmail(email).orElse(null);
                    if (user == null) {
                        user = User.builder()
                                .email(email)
                                .username(username)
                                .passwordHash(passwordHash)
                                .role(Role.STUDENT)
                                .enabled(true)
                                .emailVerified(true)
                                .createdAt(Instant.now())
                                .updatedAt(Instant.now())
                                .build();
                        user = userRepository.save(user);
                    }
                    map.put(email, user);

                    Roadmap roadmap = roadmaps.get(dept);
                    if (roadmap != null) {
                        StudentProgress sp = studentProgressRepository.findByStudentId(user.getId()).orElse(null);
                        if (sp == null) {
                            List<String> completed = new ArrayList<>();
                            if (currentSem > 1) {
                                addCompletedCourseCode(completed, courses, "EM1");
                                addCompletedCourseCode(completed, courses, "EP");
                                addCompletedCourseCode(completed, courses, "EC");
                                addCompletedCourseCode(completed, courses, "EG");
                                addCompletedCourseCode(completed, courses, "PF");
                            }
                            if (currentSem > 3) {
                                addCompletedCourseCode(completed, courses, "EM2");
                                addCompletedCourseCode(completed, courses, "BEE");
                                addCompletedCourseCode(completed, courses, "BE");
                                addCompletedCourseCode(completed, courses, "EMech");
                            }

                            sp = StudentProgress.builder()
                                    .studentId(user.getId())
                                    .roadmapId(roadmap.getId())
                                    .completedCourseIds(completed)
                                    .currentSemester(currentSem)
                                    .totalCreditsEarned(completed.size() * 4)
                                    .currentGpa(8.2 + (studentIdx % 15) * 0.1)
                                    .graduationEligible(false)
                                    .createdAt(Instant.now())
                                    .updatedAt(Instant.now())
                                    .build();
                            studentProgressRepository.save(sp);
                        }
                    }
                    studentIdx++;
                }
            }
        }

        // Golden QA Student
        String goldenEmail = "golden.student@ves.ac.in";
        User golden = userRepository.findByEmail(goldenEmail).orElse(null);
        if (golden == null) {
            golden = User.builder()
                    .email(goldenEmail)
                    .username("golden.student")
                    .passwordHash(passwordHash)
                    .role(Role.STUDENT)
                    .enabled(true)
                    .emailVerified(true)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            golden = userRepository.save(golden);
        }
        map.put(goldenEmail, golden);

        Roadmap cmpnRoadmap = roadmaps.get("CMPN");
        if (cmpnRoadmap != null) {
            StudentProgress sp = studentProgressRepository.findByStudentId(golden.getId()).orElse(null);
            List<String> completed = new ArrayList<>();
            addCompletedCourseCode(completed, courses, "EM1");
            addCompletedCourseCode(completed, courses, "EM2");
            addCompletedCourseCode(completed, courses, "EP");
            addCompletedCourseCode(completed, courses, "EC");
            addCompletedCourseCode(completed, courses, "EG");
            addCompletedCourseCode(completed, courses, "PF");
            addCompletedCourseCode(completed, courses, "BEE");
            addCompletedCourseCode(completed, courses, "BE");
            addCompletedCourseCode(completed, courses, "EMech");
            addCompletedCourseCode(completed, courses, "DS");
            addCompletedCourseCode(completed, courses, "OOP");
            addCompletedCourseCode(completed, courses, "DBMS");

            if (sp == null) {
                sp = StudentProgress.builder()
                        .studentId(golden.getId())
                        .roadmapId(cmpnRoadmap.getId())
                        .completedCourseIds(completed)
                        .currentSemester(5)
                        .totalCreditsEarned(completed.size() * 4)
                        .currentGpa(9.20)
                        .graduationEligible(false)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();
            } else {
                sp.setCompletedCourseIds(completed);
                sp.setCurrentSemester(5);
                sp.setCurrentGpa(9.20);
                sp.setTotalCreditsEarned(completed.size() * 4);
            }
            studentProgressRepository.save(sp);

            SemesterPlan spPlan = semesterPlanRepository.findByStudentIdAndSemesterNumber(golden.getId(), 5).orElse(null);
            List<String> planned = new ArrayList<>();
            addCompletedCourseCode(planned, courses, "OS");
            addCompletedCourseCode(planned, courses, "CN");
            addCompletedCourseCode(planned, courses, "SE");
            addCompletedCourseCode(planned, courses, "WT");
            addCompletedCourseCode(planned, courses, "DistS");
            addCompletedCourseCode(planned, courses, "AI");

            if (spPlan == null) {
                spPlan = SemesterPlan.builder()
                        .studentId(golden.getId())
                        .roadmapId(cmpnRoadmap.getId())
                        .semesterNumber(5)
                        .plannedCourseIds(planned)
                        .totalPlannedCredits(24)
                        .finalized(true)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();
            } else {
                spPlan.setPlannedCourseIds(planned);
            }
            semesterPlanRepository.save(spPlan);
        }

        return map;
    }

    private void addCompletedCourseCode(List<String> list, Map<String, Course> courses, String code) {
        Course course = courses.get(code);
        if (course != null) {
            list.add(course.getId());
        }
    }

    private void seedResources(String goldenStudentId, Map<String, Council> councils, Map<String, Community> communities) {
        log.info("Seeding Resources...");
        long count = resourceRepository.countByIsDeletedFalse();
        if (count > 30) {
            log.info("Resources already seeded. Skipping.");
            return;
        }

        String csiId = councils.get("csi").getId().toString();
        String ieeeId = councils.get("ieee").getId().toString();
        String ccId = councils.get("cc").getId().toString();
        String sportsId = councils.get("sports").getId().toString();
        String sortId = councils.get("sort").getId().toString();
        String isteId = councils.get("iste").getId().toString();
        String isaId = councils.get("isa").getId().toString();

        String gdgCommId = communities.get("Google Developer Groups (GDG)").getId();
        String aiCommId = communities.get("AI & ML Club").getId();
        String cyberCommId = communities.get("Cybersecurity Club").getId();
        String webCommId = communities.get("Web Development Club").getId();
        String photoCommId = communities.get("Photography Club").getId();

        String[][] resData = {
                // Academic
                {"VESIT Semester Academic Calendar", "Official academic schedule for the term.", "academic-calendar.pdf", "PDF", "102400", "http://vesit.edu/calendar"},
                {"Database Management Systems Lecture Notes", "Study notes covering DBMS Relational Algebra, SQL.", "dbms-notes.pdf", "PDF", "4096000", "http://vesit.edu/dbms"},
                {"Digital Communication Laboratory Manual", "Lab experiments for EXTC students.", "digital-comm-lab.pdf", "PDF", "800000", "http://vesit.edu/lab-manual"},
                {"Data Structures Lab Manual", "Standard data structure implementations in C/C++.", "ds-lab-manual.pdf", "PDF", "1500000", "http://vesit.edu/ds-manual"},
                {"Operating Systems Lecture Slides", "Slides covering process scheduling and memory management.", "os-slides.pdf", "PDF", "2500000", "http://vesit.edu/os-slides"},
                {"Computer Networks Lab Manual", "Socket programming experiments and packet analysis.", "cn-lab-manual.pdf", "PDF", "1800000", "http://vesit.edu/cn-manual"},
                {"Digital Electronics Design Laboratory Manual", "Logic gates, flip-flops, and counter design lab manual.", "de-lab-manual.pdf", "PDF", "1100000", "http://vesit.edu/de-manual"},
                {"Embedded Systems RTOS Study Material", "Real-Time Operating Systems concepts and scheduling algorithms.", "rtos-study.pdf", "PDF", "1900000", "http://vesit.edu/rtos-study"},
                {"VLSI Design Lab Reference", "VHDL and Verilog code templates for combinational circuit design.", "vlsi-reference.pdf", "PDF", "1600000", "http://vesit.edu/vlsi-ref"},
                {"Signals & Systems Transform Formula Sheet", "Fourier, Laplace, and Z-transform formula reference sheet.", "ss-formula-sheet.pdf", "PDF", "500000", "http://vesit.edu/ss-sheet"},
                
                // Technical
                {"Python Programming Language Reference Guide", "Hands-on reference cheat sheet for Python.", "python-guide.pdf", "PDF", "512000", "http://vesit.edu/python"},
                {"Modern ReactJS Handbook", "Tutorials on Hooks, Context API, and state management.", "react-handbook.pdf", "PDF", "1024000", "http://vesit.edu/react"},
                {"Git and GitHub Version Control basics", "Walkthrough of standard git workflows.", "git-basics.pdf", "PDF", "128000", "http://vesit.edu/git"},
                {"Machine Learning Foundations", "AI & ML Club Study material.", "ml-foundations.pdf", "PDF", "3128000", "http://vesit.edu/ml"},
                {"Automation and PLC Programming Guide", "Practical training reference for robotics.", "plc-guide.pdf", "PDF", "1500000", "http://vesit.edu/plc-guide"},
                {"Cybersecurity Club Ethical Hacking Guide", "Basics of network scanning, Wireshark, and pentesting.", "hacking-basics.pdf", "PDF", "3200000", "http://vesit.edu/cyber-hacking"},
                {"Deep Learning CNN Laboratory Manual", "Step-by-step image classification lab instructions in PyTorch.", "dl-lab-manual.pdf", "PDF", "2900000", "http://vesit.edu/dl-manual"},
                {"Natural Language Processing Guide", "Reference material on tokenization, TF-IDF, and word embeddings.", "nlp-guide.pdf", "PDF", "2100000", "http://vesit.edu/nlp-guide"},
                {"Generative AI Foundations", "Guide to LLMs, prompt engineering, and RAG systems.", "genai-foundations.pdf", "PDF", "1400000", "http://vesit.edu/genai-guide"},
                {"Internet of Things Project Manual", "Interfacing sensors, ESP8266, and MQTT cloud sync experiments.", "iot-manual.pdf", "PDF", "2400000", "http://vesit.edu/iot-manual"},
                
                // Organization
                {"Turing Computer Science Hall Campus Map", "Detailed indoor navigation and floor map of VESIT CSH.", "campus-map.pdf", "PDF", "2048000", "http://vesit.edu/map"},
                {"Introduction to Cloud Architecture", "CSI Cloud Workshop PPT slides.", "cloud-slides.pptx", "PPTX", "2048000", "http://vesit.edu/cloud"},
                {"VESIT IEEE Technical Journal", "Compilation of student research papers and project abstracts.", "ieee-journal.pdf", "PDF", "4500000", "http://vesit.edu/ieee-journal"},
                {"GDG Flutter Application Development Slides", "Introduction to cross-platform mobile apps with Flutter.", "flutter-slides.pptx", "PPTX", "3500000", "http://vesit.edu/gdg-flutter"},
                {"CSI MongoDB Database Guide", "Cheat sheet for MongoDB queries and aggregation pipelines.", "mongodb-guide.pdf", "PDF", "600000", "http://vesit.edu/csi-mongodb"},
                {"VESIT CC Utsav Cultural Brochure", "List of events, schedules, rules, and guidelines for Utsav 2026.", "utsav-brochure.pdf", "PDF", "1200000", "http://vesit.edu/utsav-brochure"},
                {"Sports Council Tournament Rulebook", "Guidelines, teams, schedules, and code of conduct for tournaments.", "sports-rulebook.pdf", "PDF", "900000", "http://vesit.edu/sports-rulebook"},
                {"SORT Blood Donation Drive Pamphlet", "Pre-donation checklists and facts about blood donation.", "sort-blood-pamphlet.pdf", "PDF", "300000", "http://vesit.edu/sort-blood"},
                {"ISTE Technical Workshop Notes", "Basic web technologies, HTML, CSS, and JS reference guide.", "iste-web-notes.pdf", "PDF", "1000000", "http://vesit.edu/iste-notes"},
                {"ISA PLC & Industrial Automation PPT", "Slides from PLC and automation industrial seminar.", "isa-plc-slides.pptx", "PPTX", "2200000", "http://vesit.edu/isa-plc"},
                {"VESLANG Public Speaking Guide", "Tips and tricks for debating, MUN, and elocution.", "veslang-guide.pdf", "PDF", "750000", "http://vesit.edu/veslang-guide"},
                {"VESLIT Writing Workshop Anthology", "Compilation of student poems, stories, and articles.", "veslit-anthology.pdf", "PDF", "1500000", "http://vesit.edu/veslit-anthology"},
                
                // Campus
                {"VESIT Student Handbook 2026", "Campus guidelines, regulations, and grading policy.", "student-handbook.pdf", "PDF", "3072000", "http://vesit.edu/handbook"},
                {"VESIT Examination Guidelines", "Official rules for exams, copying policy, and marks distribution.", "exam-guidelines.pdf", "PDF", "409600", "http://vesit.edu/exams"},
                {"VESIT Central Library Information Guide", "Borrowing rules, online access instructions, and catalog list.", "library-guide.pdf", "PDF", "819200", "http://vesit.edu/library"}
        };

        for (String[] data : resData) {
            String title = data[0];
            String desc = data[1];
            String fileName = data[2];
            String fileType = data[3];
            Long fileSize = Long.parseLong(data[4]);
            String url = data[5];

            Resource resource = Resource.builder()
                    .title(title)
                    .description(desc)
                    .uploaderId(goldenStudentId)
                    .fileName(fileName)
                    .originalFileName(fileName)
                    .fileType(fileType)
                    .fileSize(fileSize)
                    .downloadUrl(url)
                    .tags(List.of("Study", "Reference", "MVP"))
                    .isDeleted(false)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            if (title.contains("CSI")) {
                resource.setCouncilId(csiId);
            } else if (title.contains("GDG")) {
                resource.setCommunityId(gdgCommId);
            } else if (title.contains("Machine Learning") || title.contains("AI & ML") || title.contains("Deep Learning") || title.contains("Generative AI")) {
                resource.setCommunityId(aiCommId);
            } else if (title.contains("IEEE")) {
                resource.setCouncilId(ieeeId);
            } else if (title.contains("CC") || title.contains("Utsav")) {
                resource.setCouncilId(ccId);
            } else if (title.contains("Sports")) {
                resource.setCouncilId(sportsId);
            } else if (title.contains("SORT")) {
                resource.setCouncilId(sortId);
            } else if (title.contains("ISTE")) {
                resource.setCouncilId(isteId);
            } else if (title.contains("ISA")) {
                resource.setCouncilId(isaId);
            } else if (title.contains("Cybersecurity")) {
                resource.setCommunityId(cyberCommId);
            } else if (title.contains("React")) {
                resource.setCommunityId(webCommId);
            } else if (title.contains("Photography")) {
                resource.setCommunityId(photoCommId);
            }

            resourceRepository.save(resource);
        }
    }

    private void seedNotices(Map<String, Council> councils) {
        log.info("Seeding Notices...");
        long count = noticeRepository.count();
        if (count > 25) {
            log.info("Notices already seeded. Skipping.");
            return;
        }

        UUID csiId = councils.get("csi").getId();
        UUID ieeeId = councils.get("ieee").getId();
        UUID ccId = councils.get("cc").getId();
        UUID sportsId = councils.get("sports").getId();
        UUID sortId = councils.get("sort").getId();
        UUID isteId = councils.get("iste").getId();
        UUID isaId = councils.get("isa").getId();
        UUID veslangId = councils.get("veslang").getId();
        UUID veslitId = councils.get("veslit").getId();

        LocalDateTime now = LocalDateTime.now();

        Object[][] noticesData = {
                // Exam & Academic
                {"Midterm Exam Timetable Release", "timetable-midterm", "The midterm examination timetable for FE to BE has been released and is active on the notice board. Exams start September 10.", NoticeCategory.EXAM, NoticePriority.HIGH, NoticeVisibility.STUDENTS, null, now.minusDays(2), now.plusDays(10), true, true},
                {"Placement Prep Session: TCS/Wipro", "placement-tcs-wipro", "Special orientation session by placement committee for final year CMPN/IT students in CSH-101.", NoticeCategory.PLACEMENT, NoticePriority.URGENT, NoticeVisibility.STUDENTS, null, now, now.plusDays(1), true, true},
                {"Semester Reopening and Fee Payment guidelines", "semester-reopening-fees", "Instructions for online fee payment and registration for the upcoming semester starting next month.", NoticeCategory.ACADEMIC, NoticePriority.MEDIUM, NoticeVisibility.STUDENTS, null, now.minusDays(5), now.plusDays(20), false, true},
                {"Scholarship: MahaDBT registration deadline extension", "mahadbt-scholarship-extended", "The deadline to apply for MahaDBT scholarships has been extended to November 30. Check documentation guidelines.", NoticeCategory.SCHOLARSHIP, NoticePriority.MEDIUM, NoticeVisibility.PUBLIC, null, now.minusDays(1), now.plusDays(45), false, true},
                {"Project Review: Final Year projects submission deadline", "final-project-submission", "All BE project groups must submit their progress reports and software architectural designs to their respective guides.", NoticeCategory.ACADEMIC, NoticePriority.HIGH, NoticeVisibility.STUDENTS, null, now.minusDays(1), now.plusDays(4), false, true},
                {"Examination Office: Photocopy and revaluation form deadlines", "photocopy-reval-forms", "Students who wish to apply for photocopy or revaluation of end semester papers can submit forms online.", NoticeCategory.EXAM, NoticePriority.HIGH, NoticeVisibility.STUDENTS, null, now, now.plusDays(5), false, true},
                
                // Events
                {"GDG Cloud Inception Workshop", "gdg-cloud-workshop", "CSI in collaboration with GDG brings a Cloud Inception Workshop on Thursday in the AIDS Seminar Room.", NoticeCategory.EVENT, NoticePriority.MEDIUM, NoticeVisibility.PUBLIC, csiId, now.minusDays(1), now.plusDays(5), false, true},
                {"Sports Tournament Registrations", "sports-tournament-2026", "Registrations for the Annual Football and Cricket Tournament are now open. Visit sports portal to sign up.", NoticeCategory.EVENT, NoticePriority.MEDIUM, NoticeVisibility.PUBLIC, sportsId, now, now.plusDays(7), false, true},
                {"Blood Donation Drive - SORT", "blood-donation-sort", "SORT is organizing a Blood Donation Camp at the Ground Floor Lobby. Show up and save lives.", NoticeCategory.GENERAL, NoticePriority.HIGH, NoticeVisibility.PUBLIC, sortId, now.minusDays(3), now.plusDays(1), false, true},
                {"VESIT Literature Debate Competition", "veslit-debate-comp", "Join the VESLIT Debate challenge this Saturday in the 1st Floor Auditorium. Exciting prizes!", NoticeCategory.EVENT, NoticePriority.LOW, NoticeVisibility.STUDENTS, veslitId, now.minusDays(5), now.minusDays(1), false, true},
                {"CSI Membership Registrations Open", "csi-membership-registrations", "Register for the Computer Society of India (CSI) VESIT Chapter. Perks include workshops and coding tests.", NoticeCategory.EVENT, NoticePriority.LOW, NoticeVisibility.PUBLIC, csiId, now.minusDays(8), now.plusDays(15), false, true},
                {"ISTE Tech Week Inauguration notice", "iste-tech-week-2026", "Welcome to ISTE Tech Week. A series of hands-on electronics and coding challenges starting next Monday.", NoticeCategory.EVENT, NoticePriority.MEDIUM, NoticeVisibility.PUBLIC, isteId, now.minusDays(2), now.plusDays(6), false, true},
                {"ISA Robotics Exhibition call", "isa-robotics-exhibition", "Call for projects and models for the annual ISA Robotics Exhibition. Showcase your autonomous designs.", NoticeCategory.EVENT, NoticePriority.HIGH, NoticeVisibility.PUBLIC, isaId, now, now.plusDays(12), false, true},
                {"Cultural Council: Auditions for Music and Dance teams", "cc-auditions-2026", "Auditions for music, dance, and drama teams are scheduled at the Music Room and Amphitheatre this week.", NoticeCategory.EVENT, NoticePriority.LOW, NoticeVisibility.STUDENTS, ccId, now.minusDays(2), now.plusDays(3), false, true},
                {"Cyber Club: Capture The Flag (CTF) hacker tournament notice", "cyber-ctf-tournament", "Participate in the IEEE Cybersecurity Club's 12-hour CTF. Teams of up to 3 are welcome.", NoticeCategory.EVENT, NoticePriority.HIGH, NoticeVisibility.PUBLIC, ieeeId, now, now.plusDays(8), false, true},
                {"Deep Learning Seminar: Dr. Sanjay Patel guest lecture", "dl-seminar-sanjay-patel", "Dr. Sanjay Patel will speak on Generative AI and deep learning applications at the Auditorium.", NoticeCategory.EVENT, NoticePriority.MEDIUM, NoticeVisibility.PUBLIC, null, now.minusDays(1), now.plusDays(3), false, true},
                {"VESLANG: Model United Nations (MUN) registrations notice", "veslang-mun-registrations", "Join the VESLANG MUN. Represent countries and debate global challenges in the 1st Floor Seminar room.", NoticeCategory.EVENT, NoticePriority.LOW, NoticeVisibility.STUDENTS, veslangId, now.minusDays(4), now.plusDays(10), false, true},
                {"VESLIT: Creative writing submissions open for annual magazine", "veslit-creative-writing", "Submit your articles, poems, and short stories for the annual VESIT magazine.", NoticeCategory.EVENT, NoticePriority.LOW, NoticeVisibility.STUDENTS, veslitId, now.minusDays(10), now.plusDays(30), false, true},
                
                // Administrative / General
                {"Maintenance: Lifts shutdown scheduled", "lift-maintenance", "Lifts near the rear backdoor section will be shut down for maintenance on Sunday between 9 AM and 1 PM.", NoticeCategory.ADMINISTRATIVE, NoticePriority.HIGH, NoticeVisibility.INTERNAL, null, now, now.plusDays(2), false, true},
                {"Holiday Announcement: Independence Day", "holiday-independence", "The institute will remain closed on 15th August on account of Independence Day celebrations.", NoticeCategory.GENERAL, NoticePriority.LOW, NoticeVisibility.PUBLIC, null, now.minusDays(10), now.plusDays(6), false, true},
                {"Library: Extended reading room hours during exams", "library-extended-hours", "Central library reading room will remain open till 9:00 PM during exam weeks.", NoticeCategory.ADMINISTRATIVE, NoticePriority.LOW, NoticeVisibility.STUDENTS, null, now.minusDays(1), now.plusDays(25), false, true},
                {"Hostel Block 1: Security rules and entry timing notice", "hostel-block1-security", "Strict compliance of 10:00 PM entry deadline for hostel residents. Violations will be penalised.", NoticeCategory.ADMINISTRATIVE, NoticePriority.MEDIUM, NoticeVisibility.INTERNAL, null, now.minusDays(3), now.plusDays(60), false, true},
                {"Hostel Block 2: Water supply maintenance shutdown", "hostel-water-shutdown", "Temporary water supply shutdown in hostel block 2 on Wednesday morning for tank cleaning.", NoticeCategory.ADMINISTRATIVE, NoticePriority.MEDIUM, NoticeVisibility.INTERNAL, null, now, now.plusDays(2), false, true},
                {"Admission Office: First Year Engineering Orientation program", "fe-orientation-schedule", "Orientation schedule for newly admitted First Year (FE) students in the Auditorium.", NoticeCategory.ADMINISTRATIVE, NoticePriority.HIGH, NoticeVisibility.PUBLIC, null, now.minusDays(4), now.plusDays(2), false, true},
                {"MCA Entrance Prep Session: Free workshop", "mca-entrance-prep", "MCA department is organizing a free guidelines session for MCA CET aspirants in Classroom 105.", NoticeCategory.GENERAL, NoticePriority.LOW, NoticeVisibility.PUBLIC, null, now, now.plusDays(4), false, true},
                {"FE Woodwork Workshop: Mandatory safety dress code guidelines", "workshop-safety-dresscode", "Students must wear closed shoes and avoid loose clothes in the FE Woodwork Workshop.", NoticeCategory.ADMINISTRATIVE, NoticePriority.HIGH, NoticeVisibility.STUDENTS, null, now.minusDays(2), now.plusDays(90), false, true},
                {"Canteen: New digital menu card and hygiene guidelines", "canteen-digital-menu", "Canteen food order goes digital. Scan QR codes at tables to view menu and pay.", NoticeCategory.GENERAL, NoticePriority.LOW, NoticeVisibility.PUBLIC, null, now.minusDays(5), now.plusDays(90), false, true},
                {"SORT Donation Box placements notice", "sort-donation-boxes", "Donation boxes for books and clothes are placed at Ground floor lobby near admission office.", NoticeCategory.GENERAL, NoticePriority.LOW, NoticeVisibility.PUBLIC, sortId, now.minusDays(2), now.plusDays(20), false, true},
                {"Alok Bajpai: Industrial automation SCADA lab slots allocation", "scada-lab-slots", "Batch-wise lab slot allocation for SCADA laboratory practicals on the 1st floor.", NoticeCategory.ACADEMIC, NoticePriority.MEDIUM, NoticeVisibility.STUDENTS, null, now.minusDays(1), now.plusDays(6), false, true},
                {"Principal's Office: Strict anti-ragging measures warning", "anti-ragging-warning", "VESIT maintains zero tolerance for ragging. Contact principal's office immediately to report any issues.", NoticeCategory.ADMINISTRATIVE, NoticePriority.URGENT, NoticeVisibility.PUBLIC, null, now.minusDays(10), now.plusDays(180), true, true}
        };

        for (Object[] data : noticesData) {
            String slug = (String) data[1];
            if (noticeRepository.existsBySlug(slug)) {
                continue;
            }
            Notice notice = Notice.builder()
                    .id(UUID.randomUUID())
                    .title((String) data[0])
                    .slug(slug)
                    .content((String) data[2])
                    .category((NoticeCategory) data[3])
                    .priority((NoticePriority) data[4])
                    .visibility((NoticeVisibility) data[5])
                    .councilId((UUID) data[6])
                    .publishedAt((LocalDateTime) data[7])
                    .expiresAt((LocalDateTime) data[8])
                    .isPinned((Boolean) data[9])
                    .isPublished((Boolean) data[10])
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            noticeRepository.save(notice);
        }
    }

    private void seedEvents(Map<String, Council> councils) {
        log.info("Seeding Events...");
        long count = eventRepository.count();
        if (count > 20) {
            log.info("Events already seeded. Skipping.");
            return;
        }

        UUID csiId = councils.get("csi").getId();
        UUID ieeeId = councils.get("ieee").getId();
        UUID ccId = councils.get("cc").getId();
        UUID sportsId = councils.get("sports").getId();
        UUID sortId = councils.get("sort").getId();
        UUID isteId = councils.get("iste").getId();
        UUID isaId = councils.get("isa").getId();
        UUID veslangId = councils.get("veslang").getId();
        UUID veslitId = councils.get("veslit").getId();

        LocalDateTime now = LocalDateTime.now();

        // Let's create some overlapping events for conflict detection testing
        LocalDateTime day1 = now.plusDays(1);
        LocalDateTime day2 = now.plusDays(2);

        Object[][] eventData = {
                // Ground Floor (Auditorium Overlaps)
                {"CSI Intro to Python Workshop", "csi-python-intro", "Learn python programming basics.", csiId, "Auditorium", EventType.WORKSHOP, EventStatus.PUBLISHED, day1.withHour(10).withMinute(0), day1.withHour(12).withMinute(0), 120},
                {"VESLANG Elocution Championship", "veslang-elocution", "Public speaking and debate contest.", veslangId, "Auditorium", EventType.OTHER, EventStatus.PUBLISHED, day1.withHour(11).withMinute(0), day1.withHour(13).withMinute(0), 50}, // Overlaps with CSI workshop in Auditorium
                
                // 3rd Floor (CMPN Lab 302 Overlaps)
                {"ISTE Web Development Bootcamp", "iste-web-bootcamp", "Hands-on HTML/CSS and Javascript coding boot camp.", isteId, "CMPN Lab 302", EventType.WORKSHOP, EventStatus.PUBLISHED, day2.withHour(14).withMinute(0), day2.withHour(17).withMinute(0), 40},
                {"ISA Automation Lab Demo Session", "isa-automation-demo", "Demo on industrial automation tools and PLC devices.", isaId, "CMPN Lab 302", EventType.WORKSHOP, EventStatus.PUBLISHED, day2.withHour(15).withMinute(0), day2.withHour(16).withMinute(0), 30}, // Overlaps with ISTE bootcamp in CMPN Lab 302
                
                // Other Academic / Technical events
                {"VESIT Hackathon 2026", "vesit-hack-2026", "24-hour programming hackathon with cash rewards.", csiId, "CMPN Lab 302", EventType.HACKATHON, EventStatus.PUBLISHED, now.plusDays(15), now.plusDays(16), 100},
                {"Robotics and IoT Seminar", "ieee-robotics-iot", "Seminar on microcontroller applications.", ieeeId, "1st Floor Seminar Room", EventType.SEMINAR, EventStatus.PUBLISHED, now.plusDays(5), now.plusDays(5), 80},
                {"IEEE Coding Conclave", "ieee-coding-conclave", "Competitive programming contest on algorithms and data structures.", ieeeId, "IT Lab 502", EventType.HACKATHON, EventStatus.PUBLISHED, now.plusDays(8), now.plusDays(8).plusHours(4), 120},
                {"Guest Lecture: AI & ML Ethics", "aiml-ethics-lecture", "Guest lecture by HOD Dr. Sanjay Patel on neural network bias.", csiId, "AIDS Lab 202", EventType.SEMINAR, EventStatus.PUBLISHED, now.plusDays(4), now.plusDays(4).plusHours(2), 100},
                {"DBMS Lab Practical Prep", "dbms-practical-prep", "DBMS database query practice and revision session.", csiId, "CMPN Lab 302", EventType.WORKSHOP, EventStatus.COMPLETED, now.minusDays(10), now.minusDays(10).plusHours(2), 60},
                {"Computer Networks Packet Capture Lab", "cn-packet-capture-lab", "Wireshark packet analysis and TCP handshake lab session.", ieeeId, "CSH-302", EventType.WORKSHOP, EventStatus.COMPLETED, now.minusDays(5), now.minusDays(5).plusHours(2), 60},
                {"Operating Systems Project Review", "os-project-review-day", "Evaluation of system call implementation projects.", csiId, "Room 305", EventType.SEMINAR, EventStatus.PUBLISHED, now.plusDays(12), now.plusDays(12).plusHours(3), 50},
                {"Web Technologies React Quiz", "wt-react-quiz", "React state management and hook API online quiz contest.", isteId, "CSH-101", EventType.OTHER, EventStatus.PUBLISHED, now.plusDays(6), now.plusDays(6).plusHours(1), 80},
                
                // Cultural & Sports
                {"Annual Cultural Festival (Utsav)", "utsav-cultural-2026", "The grand cultural celebration.", ccId, "Amphitheatre", EventType.CULTURAL, EventStatus.PUBLISHED, now.plusDays(30), now.plusDays(35), 500},
                {"Inter-College Cricket Tournament", "cricket-tournament-2026", "Cricket tournament at VES Cricket Academy.", sportsId, "VES Cricket Academy", EventType.SPORTS, EventStatus.PUBLISHED, now.plusDays(20), now.plusDays(25), 200},
                {"VESLIT Poetry Slam Competition", "veslit-poetry-slam", "Spoken word poetry competition organized by literature council.", veslitId, "Amphitheatre", EventType.CULTURAL, EventStatus.PUBLISHED, now.plusDays(14), now.plusDays(14).plusHours(3), 80},
                {"Hostel Block 1 Welcome Party", "hostel-welcome-party", "Informal social gathering and music night for hostel residents.", ccId, "Hostel Lawn", EventType.CULTURAL, EventStatus.PUBLISHED, now.plusDays(3), now.plusDays(3).plusHours(4), 150},
                {"Hostel Block 2 Yoga Session", "hostel-yoga-session", "Morning health and fitness session for hostel residents.", sportsId, "Hostel Terrace", EventType.SPORTS, EventStatus.PUBLISHED, now.plusDays(7).withHour(7).withMinute(0), now.plusDays(7).withHour(8).withMinute(30), 50},
                
                // Social Outreach & Campus
                {"SORT Donation Drive for Orphanage", "sort-donation-drive", "Collect clothes, toys, and books for child welfare.", sortId, "Ground Floor Lobby", EventType.OTHER, EventStatus.PUBLISHED, now.plusDays(3), now.plusDays(5), 100},
                {"First Year Orientation Session", "fe-orientation-event", "Official orientation program welcoming new admissions and parents.", ccId, "Auditorium", EventType.SEMINAR, EventStatus.COMPLETED, now.minusDays(15), now.minusDays(15).plusHours(4), 400},
                {"TCS Placement Recruitment Prep", "tcs-placement-prep", "Mock test and placement strategy session by TCS hr representatives.", csiId, "Auditorium", EventType.SEMINAR, EventStatus.PUBLISHED, now.plusDays(10), now.plusDays(10).plusHours(3), 300},
                {"Library Books Exhibition", "library-books-exhibition", "Showcase of engineering books and digital database journals.", ieeeId, "Library Reading Room", EventType.OTHER, EventStatus.PUBLISHED, now.plusDays(2), now.plusDays(4), 100},
                {"FE Metalwork Workshop Practice", "fe-metalwork-practice", "Optional practice hours for welding and sheet metal shop tasks.", ccId, "FE Metalwork Workshop", EventType.WORKSHOP, EventStatus.PUBLISHED, now.plusDays(9), now.plusDays(9).plusHours(4), 40},
                {"Admission Inquiry Webinar", "admission-inquiry-webinar", "Online information session addressing queries on courses, fees, and forms.", csiId, "Zoom Online", EventType.SEMINAR, EventStatus.PUBLISHED, now.plusDays(11), now.plusDays(11).plusHours(2), 200},
                {"Canteen Food Festival", "canteen-food-fest", "Exhibition of food stalls and local cuisines.", sportsId, "Canteen Area", EventType.OTHER, EventStatus.PUBLISHED, now.plusDays(18), now.plusDays(19), 300},
                {"Principal's Meeting with Class Reps", "principal-cr-meeting", "Discussion on academic progress and student feedback.", csiId, "Principal's Office", EventType.SEMINAR, EventStatus.PUBLISHED, now.plusDays(7), now.plusDays(7).plusHours(1), 20}
        };

        for (Object[] data : eventData) {
            String slug = (String) data[1];
            if (eventRepository.existsBySlug(slug)) {
                continue;
            }
            Event event = Event.builder()
                    .id(UUID.randomUUID())
                    .title((String) data[0])
                    .slug(slug)
                    .description((String) data[2])
                    .councilId((UUID) data[3])
                    .venue((String) data[4])
                    .eventType((EventType) data[5])
                    .status((EventStatus) data[6])
                    .startTime((LocalDateTime) data[7])
                    .endTime((LocalDateTime) data[8])
                    .capacity((Integer) data[9])
                    .registrationRequired(true)
                    .registrationStart(now.minusDays(5))
                    .registrationEnd(now.plusDays(10))
                    .contactEmail("event.admin@ves.ac.in")
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            eventRepository.save(event);
        }
    }

    private void seedPlannerTasksAndGoals(String goldenStudentId, Map<String, User> students) {
        log.info("Seeding Planner Tasks & Goals...");
        long count = plannerTaskRepository.count();
        if (count > 5) {
            log.info("Planner tasks already seeded. Skipping.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        Object[][] goldenTasks = {
                {"Complete DBMS Assignment 2", "Submit relational algebra queries on portal.", TaskType.ASSIGNMENT, TaskPriority.HIGH, TaskStatus.IN_PROGRESS, now.plusDays(1)},
                {"Prepare for Computer Networks Exam", "Review OSI model, subnetting, TCP flow control.", TaskType.EXAM, TaskPriority.HIGH, TaskStatus.IN_PROGRESS, now.plusDays(3)},
                {"Submit Web Technologies Lab record", "Upload React routing project GitHub link.", TaskType.ASSIGNMENT, TaskPriority.MEDIUM, TaskStatus.COMPLETED, now.minusDays(1)},
                {"Review ML Project Proposal", "Send proposal to Prof. Aarti Gupta for feedback.", TaskType.PROJECT, TaskPriority.LOW, TaskStatus.IN_PROGRESS, now.plusDays(7)},
                {"Attend GDG Cloud Workshop", "Hands-on training session at AIDS lab.", TaskType.MEETING, TaskPriority.MEDIUM, TaskStatus.IN_PROGRESS, now.plusDays(2)},
                {"Set up Python environment", "Install Anaconda and Jupyter notebooks.", TaskType.STUDY, TaskPriority.MEDIUM, TaskStatus.COMPLETED, now.minusDays(5)},
                {"Complete Python Data Science Course", "Learn NumPy, Pandas, and Matplotlib data visualization techniques.", TaskType.STUDY, TaskPriority.MEDIUM, TaskStatus.IN_PROGRESS, now.plusDays(5)}
        };

        for (Object[] data : goldenTasks) {
            PlannerTask task = PlannerTask.builder()
                    .id(UUID.randomUUID())
                    .userId(goldenStudentId)
                    .title((String) data[0])
                    .description((String) data[1])
                    .type((TaskType) data[2])
                    .priority((TaskPriority) data[3])
                    .status((TaskStatus) data[4])
                    .dueAt((LocalDateTime) data[5])
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            plannerTaskRepository.save(task);

            CalendarEntry entry = CalendarEntry.builder()
                    .id(UUID.randomUUID())
                    .userId(goldenStudentId)
                    .title((String) data[0])
                    .description((String) data[1])
                    .type(CalendarEntryType.TASK)
                    .linkedPlannerTaskId(task.getId())
                    .startTime((LocalDateTime) data[5])
                    .endTime(((LocalDateTime) data[5]).plusHours(1))
                    .isAllDay(false)
                    .color("#3f51b5")
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            calendarEntryRepository.save(entry);
        }

        int tIdx = 0;
        for (User student : students.values()) {
            if (student.getEmail().equals("golden.student@ves.ac.in")) continue;
            if (tIdx > 35) break;

            PlannerTask task = PlannerTask.builder()
                    .id(UUID.randomUUID())
                    .userId(student.getId())
                    .title("Student task " + tIdx)
                    .description("Seeded task details for general student QA context.")
                    .type(TaskType.TODO)
                    .priority(TaskPriority.MEDIUM)
                    .status(tIdx % 2 == 0 ? TaskStatus.COMPLETED : TaskStatus.IN_PROGRESS)
                    .dueAt(now.plusDays(tIdx % 10 + 1))
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            plannerTaskRepository.save(task);
            tIdx++;
        }
    }

    private void seedAchievements(String goldenStudentId, Map<String, User> students) {
        log.info("Seeding Achievements...");
        long count = achievementProgressRepository.count();
        if (count > 5) {
            log.info("Achievements already seeded. Skipping.");
            return;
        }

        Object[][] achData = {
                {"ach-dbms", "Database Champion", "Master Relational Algebra, SQL, and Normalization.", AchievementCategory.ACADEMIC, AchievementStatus.EARNED, 100},
                {"ach-ml-foundations", "Machine Learning Starter", "Successfully complete AI & ML Club foundations course.", AchievementCategory.SKILLS, AchievementStatus.IN_PROGRESS, 50},
                {"ach-git-ninja", "Git and GitHub Ninja", "Successfully push 10 projects and manage pull requests.", AchievementCategory.SKILLS, AchievementStatus.IN_PROGRESS, 75},
                {"ach-sports", "Cricket Academy Athlete", "Participate in sports tournament.", AchievementCategory.CAMPUS_LIFE, AchievementStatus.LOCKED, 0},
                {"ach-sort-help", "Social Work Helper", "Volunteer for 2 SORT donation drives.", AchievementCategory.COMMUNITY, AchievementStatus.IN_PROGRESS, 20}
        };

        for (Object[] data : achData) {
            AchievementProgress progress = AchievementProgress.builder()
                    .id(UUID.randomUUID())
                    .userId(goldenStudentId)
                    .achievementCode((String) data[0])
                    .title((String) data[1])
                    .description((String) data[2])
                    .category((AchievementCategory) data[3])
                    .status((AchievementStatus) data[4])
                    .progress((Integer) data[5])
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            achievementProgressRepository.save(progress);
        }
    }

    private void seedNotifications(String goldenStudentId, Map<String, User> students) {
        log.info("Seeding Notifications...");
        long count = notificationRepository.count();
        if (count > 10) {
            log.info("Notifications already seeded. Skipping.");
            return;
        }

        Object[][] notData = {
                {"Exam Timetable Released", "Midterm timetable for TE CMPN is now available.", NotificationType.ACADEMIC, NotificationPriority.HIGH, false},
                {"Cloud Inception Workshop Registration", "You have successfully registered for the GDG Cloud workshop.", NotificationType.EVENT, NotificationPriority.NORMAL, true},
                {"Blood Donation Camp Today", "SORT is organizing a donation camp at the Ground Floor Lobby from 10 AM.", NotificationType.SYSTEM, NotificationPriority.NORMAL, false},
                {"New DBMS Study Guide Uploaded", "A new DBMS rel algebra PDF is available in resources.", NotificationType.ACADEMIC, NotificationPriority.LOW, false},
                {"Placement Committee Announcement", "Special orientation session by placement committee in CSH-101 tomorrow.", NotificationType.ACADEMIC, NotificationPriority.HIGH, false},
                {"Task Overdue Reminder", "Your Web Technologies Lab record submission task is overdue.", NotificationType.REMINDER, NotificationPriority.HIGH, false},
                {"CSI Membership Confirmed", "Welcome to CSI VESIT Chapter. Explore communities and workshops.", NotificationType.COMMUNITY, NotificationPriority.LOW, true},
                {"Security Advisory: Lift maintenance", "Rear section lifts will be shut down this Sunday.", NotificationType.SYSTEM, NotificationPriority.LOW, true}
        };

        for (Object[] data : notData) {
            Notification notification = Notification.builder()
                    .userId(goldenStudentId)
                    .title((String) data[0])
                    .message((String) data[1])
                    .type((NotificationType) data[2])
                    .priority((NotificationPriority) data[3])
                    .read((Boolean) data[4])
                    .createdAt(Instant.now().minus(new Random().nextInt(10) + 1, ChronoUnit.HOURS))
                    .build();
            notificationRepository.save(notification);
        }

        int nIdx = 0;
        for (User student : students.values()) {
            if (student.getEmail().equals("golden.student@ves.ac.in")) continue;
            if (nIdx > 42) break;

            Notification notification = Notification.builder()
                    .userId(student.getId())
                    .title("System Notification " + nIdx)
                    .message("Seeded MVP student alert notification for testing.")
                    .type(NotificationType.SYSTEM)
                    .priority(NotificationPriority.NORMAL)
                    .read(nIdx % 2 == 0)
                    .createdAt(Instant.now().minus(nIdx, ChronoUnit.HOURS))
                    .build();
            notificationRepository.save(notification);
            nIdx++;
        }
    }

    private void seedPostsAndComments(String goldenStudentId, Map<String, User> students, Map<String, Community> communities) {
        log.info("Seeding Posts and Comments...");
        long count = postRepository.count();
        if (count > 5) {
            log.info("Posts already seeded. Skipping.");
            return;
        }

        Community gdg = communities.get("Google Developer Groups (GDG)");
        Community aiMl = communities.get("AI & ML Club");

        if (gdg == null || aiMl == null) return;

        Post post1 = Post.builder()
                .title("Excited for Cloud Computing Workshop!")
                .content("Hey guys, the GDG Cloud workshop this Thursday seems awesome. Who else is joining? I am looking to set up AWS/Docker cluster.")
                .authorId(goldenStudentId)
                .communityId(gdg.getId())
                .likeCount(15)
                .commentCount(3)
                .isPinned(true)
                .isEdited(false)
                .isDeleted(false)
                .createdAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();
        post1 = postRepository.save(post1);

        Post post2 = Post.builder()
                .title("ML Foundations Project Partners Needed")
                .content("Looking for 2 partners from TE CMPN or AIDS for the upcoming ML group project. Target is NLP Sentiment analysis.")
                .authorId(goldenStudentId)
                .communityId(aiMl.getId())
                .likeCount(8)
                .commentCount(2)
                .isPinned(false)
                .isEdited(false)
                .isDeleted(false)
                .createdAt(Instant.now().minus(12, ChronoUnit.HOURS))
                .build();
        post2 = postRepository.save(post2);

        User student2 = students.values().stream()
                .filter(u -> !u.getEmail().equals("golden.student@ves.ac.in"))
                .findFirst().orElse(null);

        if (student2 != null) {
            Comment comment1 = Comment.builder()
                    .postId(post1.getId())
                    .authorId(student2.getId())
                    .content("I am definitely joining! I want to understand Docker container scaling.")
                    .isEdited(false)
                    .isDeleted(false)
                    .createdAt(Instant.now().minus(10, ChronoUnit.HOURS))
                    .build();
            commentRepository.save(comment1);

            Comment comment2 = Comment.builder()
                    .postId(post1.getId())
                    .authorId(goldenStudentId)
                    .content("Awesome! Let's sit together at the lab.")
                    .isEdited(false)
                    .isDeleted(false)
                    .createdAt(Instant.now().minus(8, ChronoUnit.HOURS))
                    .build();
            commentRepository.save(comment2);

            Comment comment3 = Comment.builder()
                    .postId(post2.getId())
                    .authorId(student2.getId())
                    .content("Hey, I am interested! I have completed Python foundations. Let's collaborate.")
                    .isEdited(false)
                    .isDeleted(false)
                    .createdAt(Instant.now().minus(5, ChronoUnit.HOURS))
                    .build();
            commentRepository.save(comment3);
        }

        int pIdx = 0;
        for (User student : students.values()) {
            if (student.getEmail().equals("golden.student@ves.ac.in")) continue;
            if (pIdx > 20) break;

            Post p = Post.builder()
                    .title("Community discussion topic " + pIdx)
                    .content("Seeded student query regarding computer engineering or tech club activities.")
                    .authorId(student.getId())
                    .communityId(pIdx % 2 == 0 ? gdg.getId() : aiMl.getId())
                    .likeCount(pIdx)
                    .commentCount(1)
                    .isPinned(false)
                    .isEdited(false)
                    .isDeleted(false)
                    .createdAt(Instant.now().minus(pIdx + 2, ChronoUnit.DAYS))
                    .build();
            p = postRepository.save(p);

            Comment c = Comment.builder()
                    .postId(p.getId())
                    .authorId(goldenStudentId)
                    .content("Good point! We should discuss this in the next community meetup.")
                    .isEdited(false)
                    .isDeleted(false)
                    .createdAt(Instant.now().minus(pIdx + 1, ChronoUnit.DAYS))
                    .build();
            commentRepository.save(c);

            pIdx++;
        }
    }

    private void seedAtlasConversations(String goldenStudentId) {
        log.info("Seeding Atlas Conversations...");
        long count = conversationRepository.count();
        if (count > 0) {
            log.info("Conversations already seeded. Skipping.");
            return;
        }

        Conversation conv = Conversation.builder()
                .userId(goldenStudentId)
                .title("AIDS HOD Location")
                .type(ConversationType.CAMPUS_ASSISTANT)
                .status(ConversationStatus.ACTIVE)
                .createdAt(Instant.now().minus(2, ChronoUnit.HOURS))
                .updatedAt(Instant.now().minus(2, ChronoUnit.HOURS))
                .build();
        conv = conversationRepository.save(conv);

        Message m1 = Message.builder()
                .conversationId(conv.getId())
                .role(MessageRole.USER)
                .content("Where is the AIDS HOD office?")
                .timestamp(Instant.now().minus(2, ChronoUnit.HOURS))
                .build();
        messageRepository.save(m1);

        Message m2 = Message.builder()
                .conversationId(conv.getId())
                .role(MessageRole.ASSISTANT)
                .content("The AIDS HOD Office is located on the 2nd floor of the VESIT building.")
                .timestamp(Instant.now().minus(2, ChronoUnit.HOURS).plus(5, ChronoUnit.SECONDS))
                .build();
        messageRepository.save(m2);

        Message m3 = Message.builder()
                .conversationId(conv.getId())
                .role(MessageRole.USER)
                .content("Thanks! What about the library?")
                .timestamp(Instant.now().minus(1, ChronoUnit.HOURS))
                .build();
        messageRepository.save(m3);

        Message m4 = Message.builder()
                .conversationId(conv.getId())
                .role(MessageRole.ASSISTANT)
                .content("The library is on the 1st floor of the VESIT building. It is open from 08:00 to 20:00.")
                .timestamp(Instant.now().minus(1, ChronoUnit.HOURS).plus(10, ChronoUnit.SECONDS))
                .build();
        messageRepository.save(m4);
    }

    private List<CourseData> getCourseDataCatalog() {
        List<CourseData> list = new ArrayList<>();
        list.add(new CourseData("EM1", "Engineering Mathematics I", "Matrices, Calculus, complex numbers.", "CMPN", 4, 1, false));
        list.add(new CourseData("EM2", "Engineering Mathematics II", "Differential equations, integral calculus.", "CMPN", 4, 2, false));
        list.add(new CourseData("EP", "Engineering Physics", "Optics, quantum physics, nanotechnology.", "CMPN", 3, 1, false));
        list.add(new CourseData("EC", "Engineering Chemistry", "Water analysis, fuels, polymers.", "CMPN", 3, 1, false));
        list.add(new CourseData("EG", "Engineering Graphics", "Engineering drawing, CAD design.", "CMPN", 3, 1, false));
        list.add(new CourseData("PF", "Programming Fundamentals", "Structured programming concepts using C.", "CMPN", 4, 1, false));
        list.add(new CourseData("BEE", "Basic Electrical Engineering", "DC circuits, AC circuits, transformers.", "CMPN", 3, 2, false));
        list.add(new CourseData("BE", "Basic Electronics", "Diodes, BJTs, op-amps.", "CMPN", 3, 2, false));
        list.add(new CourseData("EMech", "Engineering Mechanics", "Forces, equilibrium, friction.", "CMPN", 3, 2, false));

        list.add(new CourseData("DS", "Data Structures", "Stacks, queues, trees, graphs.", "CMPN", 4, 3, false));
        list.add(new CourseData("OOP", "Object-Oriented Programming", "Java programming, classes, inheritance.", "CMPN", 3, 3, false));
        list.add(new CourseData("DBMS", "Database Management Systems", "SQL, ER diagrams, normalization.", "CMPN", 4, 4, false));
        list.add(new CourseData("CN", "Computer Networks", "OSI model, routing, socket programming.", "CMPN", 4, 4, false));
        list.add(new CourseData("OS", "Operating Systems", "Process scheduling, memory, filesystems.", "CMPN", 4, 5, false));
        list.add(new CourseData("SE", "Software Engineering", "SDLC models, agile, testing.", "CMPN", 3, 5, false));
        list.add(new CourseData("WT", "Web Technologies", "HTML, CSS, JS, React, NodeJS.", "CMPN", 3, 6, false));
        list.add(new CourseData("DistS", "Distributed Systems", "RPC, consensus, cloud storage.", "CMPN", 4, 6, true));
        list.add(new CourseData("CC", "Cloud Computing", "AWS, virtualization, cloud services.", "CMPN", 4, 7, false));
        list.add(new CourseData("AI", "Artificial Intelligence", "Search algorithms, knowledge representation.", "CMPN", 4, 7, false));

        list.add(new CourseData("PS", "Probability & Statistics", "Hypothesis testing, probability distribution.", "AIDS", 4, 3, false));
        list.add(new CourseData("DA", "Data Analytics", "Pandas, Numpy, visualization.", "AIDS", 4, 4, false));
        list.add(new CourseData("ML", "Machine Learning", "Regression, classification, clustering.", "AIDS", 4, 5, false));
        list.add(new CourseData("DL", "Deep Learning", "CNN, RNN, Neural Networks.", "AIDS", 4, 6, false));
        list.add(new CourseData("NLP", "Natural Language Processing", "BERT, Transformers, tokenization.", "AIDS", 4, 7, true));
        list.add(new CourseData("CV", "Computer Vision", "OpenCV, image processing.", "AIDS", 4, 7, true));
        list.add(new CourseData("GenAI", "Generative AI", "GANs, Diffusion Models, LLMs.", "AIDS", 4, 8, false));
        list.add(new CourseData("MLOps", "MLOps", "Model deployment, pipelines, monitoring.", "AIDS", 3, 8, true));

        list.add(new CourseData("DE_ECS", "Digital Electronics", "Logic gates, combinational circuits.", "ECS", 4, 3, false));
        list.add(new CourseData("MPMC_ECS", "Microprocessors & Microcontrollers", "8086, 8051 assembly.", "ECS", 4, 4, false));
        list.add(new CourseData("ES_ECS", "Embedded Systems", "RTOS, system-on-chip design.", "ECS", 4, 5, false));
        list.add(new CourseData("VLSI_ECS", "VLSI Design", "CMOS circuits, VHDL/Verilog.", "ECS", 4, 6, false));
        list.add(new CourseData("IoT_ECS", "Internet of Things", "Sensors, esp8266, cloud sync.", "ECS", 3, 7, true));
        list.add(new CourseData("CA_ECS", "Computer Architecture", "CPU design, pipelining.", "ECS", 4, 8, false));

        list.add(new CourseData("SS_EXTC", "Signals & Systems", "Fourier transform, LTI systems.", "EXTC", 4, 3, false));
        list.add(new CourseData("DE_EXTC", "Digital Electronics EXTC", "Logic design and gates.", "EXTC", 4, 3, false));
        list.add(new CourseData("CN_EXTC", "Communication Networks", "Ethernet, IP routing.", "EXTC", 4, 4, false));
        list.add(new CourseData("DC_EXTC", "Digital Communication", "ASK, PSK, FSK modulation.", "EXTC", 4, 5, false));
        list.add(new CourseData("ES_EXTC", "Embedded Systems EXTC", "Microcontroller interfacing.", "EXTC", 4, 6, false));
        list.add(new CourseData("VLSI_EXTC", "VLSI EXTC", "Analog and digital IC design.", "EXTC", 4, 7, true));

        list.add(new CourseData("RF_AURO", "Robotics Fundamentals", "Kinematics, dynamics, actuators.", "AURO", 4, 3, false));
        list.add(new CourseData("AC_AURO", "Automation & Control", "Feedback control, PID tuning.", "AURO", 4, 4, false));
        list.add(new CourseData("ES_AURO", "Embedded Systems AURO", "Realtime robotics control.", "AURO", 4, 5, false));
        list.add(new CourseData("IA_AURO", "Industrial Automation", "SCADA, PLC programming.", "AURO", 4, 6, false));
        list.add(new CourseData("CV_AURO", "Computer Vision AURO", "Object tracking, edge detection.", "AURO", 4, 7, true));
        list.add(new CourseData("IoT_AURO", "Robotics IoT", "NodeMCU, cloud robotics.", "AURO", 3, 8, true));

        return list;
    }

    private static class CourseData {
        String code;
        String name;
        String desc;
        String dept;
        int credits;
        int sem;
        boolean elective;

        CourseData(String code, String name, String desc, String dept, int credits, int sem, boolean elective) {
            this.code = code;
            this.name = name;
            this.desc = desc;
            this.dept = dept;
            this.credits = credits;
            this.sem = sem;
            this.elective = elective;
        }
    }
}
