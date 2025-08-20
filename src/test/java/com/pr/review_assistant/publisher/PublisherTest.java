package com.pr.review_assistant.publisher;

import com.pr.review_assistant.database.ReviewJob;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class PublisherTest {

    @Autowired
    private Publisher publisher;

    @Test
    void testPublisherBeanCreation() {
        assertNotNull(publisher);
    }

    @Test
    void testFormatFindingsWithEmptyList() {
        // Test with empty findings - this won't make API calls
        ReviewJob job = new ReviewJob("test/repo", "abc123", 1, "def456");
        List<String> emptyFindings = Arrays.asList();
        
        // This should handle empty findings gracefully
        publisher.postResult(job, emptyFindings);
    }
}