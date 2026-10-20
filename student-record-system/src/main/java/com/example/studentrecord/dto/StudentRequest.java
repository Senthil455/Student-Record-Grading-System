package com.example.studentrecord.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** JSON request body for creating or updating a student through the REST API. */
public record StudentRequest(
        @NotBlank(message = "First name is required.")
        @Size(max = 60) String firstName,

        @NotBlank(message = "Last name is required.")
        @Size(max = 60) String lastName,

        @NotBlank(message = "Email is required.")
        @Email(message = "Enter a valid email address.")
        @Size(max = 120) String email,

        @Pattern(regexp = "^$|^[0-9+\\-() ]{7,20}$", message = "Enter a valid phone number.")
        String phone,

        @Past(message = "Date of birth must be in the past.")
        LocalDate dateOfBirth,

        @NotBlank(message = "Roll number is required.")
        @Size(max = 20) String rollNumber,

        @NotBlank(message = "Department is required.")
        @Size(max = 80) String department,

        @NotNull(message = "Admission year is required.")
        @Min(value = 2000, message = "Admission year must be 2000 or later.")
        @Max(value = 2100, message = "Admission year must be 2100 or earlier.")
        Integer admissionYear,

        Boolean active
) {
}
