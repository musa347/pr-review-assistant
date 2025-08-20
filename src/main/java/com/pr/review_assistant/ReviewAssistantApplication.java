package com.pr.review_assistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ReviewAssistantApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReviewAssistantApplication.class, args);
	}

}
