package com.holink.mapper;

import com.holink.dto.LinkResponse;
import com.holink.entity.Link;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LinkMapper {

    public LinkResponse toResponse(Link link) {
        return LinkResponse.builder()
                .id(link.getId())
                .title(link.getTitle())
                .url(link.getUrl())
                .active(link.isActive())
                .position(link.getPosition())
                .createdAt(link.getCreatedAt())
                .updatedAt(link.getUpdatedAt())
                .build();
    }

    public List<LinkResponse> toResponseList(List<Link> links) {
        return links.stream().map(this::toResponse).toList();
    }
}
