package com.example.studentrecord.dto;

/** Number of enrollments for a letter grade. */
public class GradeCount {

    private final String grade;
    private final Long count;

    public GradeCount(String grade, Long count) {
        this.grade = grade;
        this.count = count;
    }

    public String getGrade() {
        return grade;
    }

    public Long getCount() {
        return count;
    }

    public String getGradeCss() {
        return switch (grade) {
            case "A+", "A" -> "grade-a";
            case "B+", "B" -> "grade-b";
            case "C" -> "grade-c";
            case "D" -> "grade-d";
            default -> "grade-f";
        };
    }
}
