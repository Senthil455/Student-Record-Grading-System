package com.example.studentrecord.dto;

import java.util.List;
import java.util.Map;

/** Full course page with its roster and grade distribution. */
public class CourseDetail {

    private final CourseSummary course;
    private final List<EnrollmentView> enrollments;
    private final Map<String, Long> gradeCounts;
    private final double passRate;

    public CourseDetail(CourseSummary course, List<EnrollmentView> enrollments,
                        Map<String, Long> gradeCounts, double passRate) {
        this.course = course;
        this.enrollments = enrollments;
        this.gradeCounts = gradeCounts;
        this.passRate = passRate;
    }

    public CourseSummary getCourse() {
        return course;
    }

    public List<EnrollmentView> getEnrollments() {
        return enrollments;
    }

    public Map<String, Long> getGradeCounts() {
        return gradeCounts;
    }

    public double getPassRate() {
        return passRate;
    }

    /** CSS class for a letter grade badge. */
    public String gradeCss(String grade) {
        return switch (grade) {
            case "A+", "A" -> "grade-a";
            case "B+", "B" -> "grade-b";
            case "C" -> "grade-c";
            case "D" -> "grade-d";
            default -> "grade-f";
        };
    }
}
