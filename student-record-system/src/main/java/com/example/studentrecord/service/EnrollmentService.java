package com.example.studentrecord.service;

import com.example.studentrecord.domain.Course;
import com.example.studentrecord.domain.Enrollment;
import com.example.studentrecord.domain.Student;
import com.example.studentrecord.dto.EnrollmentForm;
import com.example.studentrecord.dto.EnrollmentView;
import com.example.studentrecord.exception.DuplicateResourceException;
import com.example.studentrecord.exception.ResourceNotFoundException;
import com.example.studentrecord.grading.GradeResult;
import com.example.studentrecord.grading.GradingClient;
import com.example.studentrecord.repository.CourseRepository;
import com.example.studentrecord.repository.EnrollmentRepository;
import com.example.studentrecord.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** Enrollment and grade-recording workflow. */
@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final GradingClient gradingClient;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             StudentRepository studentRepository,
                             CourseRepository courseRepository,
                             GradingClient gradingClient) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.gradingClient = gradingClient;
    }

    @Transactional(readOnly = true)
    public List<EnrollmentView> findAll() {
        return enrollmentRepository.findAllDetailed().stream().map(this::toView).toList();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentView> findByStudent(Long studentId) {
        return enrollmentRepository.findByStudentIdDetailed(studentId).stream().map(this::toView).toList();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentView> findRecent() {
        return enrollmentRepository.findTop5ByOrderByIdDesc().stream().map(this::toView).toList();
    }

    @Transactional(readOnly = true)
    public EnrollmentView findView(Long id) {
        return toView(requireEnrollment(id));
    }

    /**
     * Creates an enrollment and asks the grading microservice to evaluate the
     * marks. The returned grade and grade point are persisted with the
     * enrollment so reports can query them efficiently later.
     */
    @Transactional
    public EnrollmentView create(EnrollmentForm form) {
        Student student = studentRepository.findById(form.getStudentId())
                .orElseThrow(() -> ResourceNotFoundException.of("Student", form.getStudentId()));
        Course course = courseRepository.findById(form.getCourseId())
                .orElseThrow(() -> ResourceNotFoundException.of("Course", form.getCourseId()));

        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            throw new DuplicateResourceException(
                    student.getFullName() + " is already enrolled in " + course.getCode() + ".");
        }

        GradeResult grade = gradingClient.evaluate(form.getMarks());

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setMarks(form.getMarks());
        enrollment.setGrade(grade.grade());
        enrollment.setGradePoint(grade.gradePoint());
        enrollment.setGradedBy(grade.source());
        enrollment.setSemester(course.getSemester());
        enrollment.setEnrolledOn(LocalDate.now());

        // Keep both sides of the relationship consistent inside the current
        // persistence context (important for tests and immediate redirects).
        student.getEnrollments().add(enrollment);
        course.getEnrollments().add(enrollment);

        return toView(enrollmentRepository.save(enrollment));
    }

    @Transactional
    public EnrollmentView updateMarks(Long id, Double marks) {
        Enrollment enrollment = requireEnrollment(id);
        GradeResult grade = gradingClient.evaluate(marks);
        enrollment.setMarks(marks);
        enrollment.setGrade(grade.grade());
        enrollment.setGradePoint(grade.gradePoint());
        enrollment.setGradedBy(grade.source());
        return toView(enrollment);
    }

    @Transactional
    public void delete(Long id) {
        Enrollment enrollment = requireEnrollment(id);
        enrollmentRepository.delete(enrollment);
    }

    @Transactional(readOnly = true)
    public long count() {
        return enrollmentRepository.count();
    }

    @Transactional(readOnly = true)
    public long gradedCount() {
        return enrollmentRepository.countByGradeIsNotNull();
    }

    @Transactional(readOnly = true)
    public Double overallAverage() {
        Double average = enrollmentRepository.findOverallAverage();
        return average == null ? null : Math.round(average * 100.0) / 100.0;
    }

    public boolean isGradingServiceOnline() {
        return gradingClient.isServiceAvailable();
    }

    private Enrollment requireEnrollment(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Enrollment", id));
    }

    private EnrollmentView toView(Enrollment enrollment) {
        return new EnrollmentView(
                enrollment.getId(),
                enrollment.getStudent().getId(),
                enrollment.getStudent().getFullName(),
                enrollment.getStudent().getRollNumber(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getCode(),
                enrollment.getCourse().getTitle(),
                enrollment.getCourse().getCredits(),
                enrollment.getSemester(),
                enrollment.getMarks(),
                enrollment.getGrade(),
                enrollment.getGradePoint(),
                enrollment.getEnrolledOn(),
                enrollment.getGradedBy()
        );
    }
}
