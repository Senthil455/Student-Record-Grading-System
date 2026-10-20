package com.example.gradingservice.dto;

import java.time.Instant;

/** Result returned by POST /api/grade/evaluate. */
public record GradeResponse(
        double marks,
        String grade,
        double gradePoint,
        boolean passed,
        String evaluatedBy,
        Instant evaluatedAt
) {
}
