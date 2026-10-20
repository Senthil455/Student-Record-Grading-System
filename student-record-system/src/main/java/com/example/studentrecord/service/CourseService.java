package com.example.studentrecord.service;

import com.example.studentrecord.domain.Course;
import com.example.studentrecord.domain.Enrollment;
import com.example.studentrecord.domain.Instructor;
import com.example.studentrecord.dto.CourseDetail;
import com.example.studentrecord.dto.CourseForm;
import com.example.studentrecord.dto.CourseOption;
import com.example.studentrecord.dto.CourseSummary;
import com.example.studentrecord.dto.EnrollmentView;
import com.example.studentrecord.dto.InstructorOption;
import com.example.studentrecord.exception.DuplicateResourceException;
import com.example.studentrecord.exception.ResourceNotFoundException;
import com.example.studentrecord.repository.CourseRepository;
import com.example.studentrecord.repository.InstructorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Business logic for course CRUD, rosters and performance summaries. */
@Service
public class CourseService {

    private static final List<String> GRADE_ORDER = List.of("A+", "A", "B+", "B", "C", "D", "F");

    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;

    public CourseService(CourseRepository courseRepository, InstructorRepository instructorRepository) {
        this.courseRepository = courseRepository;
        this.instructorRepository = instructorRepository;
    }

    @Transactional(readOnly = true)
    public List<CourseSummary> findAll(String query) {
        List<Course> courses = (query == null || query.isBlank())
                ? courseRepository.findAllWithDetails()
                : courseRepository.searchWithDetails(query.trim());
        return courses.stream().map(this::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public List<CourseOption> findOptions() {
        return courseRepository.findAllByOrderByCodeAsc().stream()
                .map(c -> new CourseOption(c.getId(), c.getCode(), c.getTitle(), c.getCredits(), c.getSemester()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InstructorOption> findInstructorOptions() {
        return instructorRepository.findAllByOrderByFirstNameAscLastNameAsc().stream()
                .map(i -> new InstructorOption(i.getId(), i.getFullName(), i.getEmployeeCode(), i.getDesignation()))
                .toList();
    }

    @Transactional(readOnly = true)
    public CourseSummary findSummary(Long id) {
        return toSummary(requireCourse(id));
    }

    @Transactional(readOnly = true)
    public CourseDetail findDetail(Long id) {
        Course course = courseRepository.findDetailedById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Course", id));

        List<EnrollmentView> enrollments = course.getEnrollments().stream()
                .sorted(Comparator.comparing(e -> e.getStudent().getRollNumber()))
                .map(this::toView)
                .toList();

        Map<String, Long> gradeCounts = new LinkedHashMap<>();
        GRADE_ORDER.forEach(grade -> gradeCounts.put(grade, 0L));
        for (Enrollment enrollment : course.getEnrollments()) {
            if (enrollment.getGrade() != null) {
                gradeCounts.merge(enrollment.getGrade(), 1L, Long::sum);
            }
        }

        long graded = course.getEnrollments().stream()
                .filter(e -> e.getGrade() != null)
                .count();
        long passed = course.getEnrollments().stream()
                .filter(e -> e.getGrade() != null && !"F".equalsIgnoreCase(e.getGrade()))
                .count();
        double passRate = graded == 0 ? 0.0 : Math.round((passed * 1000.0) / graded) / 10.0;

        return new CourseDetail(toSummary(course), enrollments, gradeCounts, passRate);
    }

    @Transactional(readOnly = true)
    public CourseForm findForm(Long id) {
        Course course = requireCourse(id);
        CourseForm form = new CourseForm();
        form.setId(course.getId());
        form.setCode(course.getCode());
        form.setTitle(course.getTitle());
        form.setCredits(course.getCredits());
        form.setSemester(course.getSemester());
        form.setDepartment(course.getDepartment());
        form.setDescription(course.getDescription());
        form.setInstructorId(course.getInstructor() == null ? null : course.getInstructor().getId());
        return form;
    }

    @Transactional
    public CourseSummary create(CourseForm form) {
        String code = normalizeCode(form.getCode());
        if (courseRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateResourceException("Course code " + code + " already exists.");
        }

        Course course = new Course();
        apply(form, course, code);
        Course saved = courseRepository.save(course);
        return toSummary(saved);
    }

    @Transactional
    public CourseSummary update(Long id, CourseForm form) {
        Course course = requireCourse(id);
        String code = normalizeCode(form.getCode());
        if (courseRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new DuplicateResourceException("Course code " + code + " already exists.");
        }

        apply(form, course, code);
        return toSummary(course);
    }

    @Transactional
    public void delete(Long id) {
        Course course = requireCourse(id);
        courseRepository.delete(course);
    }

    @Transactional(readOnly = true)
    public long count() {
        return courseRepository.count();
    }

    private void apply(CourseForm form, Course course, String code) {
        course.setCode(code);
        course.setTitle(form.getTitle().trim());
        course.setCredits(form.getCredits());
        course.setSemester(form.getSemester());
        course.setDepartment(blankToNull(form.getDepartment()));
        course.setDescription(blankToNull(form.getDescription()));

        if (form.getInstructorId() == null) {
            course.setInstructor(null);
        } else {
            Instructor instructor = instructorRepository.findById(form.getInstructorId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Instructor", form.getInstructorId()));
            course.setInstructor(instructor);
        }
    }

    private Course requireCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Course", id));
    }

    private CourseSummary toSummary(Course course) {
        List<Enrollment> enrollments = course.getEnrollments();
        java.util.OptionalDouble averageValue = enrollments.stream()
                .map(Enrollment::getMarks)
                .filter(m -> m != null)
                .mapToDouble(Double::doubleValue)
                .average();
        Double average = averageValue.isPresent() ? averageValue.getAsDouble() : null;

        return new CourseSummary(
                course.getId(),
                course.getCode(),
                course.getTitle(),
                course.getCredits(),
                course.getSemester(),
                course.getDepartment(),
                course.getDescription(),
                course.getInstructor() == null ? "Unassigned" : course.getInstructor().getFullName(),
                enrollments.size(),
                round(average)
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

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private Double round(Double value) {
        return value == null ? null : Math.round(value * 100.0) / 100.0;
    }
}
