package com.campusguide.modules.ai.recommendation.dto;

import com.campusguide.modules.community.entity.Community;
import com.campusguide.modules.course.entity.Course;
import com.campusguide.modules.event.entity.Event;
import com.campusguide.modules.post.entity.Post;
import com.campusguide.modules.progress.entity.StudentProgress;
import com.campusguide.modules.resource.entity.Resource;
import com.campusguide.modules.roadmap.entity.Roadmap;
import com.campusguide.modules.semester.entity.SemesterPlan;
import com.campusguide.modules.user.entity.User;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecommendationUserContext {
    private final User user;
    private final StudentProgress studentProgress;
    private final List<String> completedCourseIds;
    private final Integer currentSemester;
    private final List<Course> allActiveCourses;
    private final List<Roadmap> roadmaps;
    private final List<SemesterPlan> semesterPlans;
    private final List<Post> userPosts;
    private final List<Event> upcomingEvents;
    private final List<Community> allActiveCommunities;
    private final List<Resource> allActiveResources;
}
