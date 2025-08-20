package com.pr.review_assistant.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pr.review_assistant.database.ReviewJob;
import com.pr.review_assistant.jobs.JobQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/webhook/github")
public class GithubWebhookController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String PULL_REQUEST_EVENT = "pull_request";

    private final JobQueue jobQueue;

    public GithubWebhookController(JobQueue jobQueue) {
        this.jobQueue = jobQueue;
    }

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestHeader("X-GitHub-Event") String event,
            @RequestBody String payload) throws Exception {

        log.info("Received event: " + event);

        // Only care about pull_request events for now
        if (!PULL_REQUEST_EVENT.equals(event)) {
            return ResponseEntity.ok("Ignored non-PR event");
        }

        JsonNode json = objectMapper.readTree(payload);
        String action = json.get("action").asText();

        // Only react when PR is opened or updated
        if (!(action.equals("opened") || action.equals("synchronize"))) {
            return ResponseEntity.ok("Ignored action: " + action);
        }

        String repo = json.get("repository").get("full_name").asText();
        int prNumber = json.get("number").asInt();
        String headSha = json.get(PULL_REQUEST_EVENT).get("head").get("sha").asText();
        String baseSha = json.get(PULL_REQUEST_EVENT).get("base").get("sha").asText();

        log.info("Repo: {}", repo);
        log.info("PR Number: {}", prNumber);
        log.info("Base SHA: {}", baseSha);
        log.info("Head SHA: {}", headSha);

        // Enqueue a job for later analysis
        ReviewJob job = new ReviewJob(repo, headSha, prNumber, baseSha);
        jobQueue.enqueue(job);

        log.info(" Job enqueued for PR #{} on {}", prNumber, repo);

        return ResponseEntity.ok("PR event handled successfully");
    }
}
