package com.pr.review_assistant.analysis;

import com.pr.review_assistant.database.ReviewJob;
import com.pr.review_assistant.publisher.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Component
public class AnalysisRunner {
    
    private final Publisher publisher;
    
    public AnalysisRunner(Publisher publisher) {
        this.publisher = publisher;
    }
    
    public void run(ReviewJob job) {
        log.info("Starting analysis for PR #{} on {}", job.getPrNumber(), job.getRepo());
        
        try {
            ProcessBuilder pb = new ProcessBuilder("bash", "scripts/run-analysis.sh");
            pb.environment().put("REPO", job.getRepo());
            pb.environment().put("HEAD_SHA", job.getHeadSha());
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                log.error("Analysis script failed with exit code: {}", exitCode);
                return;
            }

            // Parse results into findings
            List<String> findings = Files.readAllLines(Paths.get("artifacts/checkstyle-result.xml"));
            log.info("Found {} findings for PR #{}", findings.size(), job.getPrNumber());
            
            publisher.postResult(job, findings);

        } catch (Exception e) {
            log.error("Analysis failed for PR #{} on {}: {}", 
                     job.getPrNumber(), job.getRepo(), e.getMessage(), e);
        }
    }
}
