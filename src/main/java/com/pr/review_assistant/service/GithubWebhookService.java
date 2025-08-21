package com.pr.review_assistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pr.review_assistant.database.ReviewJob;
import com.pr.review_assistant.jobs.JobQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GithubWebhookService {

    private static final String PULL_REQUEST_EVENT = "pull_request";
    private static final String ACTION_OPENED = "opened";
    private static final String ACTION_SYNCHRONIZE = "synchronize";

    private final ObjectMapper objectMapper;
    private final JobQueue jobQueue;

    public GithubWebhookService(ObjectMapper objectMapper, JobQueue jobQueue) {
        this.objectMapper = objectMapper;
        this.jobQueue = jobQueue;
    }

    /**
     * Processes a GitHub webhook event and returns the appropriate HTTP response.
     *
     * @param event   The GitHub event type
     * @param payload The webhook payload
     * @return ResponseEntity with appropriate status and message
     */
    public ResponseEntity<String> processWebhook(String event, String payload) {
        log.info("Processing GitHub event: {}", event);

        if (!isPullRequestEvent(event)) {
            log.debug("Ignoring non-PR event: {}", event);
            return ResponseEntity.ok("Ignored non-PR event");
        }

        try {
            JsonNode json = objectMapper.readTree(payload);
            String action = extractAction(json);

            if (!isRelevantAction(action)) {
                log.debug("Ignoring PR action: {}", action);
                return ResponseEntity.ok("Ignored action: " + action);
            }

            PullRequestInfo prInfo = extractPullRequestInfo(json);
            logPullRequestInfo(prInfo);

            ReviewJob job = createReviewJob(prInfo);
            jobQueue.enqueue(job);

            log.info("Job enqueued for PR #{} on {}", prInfo.getPrNumber(), prInfo.getRepo());
            return ResponseEntity.ok("PR event handled successfully");

        } catch (Exception e) {
            log.error("Error processing webhook payload", e);
            return ResponseEntity.badRequest().body("Error processing webhook: " + e.getMessage());
        }
    }

    private boolean isPullRequestEvent(String event) {
        return PULL_REQUEST_EVENT.equals(event);
    }

    private String extractAction(JsonNode json) {
        return json.get("action").asText();
    }

    private boolean isRelevantAction(String action) {
        return ACTION_OPENED.equals(action) || ACTION_SYNCHRONIZE.equals(action);
    }

    private PullRequestInfo extractPullRequestInfo(JsonNode json) {
        String repo = json.get("repository").get("full_name").asText();
        int prNumber = json.get("number").asInt();
        String headSha = json.get(PULL_REQUEST_EVENT).get("head").get("sha").asText();
        String baseSha = json.get(PULL_REQUEST_EVENT).get("base").get("sha").asText();

        return new PullRequestInfo(repo, prNumber, headSha, baseSha);
    }

    private void logPullRequestInfo(PullRequestInfo prInfo) {
        log.info("Repo: {}", prInfo.getRepo());
        log.info("PR Number: {}", prInfo.getPrNumber());
        log.info("Base SHA: {}", prInfo.getBaseSha());
        log.info("Head SHA: {}", prInfo.getHeadSha());
    }

    private ReviewJob createReviewJob(PullRequestInfo prInfo) {
        return new ReviewJob(prInfo.getRepo(), prInfo.getHeadSha(), prInfo.getPrNumber(), prInfo.getBaseSha());
    }

    /**
     * Data class to hold pull request information extracted from webhook payload
     */
    private static class PullRequestInfo {
        private final String repo;
        private final int prNumber;
        private final String headSha;
        private final String baseSha;

        public PullRequestInfo(String repo, int prNumber, String headSha, String baseSha) {
            this.repo = repo;
            this.prNumber = prNumber;
            this.headSha = headSha;
            this.baseSha = baseSha;
        }

        public String getRepo() { return repo; }
        public int getPrNumber() { return prNumber; }
        public String getHeadSha() { return headSha; }
        public String getBaseSha() { return baseSha; }
    }

}