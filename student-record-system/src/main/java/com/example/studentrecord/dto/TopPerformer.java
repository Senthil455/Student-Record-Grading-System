package com.example.studentrecord.dto;

/** Row returned by the HQL top-performer query. */
public class TopPerformer {

    private final Long id;
    private final String rollNumber;
    private final String fullName;
    private final String department;
    private final Double averageMarks;
    private final Long courseCount;

    public TopPerformer(Long id, String rollNumber, String fullName, String department,
                        Double averageMarks, Long courseCount) {
        this.id = id;
        this.rollNumber = rollNumber;
        this.fullName = fullName;
        this.department = department;
        this.averageMarks = averageMarks;
        this.courseCount = courseCount;
    }

    public Long getId() {
        return id;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public String getInitials() {
        if (fullName == null || fullName.isBlank()) {
            return "?";
        }
        String[] parts = fullName.trim().split("\\s+");
        return (parts[0].substring(0, 1) + (parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : ""))
                .toUpperCase();
    }

    public String getDepartment() {
        return department;
    }

    public Double getAverageMarks() {
        return averageMarks;
    }

    public Long getCourseCount() {
        return courseCount;
    }
}
