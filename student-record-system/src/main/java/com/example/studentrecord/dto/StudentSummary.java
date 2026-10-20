package com.example.studentrecord.dto;

import java.time.LocalDate;

/** Read model used in lists and REST responses; contains derived academic metrics. */
public class StudentSummary {

    private final Long id;
    private final String rollNumber;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phone;
    private final LocalDate dateOfBirth;
    private final String department;
    private final int admissionYear;
    private final boolean active;
    private final long courseCount;
    private final Double averageMarks;
    private final Double gpa;

    public StudentSummary(Long id, String rollNumber, String firstName, String lastName, String email,
                          String phone, LocalDate dateOfBirth, String department, int admissionYear, boolean active,
                          long courseCount, Double averageMarks, Double gpa) {
        this.id = id;
        this.rollNumber = rollNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.department = department;
        this.admissionYear = admissionYear;
        this.active = active;
        this.courseCount = courseCount;
        this.averageMarks = averageMarks;
        this.gpa = gpa;
    }

    public Long getId() {
        return id;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getInitials() {
        String first = firstName == null || firstName.isBlank() ? "" : firstName.substring(0, 1);
        String last = lastName == null || lastName.isBlank() ? "" : lastName.substring(0, 1);
        return (first + last).toUpperCase();
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getDepartment() {
        return department;
    }

    public int getAdmissionYear() {
        return admissionYear;
    }

    public boolean isActive() {
        return active;
    }

    public long getCourseCount() {
        return courseCount;
    }

    public Double getAverageMarks() {
        return averageMarks;
    }

    public Double getGpa() {
        return gpa;
    }
}
