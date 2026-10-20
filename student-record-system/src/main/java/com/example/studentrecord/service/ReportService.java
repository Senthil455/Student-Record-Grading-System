package com.example.studentrecord.service;

import com.example.studentrecord.dto.CourseStat;
import com.example.studentrecord.dto.DepartmentStat;
import com.example.studentrecord.dto.GradeCount;
import com.example.studentrecord.dto.TopPerformer;
import com.example.studentrecord.repository.EnrollmentRepository;
import com.example.studentrecord.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Read-only reporting service built on explicit HQL aggregate queries. */
@Service
public class ReportService {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;

    public ReportService(StudentRepository studentRepository, EnrollmentRepository enrollmentRepository) {
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional(readOnly = true)
    public List<TopPerformer> topPerformers(double minimumAverage) {
        double threshold = Math.max(0.0, Math.min(100.0, minimumAverage));
        return studentRepository.findTopPerformers(threshold).stream()
                .map(row -> new TopPerformer(
                        row.getId(),
                        row.getRollNumber(),
                        row.getFirstName() + " " + row.getLastName(),
                        row.getDepartment(),
                        round(row.getAverageMarks()),
                        row.getCourseCount()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseStat> courseStatistics() {
        return enrollmentRepository.findCourseStatistics().stream()
                .map(row -> new CourseStat(
                        row.getCode(),
                        row.getTitle(),
                        round(row.getAverageMarks()),
                        row.getEnrollmentCount(),
                        round(row.getHighestMarks()),
                        round(row.getLowestMarks())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DepartmentStat> departmentStatistics() {
        return studentRepository.countByDepartment().stream()
                .map(row -> new DepartmentStat(row.getDepartment(), row.getStudentCount()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GradeCount> gradeDistribution() {
        return enrollmentRepository.countByGrade().stream()
                .map(row -> new GradeCount(row.getGrade(), row.getGradeCount()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Double overallAverage() {
        return round(enrollmentRepository.findOverallAverage());
    }

    private Double round(Double value) {
        return value == null ? null : Math.round(value * 100.0) / 100.0;
    }
}
