package com.holink.controller;

import com.holink.dto.ApiResponse;
import com.holink.dto.CreateProfileRequest;
import com.holink.dto.ProfileResponse;
import com.holink.dto.PublicProfileResponse;
import com.holink.dto.UpdateProfileRequest;
import com.holink.security.SecurityUtils;
import com.holink.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final SecurityUtils securityUtils;

    @PostMapping("/api/profiles")
    public ResponseEntity<ApiResponse<ProfileResponse>> createProfile(
            @Valid @RequestBody CreateProfileRequest request) {
        UUID userId = securityUtils.getCurrentUserId();
        ProfileResponse response = profileService.createProfile(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PutMapping("/api/profiles/{id}")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProfileRequest request) {
        UUID userId = securityUtils.getCurrentUserId();
        ProfileResponse response = profileService.updateProfile(userId, id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/api/profiles/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> getMyProfile() {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(profileService.getProfileByUserId(userId)));
    }

    @GetMapping("/api/public/{username}")
    public ResponseEntity<ApiResponse<PublicProfileResponse>> getPublicProfile(
            @PathVariable String username,
            HttpServletRequest request) {
        String referrer = request.getHeader("Referer");
        String userAgent = request.getHeader("User-Agent");
        String clientIp = resolveClientIp(request);
        return ResponseEntity.ok(ApiResponse.ok(profileService.getPublicProfile(username, referrer, userAgent, clientIp)));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
