package com.example.studentrecord.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/** JSON request body for enrolling a student and recording marks. */
public record EnrollmentRequest(
        @NotNull(message = "Student ID is required.")
        Long studentId,

        @NotNull(message = "Course ID is required.")
        Long courseId,

        @NotNull(message = "Marks are required.")
        @DecimalMin(value = "0.0", message = "Marks cannot be below 0.")
        @DecimalMax(value = "100.0", message = "Marks cannot be above 100.")
        Double marks
) {
}
