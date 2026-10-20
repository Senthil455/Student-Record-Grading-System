package com.example.studentrecord.dto;

/** Lightweight option used in student dropdowns. */
public class StudentOption {

    private final Long id;
    private final String fullName;
    private final String rollNumber;

    public StudentOption(Long id, String fullName, String rollNumber) {
        this.id = id;
        this.fullName = fullName;
        this.rollNumber = rollNumber;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRollNumber() {
        return rollNumber;
    }
}
