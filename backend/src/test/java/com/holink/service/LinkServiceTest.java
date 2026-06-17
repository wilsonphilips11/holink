package com.holink.service;

import com.holink.analytics.ClickTrackingEvent;
import com.holink.dto.CreateLinkRequest;
import com.holink.dto.UpdateLinkRequest;
import com.holink.entity.Link;
import com.holink.entity.Profile;
import com.holink.entity.User;
import com.holink.exception.ForbiddenException;
import com.holink.exception.ResourceNotFoundException;
import com.holink.mapper.LinkMapper;
import com.holink.repository.LinkRepository;
import com.holink.repository.ProfileRepository;
import com.holink.validation.util.IpHashUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LinkServiceTest {

    @Mock
    private LinkRepository linkRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private ProfileService profileService;
    @Mock
    private LinkMapper linkMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private IpHashUtil ipHashUtil;

    @InjectMocks
    private LinkService linkService;

    private UUID userId;
    private Profile profile;
    private Link link;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        User user = User.builder().id(userId).email("test@example.com").passwordHash("hash").build();
        profile = Profile.builder()
                .id(UUID.randomUUID())
                .user(user)
                .username("johndoe")
                .displayName("John Doe")
                .build();
        link = Link.builder()
                .id(UUID.randomUUID())
                .profile(profile)
                .title("My Link")
                .url("https://example.com")
                .active(true)
                .position(0)
                .build();
    }

    @Test
    void createLink_success() {
        CreateLinkRequest request = new CreateLinkRequest();
        request.setTitle("My Link");
        request.setUrl("https://example.com");

        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(linkRepository.findMaxPosition(profile.getId())).thenReturn(0);
        when(linkRepository.save(any(Link.class))).thenReturn(link);
        when(linkMapper.toResponse(link)).thenReturn(
                com.holink.dto.LinkResponse.builder().title("My Link").url("https://example.com").build()
        );

        var response = linkService.createLink(userId, request);

        assertEquals("My Link", response.getTitle());
        verify(linkRepository).save(any(Link.class));
    }

    @Test
    void updateLink_notOwner_throwsForbidden() {
        UUID otherUserId = UUID.randomUUID();
        UpdateLinkRequest request = new UpdateLinkRequest();
        request.setTitle("Updated");
        request.setUrl("https://example.com");

        when(linkRepository.findActiveById(link.getId())).thenReturn(Optional.of(link));
        org.mockito.Mockito.doThrow(new ForbiddenException("Forbidden"))
                .when(profileService).assertOwnership(otherUserId, profile);

        assertThrows(ForbiddenException.class,
                () -> linkService.updateLink(otherUserId, link.getId(), request));
    }

    @Test
    void trackClick_returnsRedirectUrl() {
        when(linkRepository.findActiveById(link.getId())).thenReturn(Optional.of(link));
        when(ipHashUtil.hashIp("127.0.0.1")).thenReturn("hashed-ip");

        var response = linkService.trackClick(
                link.getId(), "https://referrer.com", "Mozilla", null, null, null, "127.0.0.1");

        assertEquals("https://example.com", response.getRedirectUrl());
        verify(eventPublisher).publishEvent(any(ClickTrackingEvent.class));
    }

    @Test
    void trackClick_linkNotFound() {
        when(linkRepository.findActiveById(link.getId())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> linkService.trackClick(link.getId(), null, null, null, null, null, "127.0.0.1"));
    }
}
