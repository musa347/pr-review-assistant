package com.pr.review_assistant.database;

import lombok.Data;

@Data
public class ReviewJob {
    private final String repo;
    private final String headSha;
    private final int prNumber;
    private final String baseSha;
}
