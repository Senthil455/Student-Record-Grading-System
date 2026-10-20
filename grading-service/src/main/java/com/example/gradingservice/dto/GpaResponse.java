package com.example.gradingservice.dto;

import java.time.Instant;

/** Result returned by POST /api/grade/gpa. */
public record GpaResponse(
        double gpa,
        int totalCredits,
        int courses,
        String evaluatedBy,
        Instant evaluatedAt
) {
}
