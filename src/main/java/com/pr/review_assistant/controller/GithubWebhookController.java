package com.pr.review_assistant.controller;

import com.pr.review_assistant.service.GithubWebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook/github")
public class GithubWebhookController {

    private final GithubWebhookService webhookService;

    public GithubWebhookController(GithubWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestHeader("X-GitHub-Event") String event,
            @RequestBody String payload) {
        
        return webhookService.processWebhook(event, payload);
    }
}
