package com.example.studentrecord.repository;

import com.example.studentrecord.domain.Course;
import com.example.studentrecord.domain.Enrollment;
import com.example.studentrecord.domain.Instructor;
import com.example.studentrecord.domain.Student;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository-level tests for the explicit HQL reports and the JOINED
 * inheritance mapping. @DataJpaTest is transactional and rolls back.
 */
@DataJpaTest
@ActiveProfiles("test")
class RepositoryHqlTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private EntityManager entityManager;

    private Student highPerformer;
    private Student lowPerformer;
    private Student studentWithoutCourses;
    private Course course;

    @BeforeEach
    void setUp() {
        Instructor instructor = new Instructor();
        instructor.setFirstName("Test");
        instructor.setLastName("Instructor");
        instructor.setEmail("instructor@test.edu");
        instructor.setEmployeeCode("EMP9001");
        instructor.setDesignation("Professor");
        instructorRepository.save(instructor);

        course = new Course();
        course.setCode("HQL101");
        course.setTitle("HQL Testing");
        course.setCredits(4);
        course.setSemester(2);
        course.setDepartment("B.Sc Computer Science");
        course.setInstructor(instructor);
        courseRepository.save(course);

        highPerformer = student("Ananya", "High", "high@test.edu", "HQL001", "B.Sc Computer Science");
        lowPerformer = student("Ravi", "Low", "low@test.edu", "HQL002", "B.Sc Computer Science");
        studentWithoutCourses = student("Nina", "New", "new@test.edu", "HQL003", "BCA");

        enrollment(highPerformer, course, 90.0, "A+", 10.0);
        enrollment(lowPerformer, course, 60.0, "C", 6.0);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void topPerformerHqlAppliesGroupingHavingAndOrdering() {
        var above80 = studentRepository.findTopPerformers(80.0);
        assertThat(above80).hasSize(1);
        assertThat(above80.get(0).getRollNumber()).isEqualTo("HQL001");
        assertThat(above80.get(0).getAverageMarks()).isEqualTo(90.0);
        assertThat(above80.get(0).getCourseCount()).isEqualTo(1L);

        var all = studentRepository.findTopPerformers(0.0);
        assertThat(all).extracting(row -> row.getRollNumber())
                .containsExactly("HQL001", "HQL002");
    }

    @Test
    void courseStatisticsHqlAggregatesMarks() {
        var stats = enrollmentRepository.findCourseStatistics();

        assertThat(stats).hasSize(1);
        assertThat(stats.get(0).getCode()).isEqualTo("HQL101");
        assertThat(stats.get(0).getAverageMarks()).isEqualTo(75.0);
        assertThat(stats.get(0).getHighestMarks()).isEqualTo(90.0);
        assertThat(stats.get(0).getLowestMarks()).isEqualTo(60.0);
        assertThat(stats.get(0).getEnrollmentCount()).isEqualTo(2L);
    }

    @Test
    void findsStudentsWithoutEnrollmentsUsingSubquery() {
        assertThat(studentRepository.findStudentsWithoutEnrollments())
                .extracting(Student::getRollNumber)
                .containsExactly("HQL003");
    }

    @Test
    void groupsStudentsByDepartment() {
        assertThat(studentRepository.countByDepartment())
                .extracting(row -> row.getDepartment() + ":" + row.getStudentCount())
                .containsExactlyInAnyOrder(
                        "B.Sc Computer Science:2",
                        "BCA:1");
    }

    @Test
    void joinedInheritanceStoresSharedAndSubclassColumnsInSeparateTables() {
        Number personRows = (Number) entityManager.createNativeQuery("select count(*) from persons").getSingleResult();
        Number studentRows = (Number) entityManager.createNativeQuery("select count(*) from students").getSingleResult();
        Number instructorRows = (Number) entityManager.createNativeQuery("select count(*) from instructors").getSingleResult();

        assertThat(personRows.longValue()).isEqualTo(4L);       // 3 students + 1 instructor
        assertThat(studentRows.longValue()).isEqualTo(3L);      // only students
        assertThat(instructorRows.longValue()).isEqualTo(1L);   // only instructors

        Long polymorphicCount = entityManager
                .createQuery("select count(p) from Person p", Long.class)
                .getSingleResult();
        assertThat(polymorphicCount).isEqualTo(4L);
    }

    private Student student(String firstName, String lastName, String email,
                            String rollNumber, String department) {
        Student student = new Student();
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setEmail(email);
        student.setRollNumber(rollNumber);
        student.setDepartment(department);
        student.setAdmissionYear(2024);
        student.setDateOfBirth(LocalDate.of(2005, 1, 1));
        student.setActive(true);
        return studentRepository.save(student);
    }

    private void enrollment(Student student, Course course, double marks, String grade, double gradePoint) {
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setMarks(marks);
        enrollment.setGrade(grade);
        enrollment.setGradePoint(gradePoint);
        enrollment.setGradedBy("test");
        enrollment.setSemester(course.getSemester());
        enrollment.setEnrolledOn(LocalDate.now());
        enrollmentRepository.save(enrollment);
    }
}
