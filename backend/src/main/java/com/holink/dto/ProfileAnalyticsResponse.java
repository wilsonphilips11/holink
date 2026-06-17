package com.holink.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileAnalyticsResponse {

    private String username;
    private String displayName;
    private long totalClicks;
    private long totalProfileViews;
    private long totalLinks;
    private long activeLinks;
}
