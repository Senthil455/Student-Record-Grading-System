package com.example.studentrecord.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** JSON request body for creating or updating a course through the REST API. */
public record CourseRequest(
        @NotBlank(message = "Course code is required.")
        @Size(max = 15) String code,

        @NotBlank(message = "Course title is required.")
        @Size(max = 120) String title,

        @NotNull(message = "Credits are required.")
        @Min(value = 1, message = "A course must have at least 1 credit.")
        @Max(value = 10, message = "A course cannot have more than 10 credits.")
        Integer credits,

        @NotNull(message = "Semester is required.")
        @Min(value = 1, message = "Semester must be between 1 and 8.")
        @Max(value = 8, message = "Semester must be between 1 and 8.")
        Integer semester,

        @Size(max = 80) String department,

        @Size(max = 500) String description,

        Long instructorId
) {
}
