package com.holink.controller;

import com.holink.config.ClickRateLimiter;
import com.holink.dto.ApiResponse;
import com.holink.dto.ClickResponse;
import com.holink.dto.ClickTrackingRequest;
import com.holink.dto.CreateLinkRequest;
import com.holink.dto.LinkResponse;
import com.holink.dto.ReorderLinksRequest;
import com.holink.dto.UpdateLinkRequest;
import com.holink.security.SecurityUtils;
import com.holink.service.LinkService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/links")
@RequiredArgsConstructor
public class LinkController {

    private final LinkService linkService;
    private final SecurityUtils securityUtils;
    private final ClickRateLimiter clickRateLimiter;

    @PostMapping
    public ResponseEntity<ApiResponse<LinkResponse>> createLink(
            @Valid @RequestBody CreateLinkRequest request) {
        UUID userId = securityUtils.getCurrentUserId();
        LinkResponse response = linkService.createLink(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LinkResponse>> updateLink(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLinkRequest request) {
        UUID userId = securityUtils.getCurrentUserId();
        LinkResponse response = linkService.updateLink(userId, id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLink(@PathVariable UUID id) {
        UUID userId = securityUtils.getCurrentUserId();
        linkService.deleteLink(userId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PutMapping("/reorder")
    public ResponseEntity<ApiResponse<List<LinkResponse>>> reorderLinks(
            @Valid @RequestBody ReorderLinksRequest request) {
        UUID userId = securityUtils.getCurrentUserId();
        List<LinkResponse> links = linkService.reorderLinks(userId, request);
        return ResponseEntity.ok(ApiResponse.ok(links));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LinkResponse>>> getLinks() {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(linkService.getLinksForUser(userId)));
    }

    @PostMapping("/{id}/click")
    public ResponseEntity<ApiResponse<ClickResponse>> trackClick(
            @PathVariable UUID id,
            @RequestBody(required = false) ClickTrackingRequest trackingRequest,
            HttpServletRequest request) {
        String clientIp = resolveClientIp(request);
        clickRateLimiter.checkRateLimit(clientIp);

        String referrer = request.getHeader("Referer");
        String userAgent = request.getHeader("User-Agent");

        String utmSource = trackingRequest != null ? trackingRequest.getUtmSource() : null;
        String utmMedium = trackingRequest != null ? trackingRequest.getUtmMedium() : null;
        String utmCampaign = trackingRequest != null ? trackingRequest.getUtmCampaign() : null;

        ClickResponse response = linkService.trackClick(
                id, referrer, userAgent, utmSource, utmMedium, utmCampaign, clientIp);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
