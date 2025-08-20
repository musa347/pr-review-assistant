package com.pr.review_assistant.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class GitHubIntegrationTest {

    @Value("${github.token}")
    private String githubToken;

    @Value("${github.api.url}")
    private String githubApiUrl;

    @Test
    void testGitHubTokenConfiguration() {
        assertNotNull(githubToken, "GitHub token should be configured");
        assertNotNull(githubApiUrl, "GitHub API URL should be configured");
        assertEquals("https://api.github.com", githubApiUrl);
    }

    @Test
    void testGitHubTokenValidation() {
        // This test validates the token format without making API calls
        assertTrue(githubToken.startsWith("ghp_") || githubToken.equals("test-token"), 
                  "GitHub token should start with 'ghp_' or be test token");
        
        if (githubToken.equals("test-token")) {
            assertEquals(10, githubToken.length(), "Test token should be exactly 10 characters");
        } else {
            assertTrue(githubToken.length() > 30, "Real GitHub token should be longer than 30 characters");
        }
    }

    @Test
    void testGitHubAPIConnectivity() {
        // Test basic connectivity to GitHub API (this endpoint doesn't require auth)
        RestTemplate restTemplate = new RestTemplate();
        
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(githubApiUrl, String.class);
            assertTrue(response.getStatusCode().is2xxSuccessful() || 
                      response.getStatusCode() == HttpStatus.NOT_FOUND,
                      "Should be able to connect to GitHub API");
        } catch (Exception e) {
            fail("Should be able to connect to GitHub API: " + e.getMessage());
        }
    }
}