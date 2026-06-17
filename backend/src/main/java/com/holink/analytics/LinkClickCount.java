package com.holink.analytics;

import java.util.UUID;

public record LinkClickCount(UUID linkId, String title, long clicks) {
}
