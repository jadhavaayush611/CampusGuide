package com.campusguide.personal.planner;

import com.campusguide.common.migration.V1_1__MVPSeedDataset;
import com.campusguide.personal.planner.entity.PlannerTask;
import com.campusguide.personal.planner.entity.StudyGoal;
import com.campusguide.personal.planner.entity.TaskType;
import com.campusguide.personal.planner.repository.PlannerTaskRepository;
import com.campusguide.personal.planner.repository.StudyGoalRepository;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PlannerGoldenSeederIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlannerTaskRepository plannerTaskRepository;

    @Autowired
    private StudyGoalRepository studyGoalRepository;

    @Autowired
    private V1_1__MVPSeedDataset migration;

    @Autowired
    private MongoTemplate mongoTemplate;

    @org.junit.jupiter.api.BeforeEach
    void setUp() throws Exception {
        migration.execute(mongoTemplate);
    }

    @Test
    void verifyGoldenQAStudentSeedDataAndIdempotency() throws Exception {
        // Find Golden QA Student by Email
        User golden = userRepository.findByEmail("golden.student@ves.ac.in")
                .orElseThrow(() -> new AssertionError("Golden student not found"));

        // 1. Verify username is test.student
        assertEquals("test.student", golden.getUsername(), "Golden QA Student username must be test.student");
        assertEquals("golden.student@ves.ac.in", golden.getEmail());

        // 2. Verify exactly 7 planner tasks exist for golden student
        List<PlannerTask> tasks = plannerTaskRepository.findByUserId(golden.getId());
        assertEquals(7, tasks.size(), "Golden student must have exactly 7 planner tasks");

        // 3. Verify exactly 2 tasks are of type STUDY
        long studyTaskCount = tasks.stream()
                .filter(t -> t.getType() == TaskType.STUDY)
                .count();
        assertEquals(2, studyTaskCount, "Golden student must have exactly 2 STUDY planner tasks");

        // 4. Verify exactly 2 Study Goals in MongoDB
        List<StudyGoal> goals = studyGoalRepository.findByUserId(golden.getId());
        assertEquals(2, goals.size(), "Golden student must have exactly 2 Study Goals");

        // 5. Test idempotency: re-running migration must not duplicate or corrupt
        migration.execute(mongoTemplate);

        User reloadedGolden = userRepository.findByEmail("golden.student@ves.ac.in")
                .orElseThrow(() -> new AssertionError("Golden student not found after re-executing migration"));
        assertEquals("test.student", reloadedGolden.getUsername());

        List<PlannerTask> tasksAfter = plannerTaskRepository.findByUserId(reloadedGolden.getId());
        assertEquals(7, tasksAfter.size(), "Tasks must remain exactly 7 after re-running migration");

        List<StudyGoal> goalsAfter = studyGoalRepository.findByUserId(reloadedGolden.getId());
        assertEquals(2, goalsAfter.size(), "Goals must remain exactly 2 after re-running migration");
    }
}
