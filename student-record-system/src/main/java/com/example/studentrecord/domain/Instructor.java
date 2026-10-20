package com.example.studentrecord.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/** An instructor is a {@link Person} who can be assigned to courses. */
@Entity
@Table(name = "instructors")
@PrimaryKeyJoinColumn(name = "person_id")
public class Instructor extends Person {

    @Column(name = "employee_code", nullable = false, unique = true, length = 20)
    private String employeeCode;

    @Column(name = "designation", length = 80)
    private String designation;

    @Column(name = "specialization", length = 120)
    private String specialization;

    @OneToMany(mappedBy = "instructor")
    private List<Course> courses = new ArrayList<>();

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }
}
