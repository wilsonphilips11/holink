package com.holink.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {

    private UUID id;
    private String username;
    private String displayName;
    private String bio;
    private String avatarUrl;
    private Instant createdAt;
    private Instant updatedAt;
}
