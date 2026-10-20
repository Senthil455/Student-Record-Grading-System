package com.example.studentrecord.service;

import com.example.studentrecord.domain.Enrollment;
import com.example.studentrecord.domain.Student;
import com.example.studentrecord.dto.EnrollmentView;
import com.example.studentrecord.dto.StudentDetail;
import com.example.studentrecord.dto.StudentForm;
import com.example.studentrecord.dto.StudentOption;
import com.example.studentrecord.dto.StudentSummary;
import com.example.studentrecord.exception.DuplicateResourceException;
import com.example.studentrecord.exception.ResourceNotFoundException;
import com.example.studentrecord.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Business logic for student CRUD and derived academic metrics. */
@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public List<StudentSummary> findAll(String query) {
        List<Student> students = (query == null || query.isBlank())
                ? studentRepository.findAllWithEnrollments()
                : studentRepository.searchWithEnrollments(query.trim());
        return students.stream().map(this::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public List<StudentOption> findOptions() {
        return studentRepository.findAllByOrderByRollNumberAsc().stream()
                .map(s -> new StudentOption(s.getId(), s.getFullName(), s.getRollNumber()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StudentSummary> findWithoutEnrollments() {
        return studentRepository.findStudentsWithoutEnrollments().stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentSummary findSummary(Long id) {
        return toSummary(requireStudent(id));
    }

    @Transactional(readOnly = true)
    public StudentDetail findDetail(Long id) {
        Student student = studentRepository.findDetailedById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Student", id));

        List<Enrollment> enrollments = student.getEnrollments().stream()
                .sorted(Comparator.comparing(e -> e.getCourse().getCode()))
                .toList();

        List<EnrollmentView> views = enrollments.stream().map(this::toView).toList();
        int graded = (int) enrollments.stream().filter(e -> e.getGradePoint() != null).count();
        int passed = (int) enrollments.stream()
                .filter(e -> e.getGrade() != null && !"F".equalsIgnoreCase(e.getGrade()))
                .count();
        double credits = enrollments.stream()
                .mapToDouble(e -> e.getCourse().getCredits())
                .sum();

        return new StudentDetail(toSummary(student), views, credits, graded, passed);
    }

    @Transactional(readOnly = true)
    public StudentForm findForm(Long id) {
        Student student = requireStudent(id);
        StudentForm form = new StudentForm();
        form.setId(student.getId());
        form.setFirstName(student.getFirstName());
        form.setLastName(student.getLastName());
        form.setEmail(student.getEmail());
        form.setPhone(student.getPhone());
        form.setDateOfBirth(student.getDateOfBirth());
        form.setRollNumber(student.getRollNumber());
        form.setDepartment(student.getDepartment());
        form.setAdmissionYear(student.getAdmissionYear());
        form.setActive(student.isActive());
        return form;
    }

    @Transactional
    public StudentSummary create(StudentForm form) {
        String email = normalizeEmail(form.getEmail());
        String rollNumber = normalizeRollNumber(form.getRollNumber());

        if (studentRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("A student with email " + email + " already exists.");
        }
        if (studentRepository.existsByRollNumberIgnoreCase(rollNumber)) {
            throw new DuplicateResourceException("Roll number " + rollNumber + " is already in use.");
        }

        Student student = new Student();
        apply(form, student, email, rollNumber);
        Student saved = studentRepository.save(student);
        return toSummary(saved);
    }

    @Transactional
    public StudentSummary update(Long id, StudentForm form) {
        Student student = requireStudent(id);
        String email = normalizeEmail(form.getEmail());
        String rollNumber = normalizeRollNumber(form.getRollNumber());

        if (studentRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new DuplicateResourceException("A student with email " + email + " already exists.");
        }
        if (studentRepository.existsByRollNumberIgnoreCaseAndIdNot(rollNumber, id)) {
            throw new DuplicateResourceException("Roll number " + rollNumber + " is already in use.");
        }

        apply(form, student, email, rollNumber);
        return toSummary(student);
    }

    @Transactional
    public void delete(Long id) {
        Student student = requireStudent(id);
        studentRepository.delete(student);
    }

    @Transactional(readOnly = true)
    public long count() {
        return studentRepository.count();
    }

    @Transactional(readOnly = true)
    public long activeCount() {
        return studentRepository.countByActiveTrue();
    }

    private void apply(StudentForm form, Student student, String email, String rollNumber) {
        student.setFirstName(form.getFirstName().trim());
        student.setLastName(form.getLastName().trim());
        student.setEmail(email);
        student.setPhone(blankToNull(form.getPhone()));
        student.setDateOfBirth(form.getDateOfBirth());
        student.setRollNumber(rollNumber);
        student.setDepartment(form.getDepartment().trim());
        student.setAdmissionYear(form.getAdmissionYear());
        student.setActive(form.isActive());
    }

    private Student requireStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Student", id));
    }

    private StudentSummary toSummary(Student student) {
        List<Enrollment> enrollments = student.getEnrollments();

        java.util.OptionalDouble averageValue = enrollments.stream()
                .map(Enrollment::getMarks)
                .filter(m -> m != null)
                .mapToDouble(Double::doubleValue)
                .average();
        Double average = averageValue.isPresent() ? averageValue.getAsDouble() : null;

        double weightedPoints = 0.0;
        int gradedCredits = 0;
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getGradePoint() != null && enrollment.getCourse() != null) {
                int credits = enrollment.getCourse().getCredits();
                weightedPoints += enrollment.getGradePoint() * credits;
                gradedCredits += credits;
            }
        }
        Double gpa = gradedCredits == 0 ? null : round(weightedPoints / gradedCredits);

        return new StudentSummary(
                student.getId(),
                student.getRollNumber(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                student.getPhone(),
                student.getDateOfBirth(),
                student.getDepartment(),
                student.getAdmissionYear(),
                student.isActive(),
                enrollments.size(),
                round(average),
                gpa
        );
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

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeRollNumber(String rollNumber) {
        return rollNumber == null ? "" : rollNumber.trim().toUpperCase(Locale.ROOT);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private Double round(Double value) {
        return value == null ? null : Math.round(value * 100.0) / 100.0;
    }
}
