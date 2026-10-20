package com.example.studentrecord.grading;

/** Grade value returned either by the grading microservice or by the local fallback. */
public record GradeResult(String grade, double gradePoint, String source) {
}
