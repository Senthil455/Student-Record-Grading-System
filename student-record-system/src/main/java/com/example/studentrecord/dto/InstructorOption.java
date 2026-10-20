package com.example.studentrecord.dto;

/** Instructor displayed in the course form and course pages. */
public class InstructorOption {

    private final Long id;
    private final String fullName;
    private final String employeeCode;
    private final String designation;

    public InstructorOption(Long id, String fullName, String employeeCode, String designation) {
        this.id = id;
        this.fullName = fullName;
        this.employeeCode = employeeCode;
        this.designation = designation;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public String getDesignation() {
        return designation;
    }
}
