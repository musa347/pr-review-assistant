package com.pr.review_assistant.analysis;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class Finding {
    private String file;           // Display name (basename)
    private String relativePath;   // Path relative to repo root (required for inline comments)
    private int line;
    private int column;
    private String severity;
    private String rule;
    private String message;
    private String source;
    
    // Enhanced fields
    private String explanation;
    private String suggestion;
    private String category;
    private String impact;
}