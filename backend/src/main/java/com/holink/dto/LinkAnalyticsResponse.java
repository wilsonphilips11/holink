package com.holink.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkAnalyticsResponse {

    private long totalClicks;
    private long totalProfileViews;
    private List<LinkAnalyticsItem> links;
    private LinkAnalyticsItem topLink;
    private List<DailyClickItem> clicksByDay;
}
