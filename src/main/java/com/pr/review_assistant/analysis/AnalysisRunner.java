package com.pr.review_assistant.analysis;

import com.pr.review_assistant.database.ReviewJob;
import com.pr.review_assistant.publisher.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AnalysisRunner {
    
    private final Publisher publisher;
    private final CheckstyleResultParser parser;
    private final RuleExplainer explainer;
    private final FindingFormatter formatter;
    
    @Value("${github.token}")
    private String githubToken;
    
    public AnalysisRunner(Publisher publisher, CheckstyleResultParser parser, 
                         RuleExplainer explainer, FindingFormatter formatter) {
        this.publisher = publisher;
        this.parser = parser;
        this.explainer = explainer;
        this.formatter = formatter;
    }
    
    public void run(ReviewJob job) {
        log.info("Starting analysis for PR #{} on {}", job.getPrNumber(), job.getRepo());
        
        try {
            ProcessBuilder pb = new ProcessBuilder("bash", "scripts/run-analysis.sh");
            pb.environment().put("REPO", job.getRepo());
            pb.environment().put("HEAD_SHA", job.getHeadSha());
            pb.environment().put("BASE_SHA", job.getBaseSha());
            pb.environment().put("GITHUB_TOKEN", githubToken);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // Capture output for debugging
            String output = new BufferedReader(new InputStreamReader(process.getInputStream()))
                    .lines().collect(Collectors.joining("\n"));
            
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                log.error("Analysis script failed with exit code: {}. Output: {}", exitCode, output);
                return;
            }
            
            log.debug("Analysis script output: {}", output);

            // Parse and enhance results
            List<Finding> findings = parser.parseCheckstyleResults("artifacts/checkstyle-result.xml");
            
            // Enhance findings with explanations and suggestions
            List<Finding> enhancedFindings = findings.stream()
                    .map(explainer::enhanceFinding)
                    .collect(Collectors.toList());
            
            log.info("Found {} findings for PR #{}", enhancedFindings.size(), job.getPrNumber());
            
            // Format and publish results
            String formattedResults = formatter.formatFindings(enhancedFindings);
            publisher.postResult(job, formattedResults);

        } catch (InterruptedException e) {
            log.error("Analysis interrupted for PR #{} on {}: {}", 
                     job.getPrNumber(), job.getRepo(), e.getMessage(), e);
            Thread.currentThread().interrupt(); // Preserve interrupted status
        } catch (Exception e) {
            log.error("Analysis failed for PR #{} on {}: {}", 
                     job.getPrNumber(), job.getRepo(), e.getMessage(), e);
        }
    }
}
