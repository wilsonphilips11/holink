package com.holink.analytics;

import com.holink.entity.Profile;
import com.holink.entity.ProfileViewEvent;
import com.holink.repository.ProfileRepository;
import com.holink.repository.ProfileViewEventRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProfileViewTrackingListener {

    private static final Logger log = LoggerFactory.getLogger(ProfileViewTrackingListener.class);

    private final ProfileViewEventRepository profileViewEventRepository;
    private final ProfileRepository profileRepository;

    @Async
    @EventListener
    @Transactional
    public void handleProfileViewTracking(ProfileViewTrackingEvent event) {
        try {
            Profile profile = profileRepository.findById(event.getProfileId())
                    .orElseThrow(() -> new IllegalStateException("Profile not found for view tracking"));

            ProfileViewEvent viewEvent = ProfileViewEvent.builder()
                    .profile(profile)
                    .referrer(event.getReferrer())
                    .userAgent(event.getUserAgent())
                    .ipHash(event.getIpHash())
                    .build();

            profileViewEventRepository.save(viewEvent);
        } catch (Exception ex) {
            log.error("Failed to record profile view for profile {}: {}", event.getProfileId(), ex.getMessage(), ex);
        }
    }
}
