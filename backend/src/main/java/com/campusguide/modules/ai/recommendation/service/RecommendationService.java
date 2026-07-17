package com.campusguide.modules.ai.recommendation.service;

import com.campusguide.exception.ResourceNotFoundException;
import com.campusguide.exception.UnauthorisedException;
import com.campusguide.modules.ai.recommendation.dto.RecommendationResponse;
import com.campusguide.modules.ai.recommendation.dto.RecommendationType;
import com.campusguide.modules.ai.recommendation.dto.RecommendationUserContext;
import com.campusguide.modules.ai.recommendation.engine.RecommendationEngine;
import com.campusguide.modules.community.entity.Community;
import com.campusguide.modules.community.repository.CommunityRepository;
import com.campusguide.modules.course.entity.Course;
import com.campusguide.modules.course.repository.CourseRepository;
import com.campusguide.modules.event.entity.Event;
import com.campusguide.modules.event.repository.EventRepository;
import com.campusguide.modules.post.entity.Post;
import com.campusguide.modules.post.repository.PostRepository;
import com.campusguide.modules.progress.entity.StudentProgress;
import com.campusguide.modules.progress.repository.StudentProgressRepository;
import com.campusguide.modules.resource.entity.Resource;
import com.campusguide.modules.resource.repository.ResourceRepository;
import com.campusguide.modules.roadmap.entity.Roadmap;
import com.campusguide.modules.roadmap.repository.RoadmapRepository;
import com.campusguide.modules.semester.entity.SemesterPlan;
import com.campusguide.modules.semester.repository.SemesterPlanRepository;
import com.campusguide.modules.user.entity.User;
import com.campusguide.modules.user.repository.UserRepository;
import com.campusguide.modules.notification.service.interfaces.NotificationService;
import com.campusguide.modules.notification.enums.NotificationType;
import com.campusguide.modules.notification.enums.NotificationPriority;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);

    private final UserRepository userRepository;

    private final StudentProgressRepository studentProgressRepository;
    private final CourseRepository courseRepository;
    private final RoadmapRepository roadmapRepository;
    private final SemesterPlanRepository semesterPlanRepository;
    private final PostRepository postRepository;
    private final EventRepository eventRepository;
    private final CommunityRepository communityRepository;
    private final ResourceRepository resourceRepository;
    private final RecommendationEngine recommendationEngine;
    private final NotificationService notificationService;


    /**
     * Generates personalized recommendations for the authenticated user.
     *
     * @param userDetails the authenticated user details
     * @param type optional category filter
     * @param page optional page index for pagination (0-based)
     * @param size optional page size for pagination
     * @return sorted and optionally paginated list of recommendations
     */
    public List<RecommendationResponse> getRecommendations(UserDetails userDetails, RecommendationType type, Integer page, Integer size) {
        long startTime = System.nanoTime();
        logger.info("Recommendation generation start");

        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));

        // Build the user profile context
        RecommendationUserContext context = buildUserProfileContext(user);

        // Fetch recommendations from engine
        List<RecommendationResponse> recommendations;
        if (type != null) {
            recommendations = recommendationEngine.generateRecommendationsByType(context, type);
        } else {
            recommendations = recommendationEngine.generateAllRecommendations(context);
        }

        if (!recommendations.isEmpty() && !notificationService.hasUnreadNotificationOfType(user.getId(), NotificationType.AI)) {
            notificationService.createNotification(
                    user.getId(),
                    "New Recommendations Available",
                    "We have generated new personalized recommendations for you. Explore them in the AI dashboard!",
                    NotificationType.AI,
                    NotificationPriority.NORMAL,
                    null
            );
        }


        long endTime = System.nanoTime();
        double executionTimeMs = (endTime - startTime) / 1_000_000.0;

        // Count recommendations per category
        Map<RecommendationType, Long> counts = recommendations.stream()
                .collect(Collectors.groupingBy(RecommendationResponse::getRecommendationType, Collectors.counting()));

        logger.info("Recommendation generation finished in {} ms. Counts per category: ACADEMIC={}, EVENT={}, COMMUNITY={}, RESOURCE={}",
                String.format("%.2f", executionTimeMs),
                counts.getOrDefault(RecommendationType.ACADEMIC, 0L),
                counts.getOrDefault(RecommendationType.EVENT, 0L),
                counts.getOrDefault(RecommendationType.COMMUNITY, 0L),
                counts.getOrDefault(RecommendationType.RESOURCE, 0L));

        // Apply pagination
        if (page != null && size != null && page >= 0 && size > 0) {
            int start = page * size;
            if (start >= recommendations.size()) {
                return Collections.emptyList();
            }
            int end = Math.min(start + size, recommendations.size());
            return recommendations.subList(start, end);
        }

        return recommendations;
    }

    private RecommendationUserContext buildUserProfileContext(User user) {
        StudentProgress progress = studentProgressRepository.findByStudentId(user.getId()).orElse(null);

        List<String> completedCourseIds = progress != null && progress.getCompletedCourseIds() != null
                ? progress.getCompletedCourseIds()
                : Collections.emptyList();

        Integer currentSemester = progress != null ? progress.getCurrentSemester() : 1;

        List<Course> allActiveCourses = courseRepository.findByActiveTrueOrderByCourseCodeAsc();
        List<Roadmap> roadmaps = roadmapRepository.findByIsDeletedFalseOrderByCreatedAtDesc();
        List<SemesterPlan> semesterPlans = semesterPlanRepository.findByStudentIdOrderBySemesterNumberAsc(user.getId());
        List<Post> userPosts = postRepository.findByAuthorIdAndIsDeletedFalse(user.getId());
        List<Event> upcomingEvents = eventRepository.findByIsDeletedFalseAndIsCancelledFalseAndStartTimeGreaterThanEqualOrderByStartTimeAsc(LocalDateTime.now());
        List<Community> allActiveCommunities = communityRepository.findByIsActiveTrue();
        List<Resource> allActiveResources = resourceRepository.findByIsDeletedFalseOrderByCreatedAtDesc();

        return RecommendationUserContext.builder()
                .user(user)
                .studentProgress(progress)
                .completedCourseIds(completedCourseIds)
                .currentSemester(currentSemester)
                .allActiveCourses(allActiveCourses)
                .roadmaps(roadmaps)
                .semesterPlans(semesterPlans)
                .userPosts(userPosts)
                .upcomingEvents(upcomingEvents)
                .allActiveCommunities(allActiveCommunities)
                .allActiveResources(allActiveResources)
                .build();
    }
}
