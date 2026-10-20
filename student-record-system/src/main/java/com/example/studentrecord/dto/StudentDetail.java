package com.example.studentrecord.dto;

import java.util.List;

/** Full student profile including transcript lines and credit totals. */
public class StudentDetail {

    private final StudentSummary student;
    private final List<EnrollmentView> enrollments;
    private final double totalCredits;
    private final int gradedCourses;
    private final int passedCourses;

    public StudentDetail(StudentSummary student, List<EnrollmentView> enrollments,
                         double totalCredits, int gradedCourses, int passedCourses) {
        this.student = student;
        this.enrollments = enrollments;
        this.totalCredits = totalCredits;
        this.gradedCourses = gradedCourses;
        this.passedCourses = passedCourses;
    }

    public StudentSummary getStudent() {
        return student;
    }

    public List<EnrollmentView> getEnrollments() {
        return enrollments;
    }

    public double getTotalCredits() {
        return totalCredits;
    }

    public int getGradedCourses() {
        return gradedCourses;
    }

    public int getPassedCourses() {
        return passedCourses;
    }

    public double getGpa() {
        return student.getGpa() == null ? 0.0 : student.getGpa();
    }
}
