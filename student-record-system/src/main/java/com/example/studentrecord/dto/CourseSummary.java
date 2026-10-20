package com.example.studentrecord.dto;

/** Read model for a course, including enrollment and performance metrics. */
public class CourseSummary {

    private final Long id;
    private final String code;
    private final String title;
    private final int credits;
    private final int semester;
    private final String department;
    private final String description;
    private final String instructorName;
    private final long enrollmentCount;
    private final Double averageMarks;

    public CourseSummary(Long id, String code, String title, int credits, int semester,
                         String department, String description, String instructorName,
                         long enrollmentCount, Double averageMarks) {
        this.id = id;
        this.code = code;
        this.title = title;
        this.credits = credits;
        this.semester = semester;
        this.department = department;
        this.description = description;
        this.instructorName = instructorName;
        this.enrollmentCount = enrollmentCount;
        this.averageMarks = averageMarks;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public int getCredits() {
        return credits;
    }

    public int getSemester() {
        return semester;
    }

    public String getDepartment() {
        return department;
    }

    public String getDescription() {
        return description;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public long getEnrollmentCount() {
        return enrollmentCount;
    }

    public Double getAverageMarks() {
        return averageMarks;
    }
}
