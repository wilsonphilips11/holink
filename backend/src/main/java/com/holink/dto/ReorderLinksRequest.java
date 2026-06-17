package com.holink.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ReorderLinksRequest {

    @NotEmpty(message = "Link IDs are required")
    private List<UUID> linkIds;
}
