package com.example.studentrecord.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Form-backing object for creating and editing a course in the MVC UI. */
public class CourseForm {

    private Long id;

    @NotBlank(message = "Course code is required.")
    @Size(max = 15, message = "Course code must be 15 characters or fewer.")
    private String code;

    @NotBlank(message = "Course title is required.")
    @Size(max = 120)
    private String title;

    @NotNull(message = "Credits are required.")
    @Min(value = 1, message = "A course must have at least 1 credit.")
    @Max(value = 10, message = "A course cannot have more than 10 credits.")
    private Integer credits;

    @NotNull(message = "Semester is required.")
    @Min(value = 1, message = "Semester must be between 1 and 8.")
    @Max(value = 8, message = "Semester must be between 1 and 8.")
    private Integer semester;

    @Size(max = 80)
    private String department;

    @Size(max = 500)
    private String description;

    private Long instructorId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(Long instructorId) {
        this.instructorId = instructorId;
    }
}
