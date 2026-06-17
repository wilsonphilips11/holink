package com.holink.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.holink.dto.CreateLinkRequest;
import com.holink.dto.CreateProfileRequest;
import com.holink.dto.LoginRequest;
import com.holink.dto.RegisterRequest;
import com.holink.repository.ClickEventRepository;
import com.holink.repository.LinkRepository;
import com.holink.repository.ProfileRepository;
import com.holink.repository.ProfileViewEventRepository;
import com.holink.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private ClickEventRepository clickEventRepository;

    @Autowired
    private ProfileViewEventRepository profileViewEventRepository;

    @BeforeEach
    void cleanUp() {
        profileViewEventRepository.deleteAll();
        clickEventRepository.deleteAll();
        linkRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void fullFlow_createProfile_createLink_deleteLink_analytics() throws Exception {
        String token = registerAndLogin("creator@example.com", "password123");

        CreateProfileRequest profileRequest = new CreateProfileRequest();
        profileRequest.setUsername("creator");
        profileRequest.setDisplayName("Creator Name");
        profileRequest.setBio("Hello world");

        MvcResult profileResult = mockMvc.perform(post("/api/profiles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("creator"))
                .andReturn();

        CreateLinkRequest linkRequest = new CreateLinkRequest();
        linkRequest.setTitle("Website");
        linkRequest.setUrl("https://example.com");
        linkRequest.setActive(true);

        MvcResult linkResult = mockMvc.perform(post("/api/links")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(linkRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Website"))
                .andReturn();

        String linkId = objectMapper.readTree(linkResult.getResponse().getContentAsString())
                .path("data").path("id").asText();

        mockMvc.perform(post("/api/links/" + linkId + "/click")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.redirectUrl").value("https://example.com"));

        Thread.sleep(500);

        mockMvc.perform(get("/api/analytics/links")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalClicks").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.totalProfileViews").value(0));

        mockMvc.perform(delete("/api/links/" + linkId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/public/creator"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.links").isEmpty());

        Thread.sleep(500);

        mockMvc.perform(get("/api/analytics/links")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalProfileViews").value(greaterThanOrEqualTo(1)));
    }

    @Test
    void createProfile_duplicateUsername_returns409() throws Exception {
        String token1 = registerAndLogin("user1@example.com", "password123");
        String token2 = registerAndLogin("user2@example.com", "password123");

        CreateProfileRequest request = new CreateProfileRequest();
        request.setUsername("taken");
        request.setDisplayName("User One");

        mockMvc.perform(post("/api/profiles")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        request.setDisplayName("User Two");
        mockMvc.perform(post("/api/profiles")
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    private String registerAndLogin(String email, String password) throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();
    }
}
