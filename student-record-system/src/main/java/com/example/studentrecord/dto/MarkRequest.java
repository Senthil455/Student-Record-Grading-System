package com.example.studentrecord.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/** JSON request body for changing only the marks of an enrollment. */
public record MarkRequest(
        @NotNull(message = "Marks are required.")
        @DecimalMin(value = "0.0", message = "Marks cannot be below 0.")
        @DecimalMax(value = "100.0", message = "Marks cannot be above 100.")
        Double marks
) {
}
