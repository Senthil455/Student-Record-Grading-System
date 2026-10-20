package com.example.studentrecord.dto;

import java.time.LocalDate;

/** One transcript/enrollment row used by the UI and the REST API. */
public class EnrollmentView {

    private final Long id;
    private final Long studentId;
    private final String studentName;
    private final String rollNumber;
    private final Long courseId;
    private final String courseCode;
    private final String courseTitle;
    private final int credits;
    private final int semester;
    private final Double marks;
    private final String grade;
    private final Double gradePoint;
    private final LocalDate enrolledOn;
    private final String gradedBy;

    public EnrollmentView(Long id, Long studentId, String studentName, String rollNumber,
                          Long courseId, String courseCode, String courseTitle, int credits,
                          int semester, Double marks, String grade, Double gradePoint,
                          LocalDate enrolledOn, String gradedBy) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.rollNumber = rollNumber;
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.credits = credits;
        this.semester = semester;
        this.marks = marks;
        this.grade = grade;
        this.gradePoint = gradePoint;
        this.enrolledOn = enrolledOn;
        this.gradedBy = gradedBy;
    }

    public Long getId() {
        return id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public String getInitials() {
        if (studentName == null || studentName.isBlank()) {
            return "?";
        }
        String[] parts = studentName.trim().split("\\s+");
        String initials = parts[0].substring(0, 1);
        if (parts.length > 1) {
            initials += parts[parts.length - 1].substring(0, 1);
        }
        return initials.toUpperCase();
    }

    public Long getCourseId() {
        return courseId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public int getCredits() {
        return credits;
    }

    public int getSemester() {
        return semester;
    }

    public Double getMarks() {
        return marks;
    }

    public String getGrade() {
        return grade;
    }

    public Double getGradePoint() {
        return gradePoint;
    }

    public LocalDate getEnrolledOn() {
        return enrolledOn;
    }

    public String getGradedBy() {
        return gradedBy;
    }

    public boolean isPassed() {
        return grade != null && !"F".equalsIgnoreCase(grade);
    }

    /** CSS class used to colour the grade badge. */
    public String getGradeCss() {
        if (grade == null) {
            return "grade-none";
        }
        return switch (grade) {
            case "A+", "A" -> "grade-a";
            case "B+", "B" -> "grade-b";
            case "C" -> "grade-c";
            case "D" -> "grade-d";
            default -> "grade-f";
        };
    }
}
