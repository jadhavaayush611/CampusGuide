package com.campusguide.personal.ai.recommendation.dto;

import com.campusguide.campus.community.entity.Community;
import com.campusguide.academic.course.entity.Course;
import com.campusguide.campus.event.entity.Event;
import com.campusguide.campus.post.entity.Post;
import com.campusguide.academic.progress.entity.StudentProgress;
import com.campusguide.campus.resource.entity.Resource;
import com.campusguide.academic.roadmap.entity.Roadmap;
import com.campusguide.academic.semesterplanner.entity.SemesterPlan;
import com.campusguide.platform.user.entity.User;
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
