package com.example.gradingservice.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Request body for POST /api/grade/gpa. */
public record GpaRequest(
        @NotEmpty(message = "At least one grade point is required.")
        List<@NotNull @DecimalMin("0.0") @DecimalMax("10.0") Double> gradePoints,

        @NotEmpty(message = "At least one credit value is required.")
        List<@NotNull @Min(1) Integer> credits
) {
}
