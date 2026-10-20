package com.example.studentrecord.grading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.Map;

/**
 * Client for the independent grading microservice running on port 8081.
 *
 * <p>If the service is unavailable the application logs a warning and falls
 * back to {@link LocalGradeCalculator}. This simple resilience pattern means a
 * network problem never prevents a lecturer from saving marks.</p>
 */
@Component
public class GradingClient {

    private static final Logger log = LoggerFactory.getLogger(GradingClient.class);

    private final RestClient restClient;
    private final LocalGradeCalculator fallback;
    private final boolean enabled;

    public GradingClient(RestClient gradingRestClient,
                         LocalGradeCalculator fallback,
                         @Value("${grading.service.enabled:true}") boolean enabled) {
        this.restClient = gradingRestClient;
        this.fallback = fallback;
        this.enabled = enabled;
    }

    /**
     * Evaluates marks by calling {@code POST /api/grade/evaluate} on the grading
     * service. Returns a local result when the remote call fails.
     */
    public GradeResult evaluate(double marks) {
        if (!enabled) {
            return fallback.evaluate(marks);
        }

        try {
            RemoteGradeResponse response = restClient.post()
                    .uri("/api/grade/evaluate")
                    .body(Map.of("marks", marks))
                    .retrieve()
                    .body(RemoteGradeResponse.class);

            if (response == null || response.grade() == null) {
                log.warn("Grading service returned an empty response; using local fallback.");
                return fallback.evaluate(marks);
            }

            return new GradeResult(response.grade(), response.gradePoint(), "grading-service");
        } catch (RestClientException | IllegalStateException ex) {
            log.warn("Grading service unavailable ({}). Using local fallback for {} marks.",
                    ex.getMessage(), marks);
            return fallback.evaluate(marks);
        }
    }

    /** Used by the dashboard to display whether the second service is running. */
    public boolean isServiceAvailable() {
        if (!enabled) {
            return false;
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.get()
                    .uri("/api/grade/health")
                    .retrieve()
                    .body(Map.class);
            return response != null && "UP".equals(response.get("status"));
        } catch (RestClientException ex) {
            return false;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    private record RemoteGradeResponse(double marks, String grade, double gradePoint,
                                       boolean passed, String evaluatedBy, Instant evaluatedAt) {
    }
}
