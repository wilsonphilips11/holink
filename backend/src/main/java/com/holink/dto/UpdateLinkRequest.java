package com.holink.dto;

import com.holink.validation.NoHtmlContent;
import com.holink.validation.SafeUrl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateLinkRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be at most 200 characters")
    @NoHtmlContent(message = "Title contains invalid content")
    private String title;

    @NotBlank(message = "URL is required")
    @SafeUrl(message = "Invalid URL. Only http:// and https:// URLs are allowed")
    private String url;

    private boolean active = true;
}
