package com.example.gradingservice.service;

import com.example.gradingservice.dto.GradeBandResponse;
import com.example.gradingservice.dto.GradeResponse;
import com.example.gradingservice.dto.GpaResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Central implementation of the grading scale used by the institution.
 *
 * <p>The main application calls this service through its REST API. The same
 * scale is intentionally duplicated only as a small fallback in the main
 * application so the UI still works when the grading service is offline.</p>
 */
@Service
public class GradeCalculator {

    /** Ordered from the highest band to the lowest band. */
    public enum Band {
        A_PLUS("A+", 90.0, 10.0, true, "Outstanding"),
        A("A", 85.0, 9.0, true, "Excellent"),
        B_PLUS("B+", 75.0, 8.0, true, "Very good"),
        B("B", 65.0, 7.0, true, "Good"),
        C("C", 55.0, 6.0, true, "Average"),
        D("D", 45.0, 5.0, true, "Pass"),
        F("F", 0.0, 0.0, false, "Fail");

        private final String grade;
        private final double minimumMarks;
        private final double gradePoint;
        private final boolean passed;
        private final String remark;

        Band(String grade, double minimumMarks, double gradePoint, boolean passed, String remark) {
            this.grade = grade;
            this.minimumMarks = minimumMarks;
            this.gradePoint = gradePoint;
            this.passed = passed;
            this.remark = remark;
        }

        public String getGrade() {
            return grade;
        }

        public double getMinimumMarks() {
            return minimumMarks;
        }

        public double getGradePoint() {
            return gradePoint;
        }

        public boolean isPassed() {
            return passed;
        }

        public String getRemark() {
            return remark;
        }
    }

    /**
     * Converts a percentage score into a letter grade and grade point.
     *
     * @param marks percentage in the inclusive range 0-100
     */
    public GradeResponse evaluate(double marks) {
        validateMarks(marks);
        Band band = bandFor(marks);
        return new GradeResponse(
                marks,
                band.getGrade(),
                band.getGradePoint(),
                band.isPassed(),
                "grading-service",
                Instant.now()
        );
    }

    /**
     * Computes a credit-weighted GPA. Credits act as weights, so a 4-credit
     * course affects the GPA more than a 2-credit course.
     */
    public GpaResponse calculateGpa(List<Double> gradePoints, List<Integer> credits) {
        if (gradePoints == null || credits == null || gradePoints.isEmpty() || credits.isEmpty()) {
            throw new IllegalArgumentException("Grade points and credits are required.");
        }
        if (gradePoints.size() != credits.size()) {
            throw new IllegalArgumentException("Every grade point must have a matching credit value.");
        }

        double weightedSum = 0.0;
        int totalCredits = 0;

        for (int i = 0; i < gradePoints.size(); i++) {
            Double point = gradePoints.get(i);
            Integer credit = credits.get(i);
            if (point == null || point < 0.0 || point > 10.0) {
                throw new IllegalArgumentException("Grade points must be between 0 and 10.");
            }
            if (credit == null || credit < 1) {
                throw new IllegalArgumentException("Credits must be at least 1.");
            }
            weightedSum += point * credit;
            totalCredits += credit;
        }

        double gpa = Math.round((weightedSum / totalCredits) * 100.0) / 100.0;
        return new GpaResponse(gpa, totalCredits, gradePoints.size(), "grading-service", Instant.now());
    }

    public List<GradeBandResponse> scale() {
        List<GradeBandResponse> result = new ArrayList<>();
        Band[] bands = Band.values();
        for (int i = 0; i < bands.length; i++) {
            Band band = bands[i];
            double upper = i == 0 ? 100.0 : bands[i - 1].getMinimumMarks();
            result.add(new GradeBandResponse(
                    band.getGrade(),
                    band.getMinimumMarks(),
                    upper,
                    band.getGradePoint(),
                    band.isPassed(),
                    band.getRemark()
            ));
        }
        return result;
    }

    public Band bandFor(double marks) {
        validateMarks(marks);
        for (Band band : Band.values()) {
            if (marks >= band.getMinimumMarks()) {
                return band;
            }
        }
        return Band.F;
    }

    private void validateMarks(double marks) {
        if (Double.isNaN(marks) || marks < 0.0 || marks > 100.0) {
            throw new IllegalArgumentException("Marks must be between 0 and 100.");
        }
    }
}
