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
public class LinkResponse {

    private UUID id;
    private String title;
    private String url;
    private boolean active;
    private int position;
    private Instant createdAt;
    private Instant updatedAt;
}
