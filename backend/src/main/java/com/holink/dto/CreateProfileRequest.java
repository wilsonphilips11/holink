package com.holink.dto;

import com.holink.validation.NoHtmlContent;
import com.holink.validation.SafeUrl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateProfileRequest {

    @NotBlank(message = "Username is required")
    @Size(max = 30, message = "Username must be at most 30 characters")
    @NoHtmlContent(message = "Username contains invalid content")
    private String username;

    @NotBlank(message = "Display name is required")
    @Size(max = 100, message = "Display name must be at most 100 characters")
    @NoHtmlContent(message = "Display name contains invalid content")
    private String displayName;

    @Size(max = 500, message = "Bio must be at most 500 characters")
    @NoHtmlContent(message = "Bio contains invalid content")
    private String bio;

    @Size(max = 2048, message = "Avatar URL must be at most 2048 characters")
    @SafeUrl(message = "Invalid avatar URL. Only http:// and https:// URLs are allowed")
    private String avatarUrl;
}
