package com.holink.controller;

import com.holink.dto.ApiResponse;
import com.holink.dto.LinkAnalyticsResponse;
import com.holink.dto.ProfileAnalyticsResponse;
import com.holink.security.SecurityUtils;
import com.holink.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final SecurityUtils securityUtils;

    @GetMapping("/links")
    public ResponseEntity<ApiResponse<LinkAnalyticsResponse>> getLinkAnalytics() {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getLinkAnalytics(userId)));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileAnalyticsResponse>> getProfileAnalytics() {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getProfileAnalytics(userId)));
    }
}
