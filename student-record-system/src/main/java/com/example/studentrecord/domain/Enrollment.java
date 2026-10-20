package com.example.studentrecord.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;

/**
 * Join entity between a student and a course. It also stores the awarded marks,
 * the letter grade and the grade point for the transcript.
 */
@Entity
@Table(name = "enrollments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_enrollment_student_course",
                columnNames = {"student_id", "course_id"}))
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "marks")
    private Double marks;

    @Column(name = "grade", length = 3)
    private String grade;

    @Column(name = "grade_point")
    private Double gradePoint;

    @Column(name = "semester", nullable = false)
    private Integer semester;

    @Column(name = "enrolled_on", nullable = false)
    private LocalDate enrolledOn;

    /** "grading-service" or "local-fallback"; useful when demonstrating resilience. */
    @Column(name = "graded_by", length = 30)
    private String gradedBy;

    @PrePersist
    void setDefaults() {
        if (enrolledOn == null) {
            enrolledOn = LocalDate.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Double getMarks() {
        return marks;
    }

    public void setMarks(Double marks) {
        this.marks = marks;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public Double getGradePoint() {
        return gradePoint;
    }

    public void setGradePoint(Double gradePoint) {
        this.gradePoint = gradePoint;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public LocalDate getEnrolledOn() {
        return enrolledOn;
    }

    public void setEnrolledOn(LocalDate enrolledOn) {
        this.enrolledOn = enrolledOn;
    }

    public String getGradedBy() {
        return gradedBy;
    }

    public void setGradedBy(String gradedBy) {
        this.gradedBy = gradedBy;
    }
}
