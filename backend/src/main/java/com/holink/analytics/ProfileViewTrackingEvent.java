package com.holink.analytics;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class ProfileViewTrackingEvent {

    UUID profileId;
    String referrer;
    String userAgent;
    String ipHash;
}
