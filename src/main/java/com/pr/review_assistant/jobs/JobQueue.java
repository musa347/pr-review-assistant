package com.pr.review_assistant.jobs;

import com.pr.review_assistant.analysis.AnalysisRunner;
import com.pr.review_assistant.database.ReviewJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Component
public class JobQueue {
    private final BlockingQueue<ReviewJob> queue = new LinkedBlockingQueue<>();
    private final AnalysisRunner analysisRunner;

    public JobQueue(AnalysisRunner analysisRunner) {
        this.analysisRunner = analysisRunner;
    }

    public void enqueue(ReviewJob job) {
        queue.add(job);
        log.info("Enqueued job for PR #{} on {}. Queue size: {}", 
                job.getPrNumber(), job.getRepo(), queue.size());
    }

    @Scheduled(fixedDelay = 5000)
    public void processJobs() {
        ReviewJob job = queue.poll();
        if (job != null) {
            log.info("Processing job for PR #{} on {}", job.getPrNumber(), job.getRepo());
            analysisRunner.run(job);
        }
    }
}
