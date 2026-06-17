package com.holink.service;

import com.holink.dto.CreateProfileRequest;
import com.holink.dto.UpdateProfileRequest;
import com.holink.entity.Profile;
import com.holink.entity.User;
import com.holink.exception.ConflictException;
import com.holink.exception.ForbiddenException;
import com.holink.exception.ResourceNotFoundException;
import com.holink.mapper.LinkMapper;
import com.holink.mapper.ProfileMapper;
import com.holink.repository.LinkRepository;
import com.holink.repository.ProfileRepository;
import com.holink.repository.UserRepository;
import com.holink.validation.util.IpHashUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LinkRepository linkRepository;
    @Mock
    private ProfileMapper profileMapper;
    @Mock
    private LinkMapper linkMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private IpHashUtil ipHashUtil;

    @InjectMocks
    private ProfileService profileService;

    private UUID userId;
    private User user;
    private Profile profile;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder().id(userId).email("test@example.com").passwordHash("hash").build();
        profile = Profile.builder()
                .id(UUID.randomUUID())
                .user(user)
                .username("johndoe")
                .displayName("John Doe")
                .build();
    }

    @Test
    void createProfile_normalizesUsername() {
        CreateProfileRequest request = new CreateProfileRequest();
        request.setUsername("John Doe");
        request.setDisplayName("John Doe");

        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(profileRepository.existsByUsername("johndoe")).thenReturn(false);
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));
        when(profileMapper.toResponse(any(Profile.class))).thenReturn(
                com.holink.dto.ProfileResponse.builder().username("johndoe").displayName("John Doe").build()
        );

        var response = profileService.createProfile(userId, request);

        assertEquals("johndoe", response.getUsername());
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void createProfile_duplicateUsername_throwsConflict() {
        CreateProfileRequest request = new CreateProfileRequest();
        request.setUsername("johndoe");
        request.setDisplayName("John Doe");

        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(profileRepository.existsByUsername("johndoe")).thenReturn(true);

        assertThrows(ConflictException.class, () -> profileService.createProfile(userId, request));
    }

    @Test
    void updateProfile_notOwner_throwsForbidden() {
        UUID otherUserId = UUID.randomUUID();
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("johndoe");
        request.setDisplayName("John Doe");

        when(profileRepository.findById(profile.getId())).thenReturn(Optional.of(profile));

        assertThrows(ForbiddenException.class,
                () -> profileService.updateProfile(otherUserId, profile.getId(), request));
    }

    @Test
    void getPublicProfile_notFound() {
        when(profileRepository.findByUsername("missing")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> profileService.getPublicProfile("missing"));
    }
}
