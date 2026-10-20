package com.example.studentrecord.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/** Form-backing object used when an existing enrollment's marks are edited. */
public class MarkForm {

    @NotNull(message = "Marks are required.")
    @DecimalMin(value = "0.0", message = "Marks cannot be below 0.")
    @DecimalMax(value = "100.0", message = "Marks cannot be above 100.")
    private Double marks;

    public Double getMarks() {
        return marks;
    }

    public void setMarks(Double marks) {
        this.marks = marks;
    }
}
