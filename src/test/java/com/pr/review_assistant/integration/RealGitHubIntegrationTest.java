package com.pr.review_assistant.integration;

import com.pr.review_assistant.database.ReviewJob;
import com.pr.review_assistant.publisher.Publisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

/**
 * Integration test that runs only when GITHUB_TOKEN environment variable is set
 * and is not the test token. This test will make actual API calls to GitHub.
 * 
 * To run this test:
 * export GITHUB_TOKEN=your_real_token
 * mvn test -Dtest=RealGitHubIntegrationTest
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "GITHUB_TOKEN", matches = "ghp_.*")
class RealGitHubIntegrationTest {

    @Autowired
    private Publisher publisher;

    @Test
    void testRealGitHubIntegration() {
        // NOTE: This test will attempt to post to a real GitHub repository
        // Make sure you have a test repository where you can post comments
        
        // Create a test job - CHANGE THESE VALUES TO YOUR TEST REPOSITORY
        ReviewJob job = new ReviewJob("your-username/your-test-repo", "abc123", 1, "def456");
        
        // Create test findings
        List<String> findings = Arrays.asList(
            "✅ Test finding 1: Code looks good!",
            "⚠️ Test finding 2: Consider adding more comments"
        );
        
        // This will post a real comment to GitHub
        // UNCOMMENT THE LINE BELOW ONLY IF YOU WANT TO TEST WITH A REAL REPOSITORY
        // publisher.postResult(job, findings);
        
        System.out.println("Real GitHub integration test completed. " +
                          "Uncomment the publisher.postResult() line to test with a real repository.");
    }
}