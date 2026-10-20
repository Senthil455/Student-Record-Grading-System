package com.example.studentrecord.dto;

/** Row returned by the HQL course statistics query. */
public class CourseStat {

    private final String code;
    private final String title;
    private final Double averageMarks;
    private final Long enrollmentCount;
    private final Double highestMarks;
    private final Double lowestMarks;

    public CourseStat(String code, String title, Double averageMarks, Long enrollmentCount,
                      Double highestMarks, Double lowestMarks) {
        this.code = code;
        this.title = title;
        this.averageMarks = averageMarks;
        this.enrollmentCount = enrollmentCount;
        this.highestMarks = highestMarks;
        this.lowestMarks = lowestMarks;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public Double getAverageMarks() {
        return averageMarks;
    }

    public Long getEnrollmentCount() {
        return enrollmentCount;
    }

    public Double getHighestMarks() {
        return highestMarks;
    }

    public Double getLowestMarks() {
        return lowestMarks;
    }
}
