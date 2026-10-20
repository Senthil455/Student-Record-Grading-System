package com.example.gradingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Small, independent Spring Boot service that owns the academic grading rules.
 *
 * <p>It runs on port 8081 and is called by the main student-record application over
 * REST. Keeping grading rules in a separate service demonstrates a simple
 * service-oriented (microservice) architecture.</p>
 */
@SpringBootApplication
public class GradingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GradingServiceApplication.class, args);
    }
}
