package com.holink.service;

import com.holink.dto.DailyClickItem;
import com.holink.dto.LinkAnalyticsItem;
import com.holink.dto.LinkAnalyticsResponse;
import com.holink.dto.ProfileAnalyticsResponse;
import com.holink.entity.Profile;
import com.holink.exception.ResourceNotFoundException;
import com.holink.repository.ClickEventRepository;
import com.holink.repository.LinkRepository;
import com.holink.repository.ProfileRepository;
import com.holink.repository.ProfileViewEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ProfileRepository profileRepository;
    private final LinkRepository linkRepository;
    private final ClickEventRepository clickEventRepository;
    private final ProfileViewEventRepository profileViewEventRepository;

    @Transactional(readOnly = true)
    public LinkAnalyticsResponse getLinkAnalytics(UUID userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        var clickCounts = clickEventRepository.countClicksByLink(profile.getId());
        long totalClicks = clickEventRepository.countTotalClicks(profile.getId());
        long totalProfileViews = profileViewEventRepository.countByProfileId(profile.getId());

        List<LinkAnalyticsItem> items = clickCounts.stream()
                .map(c -> LinkAnalyticsItem.builder()
                        .linkId(c.linkId())
                        .title(c.title())
                        .clicks(c.clicks())
                        .build())
                .toList();

        LinkAnalyticsItem topLink = items.isEmpty() ? null : items.get(0);

        Instant since = Instant.now().minus(30, ChronoUnit.DAYS);
        List<DailyClickItem> clicksByDay = clickEventRepository.countClicksByDay(profile.getId(), since)
                .stream()
                .map(d -> DailyClickItem.builder().date(d.date()).clicks(d.clicks()).build())
                .toList();

        return LinkAnalyticsResponse.builder()
                .totalClicks(totalClicks)
                .totalProfileViews(totalProfileViews)
                .links(items)
                .topLink(topLink)
                .clicksByDay(clicksByDay)
                .build();
    }

    @Transactional(readOnly = true)
    public ProfileAnalyticsResponse getProfileAnalytics(UUID userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        var allLinks = linkRepository.findActiveByProfileId(profile.getId());
        long activeLinks = allLinks.stream().filter(l -> l.isActive()).count();
        long totalClicks = clickEventRepository.countTotalClicks(profile.getId());
        long totalProfileViews = profileViewEventRepository.countByProfileId(profile.getId());

        return ProfileAnalyticsResponse.builder()
                .username(profile.getUsername())
                .displayName(profile.getDisplayName())
                .totalClicks(totalClicks)
                .totalProfileViews(totalProfileViews)
                .totalLinks(allLinks.size())
                .activeLinks(activeLinks)
                .build();
    }
}
