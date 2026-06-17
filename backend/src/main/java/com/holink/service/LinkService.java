package com.holink.service;

import com.holink.analytics.ClickTrackingEvent;
import com.holink.dto.ClickResponse;
import com.holink.dto.CreateLinkRequest;
import com.holink.dto.LinkResponse;
import com.holink.dto.ReorderLinksRequest;
import com.holink.dto.UpdateLinkRequest;
import com.holink.entity.Link;
import com.holink.entity.Profile;
import com.holink.exception.ForbiddenException;
import com.holink.exception.ResourceNotFoundException;
import com.holink.mapper.LinkMapper;
import com.holink.repository.LinkRepository;
import com.holink.repository.ProfileRepository;
import com.holink.validation.util.IpHashUtil;
import com.holink.validation.util.SanitizationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;
    private final ProfileRepository profileRepository;
    private final ProfileService profileService;
    private final LinkMapper linkMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final IpHashUtil ipHashUtil;

    @Transactional
    public LinkResponse createLink(UUID userId, CreateLinkRequest request) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found. Create a profile first."));

        int position = request.getPosition() != null
                ? request.getPosition()
                : linkRepository.findMaxPosition(profile.getId()) + 1;

        Link link = Link.builder()
                .profile(profile)
                .title(SanitizationUtil.sanitizeText(request.getTitle()))
                .url(request.getUrl().trim())
                .active(request.isActive())
                .position(position)
                .build();

        link = linkRepository.save(link);
        return linkMapper.toResponse(link);
    }

    @Transactional
    public LinkResponse updateLink(UUID userId, UUID linkId, UpdateLinkRequest request) {
        Link link = linkRepository.findActiveById(linkId)
                .orElseThrow(() -> new ResourceNotFoundException("Link not found"));

        profileService.assertOwnership(userId, link.getProfile());

        link.setTitle(SanitizationUtil.sanitizeText(request.getTitle()));
        link.setUrl(request.getUrl().trim());
        link.setActive(request.isActive());

        link = linkRepository.save(link);
        return linkMapper.toResponse(link);
    }

    @Transactional
    public void deleteLink(UUID userId, UUID linkId) {
        Link link = linkRepository.findActiveById(linkId)
                .orElseThrow(() -> new ResourceNotFoundException("Link not found"));

        profileService.assertOwnership(userId, link.getProfile());

        link.setDeletedAt(Instant.now());
        linkRepository.save(link);
    }

    @Transactional
    public List<LinkResponse> reorderLinks(UUID userId, ReorderLinksRequest request) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        List<Link> links = linkRepository.findByIdsAndProfileId(request.getLinkIds(), profile.getId());

        if (links.size() != request.getLinkIds().size()) {
            throw new ForbiddenException("One or more links do not belong to your profile");
        }

        Map<UUID, Link> linkMap = new HashMap<>();
        for (Link link : links) {
            linkMap.put(link.getId(), link);
        }

        for (int i = 0; i < request.getLinkIds().size(); i++) {
            Link link = linkMap.get(request.getLinkIds().get(i));
            if (link == null) {
                throw new ForbiddenException("Invalid link ID in reorder request");
            }
            link.setPosition(i);
        }

        linkRepository.saveAll(links);
        return linkMapper.toResponseList(linkRepository.findActiveByProfileId(profile.getId()));
    }

    public List<LinkResponse> getLinksForUser(UUID userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        return linkMapper.toResponseList(linkRepository.findActiveByProfileId(profile.getId()));
    }

    /**
     * Returns redirect URL immediately; click tracking is published asynchronously.
     */
    @Transactional(readOnly = true)
    public ClickResponse trackClick(UUID linkId, String referrer, String userAgent,
                                    String utmSource, String utmMedium, String utmCampaign, String clientIp) {
        Link link = linkRepository.findActiveById(linkId)
                .orElseThrow(() -> new ResourceNotFoundException("Link not found"));

        if (!link.isActive()) {
            throw new ResourceNotFoundException("Link not found");
        }

        String redirectUrl = link.getUrl();

        eventPublisher.publishEvent(ClickTrackingEvent.builder()
                .linkId(link.getId())
                .profileUsername(link.getProfile().getUsername())
                .utmSource(utmSource)
                .utmMedium(utmMedium)
                .utmCampaign(utmCampaign)
                .referrer(referrer)
                .userAgent(userAgent)
                .ipHash(ipHashUtil.hashIp(clientIp))
                .build());

        return ClickResponse.builder().redirectUrl(redirectUrl).build();
    }
}
