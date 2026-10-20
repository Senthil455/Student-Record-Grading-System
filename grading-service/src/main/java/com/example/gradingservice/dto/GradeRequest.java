package com.example.gradingservice.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/** Request body for POST /api/grade/evaluate. */
public record GradeRequest(
        @NotNull(message = "Marks are required.")
        @DecimalMin(value = "0.0", message = "Marks cannot be below 0.")
        @DecimalMax(value = "100.0", message = "Marks cannot be above 100.")
        Double marks
) {
}
