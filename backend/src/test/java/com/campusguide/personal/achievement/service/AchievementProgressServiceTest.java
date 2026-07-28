package com.campusguide.personal.achievement.service;

import com.campusguide.personal.achievement.dto.AchievementProgressResponse;
import com.campusguide.personal.achievement.dto.CreateAchievementRequest;
import com.campusguide.personal.achievement.dto.UpdateAchievementProgressRequest;
import com.campusguide.personal.achievement.dto.UpdateAchievementRequest;
import com.campusguide.personal.achievement.entity.AchievementCategory;
import com.campusguide.personal.achievement.entity.AchievementProgress;
import com.campusguide.personal.achievement.entity.AchievementStatus;
import com.campusguide.personal.achievement.exception.AchievementAccessDeniedException;
import com.campusguide.personal.achievement.exception.AchievementAlreadyExistsException;
import com.campusguide.personal.achievement.exception.AchievementNotFoundException;
import com.campusguide.personal.achievement.exception.AchievementValidationException;
import com.campusguide.personal.achievement.mapper.AchievementProgressMapper;
import com.campusguide.personal.achievement.repository.AchievementProgressRepository;
import com.campusguide.personal.achievement.validation.AchievementValidator;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.campusguide.platform.user.service.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class AchievementProgressServiceTest {

    @Mock
    private AchievementProgressRepository achievementProgressRepository;

    @Spy
    private AchievementProgressMapper achievementProgressMapper;

    @Spy
    private AchievementValidator achievementValidator;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private AchievementProgressService service;

    private UUID userId;
    private User userEntity;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userEntity = User.builder()
                .id(userId.toString())
                .email("student@achievement.com")
                .username("student")
                .build();

        userDetails = org.springframework.security.core.userdetails.User.withUsername("student@achievement.com")
                .password("password")
                .authorities("ROLE_STUDENT")
                .build();

        lenient().when(currentUserService.getCurrentUserId(any())).thenReturn(userId.toString());
    }

    @Test
    void createAchievement_Success() {
        CreateAchievementRequest request = CreateAchievementRequest.builder()
                .achievementCode("CODE_100")
                .title("Dean's List")
                .description("Achieve high GPA")
                .category(AchievementCategory.ACADEMIC)
                .progress(50)
                .build();

        when(achievementProgressRepository.existsByUserIdAndAchievementCode(userId.toString(), "CODE_100")).thenReturn(false);
        when(achievementProgressRepository.save(any(AchievementProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AchievementProgressResponse response = service.createAchievement(userDetails, request);

        assertNotNull(response);
        assertEquals("CODE_100", response.getAchievementCode());
        assertEquals(50, response.getProgress());
        assertEquals(AchievementStatus.IN_PROGRESS, response.getStatus());
        assertNull(response.getEarnedAt());
    }

    @Test
    void createAchievement_Progress100_TransitionsToEarned() {
        CreateAchievementRequest request = CreateAchievementRequest.builder()
                .achievementCode("CODE_101")
                .title("Graduate")
                .category(AchievementCategory.ACADEMIC)
                .progress(100)
                .build();

        when(achievementProgressRepository.existsByUserIdAndAchievementCode(userId.toString(), "CODE_101")).thenReturn(false);
        when(achievementProgressRepository.save(any(AchievementProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AchievementProgressResponse response = service.createAchievement(userDetails, request);

        assertNotNull(response);
        assertEquals(AchievementStatus.EARNED, response.getStatus());
        assertNotNull(response.getEarnedAt());
    }

    @Test
    void createAchievement_DuplicateCode_ThrowsAlreadyExists() {
        CreateAchievementRequest request = CreateAchievementRequest.builder()
                .achievementCode("DUPLICATE_CODE")
                .title("Duplicate")
                .category(AchievementCategory.PERSONAL)
                .build();

        when(achievementProgressRepository.existsByUserIdAndAchievementCode(userId.toString(), "DUPLICATE_CODE")).thenReturn(true);

        assertThrows(AchievementAlreadyExistsException.class, () -> service.createAchievement(userDetails, request));
    }

    @Test
    void updateProgress_Success() {
        UUID achievementId = UUID.randomUUID();
        AchievementProgress existing = AchievementProgress.builder()
                .id(achievementId)
                .userId(userId.toString())
                .achievementCode("CODE_200")
                .title("Read Books")
                .category(AchievementCategory.PERSONAL)
                .status(AchievementStatus.IN_PROGRESS)
                .progress(30)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(achievementProgressRepository.findById(achievementId)).thenReturn(Optional.of(existing));
        when(achievementProgressRepository.save(any(AchievementProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateAchievementProgressRequest request = UpdateAchievementProgressRequest.builder()
                .progress(80)
                .build();

        AchievementProgressResponse response = service.updateProgress(userDetails, achievementId, request);

        assertEquals(80, response.getProgress());
        assertEquals(AchievementStatus.IN_PROGRESS, response.getStatus());
    }

    @Test
    void updateProgress_To100_TransitionsToEarnedAndSetsEarnedAt() {
        UUID achievementId = UUID.randomUUID();
        AchievementProgress existing = AchievementProgress.builder()
                .id(achievementId)
                .userId(userId.toString())
                .achievementCode("CODE_300")
                .title("Complete Course")
                .category(AchievementCategory.ACADEMIC)
                .status(AchievementStatus.IN_PROGRESS)
                .progress(90)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(achievementProgressRepository.findById(achievementId)).thenReturn(Optional.of(existing));
        when(achievementProgressRepository.save(any(AchievementProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateAchievementProgressRequest request = UpdateAchievementProgressRequest.builder()
                .progress(100)
                .build();

        AchievementProgressResponse response = service.updateProgress(userDetails, achievementId, request);

        assertEquals(100, response.getProgress());
        assertEquals(AchievementStatus.EARNED, response.getStatus());
        assertNotNull(response.getEarnedAt());
    }

    @Test
    void updateProgress_EarnedToLocked_ThrowsValidationException() {
        UUID achievementId = UUID.randomUUID();
        AchievementProgress existing = AchievementProgress.builder()
                .id(achievementId)
                .userId(userId.toString())
                .achievementCode("CODE_400")
                .title("Completed Task")
                .category(AchievementCategory.ACADEMIC)
                .status(AchievementStatus.EARNED)
                .progress(100)
                .earnedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(achievementProgressRepository.findById(achievementId)).thenReturn(Optional.of(existing));

        UpdateAchievementProgressRequest request = UpdateAchievementProgressRequest.builder()
                .progress(50)
                .build();

        assertThrows(AchievementValidationException.class, () -> service.updateProgress(userDetails, achievementId, request));
    }

    @Test
    void getAchievementById_OtherUser_ThrowsAccessDenied() {
        UUID achievementId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        AchievementProgress otherAchievement = AchievementProgress.builder()
                .id(achievementId)
                .userId(otherUserId.toString())
                .achievementCode("OTHER_CODE")
                .build();

        when(achievementProgressRepository.findById(achievementId)).thenReturn(Optional.of(otherAchievement));

        assertThrows(AchievementAccessDeniedException.class, () -> service.getAchievementById(userDetails, achievementId));
    }

    @Test
    void getAchievementById_NotFound_ThrowsNotFoundException() {
        UUID achievementId = UUID.randomUUID();
        when(achievementProgressRepository.findById(achievementId)).thenReturn(Optional.empty());

        assertThrows(AchievementNotFoundException.class, () -> service.getAchievementById(userDetails, achievementId));
    }

    @Test
    void deleteAchievement_Success() {
        UUID achievementId = UUID.randomUUID();
        AchievementProgress existing = AchievementProgress.builder()
                .id(achievementId)
                .userId(userId.toString())
                .achievementCode("CODE_500")
                .build();

        when(achievementProgressRepository.findById(achievementId)).thenReturn(Optional.of(existing));

        service.deleteAchievement(userDetails, achievementId);

        verify(achievementProgressRepository, times(1)).delete(existing);
    }
}
