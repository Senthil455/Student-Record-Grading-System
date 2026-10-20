package com.example.studentrecord.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/** Form-backing object for enrolling a student in a course. */
public class EnrollmentForm {

    @NotNull(message = "Select a student.")
    private Long studentId;

    @NotNull(message = "Select a course.")
    private Long courseId;

    @NotNull(message = "Marks are required.")
    @DecimalMin(value = "0.0", message = "Marks cannot be below 0.")
    @DecimalMax(value = "100.0", message = "Marks cannot be above 100.")
    private Double marks;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Double getMarks() {
        return marks;
    }

    public void setMarks(Double marks) {
        this.marks = marks;
    }
}
