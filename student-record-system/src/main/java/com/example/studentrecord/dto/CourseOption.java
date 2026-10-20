package com.example.studentrecord.dto;

/** Lightweight option used in course dropdowns. */
public class CourseOption {

    private final Long id;
    private final String code;
    private final String title;
    private final int credits;
    private final int semester;

    public CourseOption(Long id, String code, String title, int credits, int semester) {
        this.id = id;
        this.code = code;
        this.title = title;
        this.credits = credits;
        this.semester = semester;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public int getCredits() {
        return credits;
    }

    public int getSemester() {
        return semester;
    }
}
