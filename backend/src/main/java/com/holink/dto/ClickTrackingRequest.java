package com.holink.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClickTrackingRequest {

    private String utmSource;
    private String utmMedium;
    private String utmCampaign;
}
