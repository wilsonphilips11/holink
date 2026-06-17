package com.holink.analytics;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class ClickTrackingEvent {

    UUID linkId;
    String profileUsername;
    String utmSource;
    String utmMedium;
    String utmCampaign;
    String referrer;
    String userAgent;
    String ipHash;
}
