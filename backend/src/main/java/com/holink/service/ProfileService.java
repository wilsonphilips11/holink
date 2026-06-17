package com.holink.service;

import com.holink.analytics.ProfileViewTrackingEvent;
import com.holink.dto.CreateProfileRequest;
import com.holink.dto.ProfileResponse;
import com.holink.dto.PublicProfileResponse;
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
import com.holink.validation.util.SanitizationUtil;
import com.holink.validation.util.UsernameUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final LinkRepository linkRepository;
    private final ProfileMapper profileMapper;
    private final LinkMapper linkMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final IpHashUtil ipHashUtil;

    @Transactional
    public ProfileResponse createProfile(UUID userId, CreateProfileRequest request) {
        if (profileRepository.findByUserId(userId).isPresent()) {
            throw new ConflictException("User already has a profile");
        }

        UsernameUtil.validateRawInput(request.getUsername());
        String normalizedUsername = UsernameUtil.normalize(request.getUsername());

        if (!UsernameUtil.isValidNormalized(normalizedUsername)) {
            throw new IllegalArgumentException("Invalid username format");
        }

        if (profileRepository.existsByUsername(normalizedUsername)) {
            throw new ConflictException("Username already exists");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Profile profile = Profile.builder()
                .user(user)
                .username(normalizedUsername)
                .displayName(SanitizationUtil.sanitizeText(request.getDisplayName()))
                .bio(SanitizationUtil.sanitizeText(request.getBio()))
                .avatarUrl(normalizeOptionalUrl(request.getAvatarUrl()))
                .build();

        profile = profileRepository.save(profile);
        return profileMapper.toResponse(profile);
    }

    @Transactional
    public ProfileResponse updateProfile(UUID userId, UUID profileId, UpdateProfileRequest request) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        assertOwnership(userId, profile);

        UsernameUtil.validateRawInput(request.getUsername());
        String normalizedUsername = UsernameUtil.normalize(request.getUsername());

        if (!UsernameUtil.isValidNormalized(normalizedUsername)) {
            throw new IllegalArgumentException("Invalid username format");
        }

        if (!profile.getUsername().equals(normalizedUsername)
                && profileRepository.existsByUsername(normalizedUsername)) {
            throw new ConflictException("Username already exists");
        }

        profile.setUsername(normalizedUsername);
        profile.setDisplayName(SanitizationUtil.sanitizeText(request.getDisplayName()));
        profile.setBio(SanitizationUtil.sanitizeText(request.getBio()));
        profile.setAvatarUrl(normalizeOptionalUrl(request.getAvatarUrl()));

        profile = profileRepository.save(profile);
        return profileMapper.toResponse(profile);
    }

    public ProfileResponse getProfileByUserId(UUID userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        return profileMapper.toResponse(profile);
    }

    public PublicProfileResponse getPublicProfile(String username) {
        return getPublicProfile(username, null, null, null);
    }

    public PublicProfileResponse getPublicProfile(String username, String referrer, String userAgent, String clientIp) {
        String normalized = UsernameUtil.normalize(username);
        Profile profile = profileRepository.findByUsername(normalized)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        var links = linkRepository.findPublicByProfileId(profile.getId());

        if (clientIp != null && !clientIp.isBlank()) {
            eventPublisher.publishEvent(ProfileViewTrackingEvent.builder()
                    .profileId(profile.getId())
                    .referrer(referrer)
                    .userAgent(userAgent)
                    .ipHash(ipHashUtil.hashIp(clientIp))
                    .build());
        }

        return PublicProfileResponse.builder()
                .profile(profileMapper.toResponse(profile))
                .links(linkMapper.toResponseList(links))
                .build();
    }

    public void assertOwnership(UUID userId, Profile profile) {
        if (!profile.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You do not have permission to modify this profile");
        }
    }

    private String normalizeOptionalUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        return url.trim();
    }
}
