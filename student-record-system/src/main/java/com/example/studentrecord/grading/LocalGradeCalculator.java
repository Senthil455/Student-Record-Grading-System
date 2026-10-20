package com.example.studentrecord.grading;

import org.springframework.stereotype.Component;

/**
 * Local copy of the institutional grading scale.
 *
 * <p>It is used only as a resilience fallback when the grading microservice is
 * unavailable, so a lecturer can still record marks during a demo. The
 * authoritative implementation lives in {@code grading-service}.</p>
 */
@Component
public class LocalGradeCalculator {

    public GradeResult evaluate(double marks) {
        if (Double.isNaN(marks) || marks < 0 || marks > 100) {
            throw new IllegalArgumentException("Marks must be between 0 and 100.");
        }

        if (marks >= 90) {
            return new GradeResult("A+", 10.0, "local-fallback");
        }
        if (marks >= 85) {
            return new GradeResult("A", 9.0, "local-fallback");
        }
        if (marks >= 75) {
            return new GradeResult("B+", 8.0, "local-fallback");
        }
        if (marks >= 65) {
            return new GradeResult("B", 7.0, "local-fallback");
        }
        if (marks >= 55) {
            return new GradeResult("C", 6.0, "local-fallback");
        }
        if (marks >= 45) {
            return new GradeResult("D", 5.0, "local-fallback");
        }
        return new GradeResult("F", 0.0, "local-fallback");
    }
}
