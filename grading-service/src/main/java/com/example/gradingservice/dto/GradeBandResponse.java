package com.example.gradingservice.dto;

/** One row of the grading scale returned by GET /api/grade/scale. */
public record GradeBandResponse(
        String grade,
        double minimumMarks,
        double maximumMarks,
        double gradePoint,
        boolean passed,
        String remark
) {
}
