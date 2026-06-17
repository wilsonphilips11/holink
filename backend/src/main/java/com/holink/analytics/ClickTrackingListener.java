package com.holink.analytics;

import com.holink.entity.ClickEvent;
import com.holink.entity.Link;
import com.holink.repository.ClickEventRepository;
import com.holink.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ClickTrackingListener {

    private static final Logger log = LoggerFactory.getLogger(ClickTrackingListener.class);

    private final ClickEventRepository clickEventRepository;
    private final LinkRepository linkRepository;

    /**
     * Async click tracking: redirect is returned before this runs.
     * Failures are logged but never block the user redirect.
     */
    @Async
    @EventListener
    @Transactional
    public void handleClickTracking(ClickTrackingEvent event) {
        try {
            Link link = linkRepository.findById(event.getLinkId())
                    .orElseThrow(() -> new IllegalStateException("Link not found for tracking"));

            ClickEvent clickEvent = ClickEvent.builder()
                    .link(link)
                    .profileUsername(event.getProfileUsername())
                    .utmSource(event.getUtmSource())
                    .utmMedium(event.getUtmMedium())
                    .utmCampaign(event.getUtmCampaign())
                    .referrer(event.getReferrer())
                    .userAgent(event.getUserAgent())
                    .ipHash(event.getIpHash())
                    .build();

            clickEventRepository.save(clickEvent);
        } catch (Exception ex) {
            log.error("Failed to record click for link {}: {}", event.getLinkId(), ex.getMessage(), ex);
        }
    }
}
